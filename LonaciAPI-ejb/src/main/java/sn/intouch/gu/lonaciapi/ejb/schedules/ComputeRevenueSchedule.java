package sn.intouch.gu.lonaciapi.ejb.schedules;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import sn.intouch.gu.lonaciapi.ejb.bigquery.services.BigQueryService;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TempTable;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.RevenueService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.TempTableService;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.ComputeParameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ComputeParameterService;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;
import sn.intouch.gu.lonaciapi.ejb.utils.Utils;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timer;
import java.util.*;

@Singleton
@Log4j2
public class ComputeRevenueSchedule {

    private static final String CI_COUNTRY_CODE = "CI";
    private static final String BF_COUNTRY_CODE = "BF";

    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);
    private final RevenueService revenueService = (RevenueService) JNDIUtils
            .lookUpEJB(EJBRegistry.RevenueServiceBean);
    private final TempTableService tempTableService = (TempTableService) JNDIUtils
            .lookUpEJB(EJBRegistry.TempTableService);

    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);
    private final ComputeParameterService computeParameterService = (ComputeParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ComputeParameterServiceBean);

    // @Schedule(dayOfWeek = "*", hour = "*", minute = "*/2", second = "59", persistent = false)
    @Schedule(dayOfWeek = "*", hour = "*/1", minute = "15", persistent = true)
    public void launch(Timer timer) {
        log.info("Starting daily revenue compute for transactions");
        TempTable tempTable = new TempTable("DAILY_" + DateUtil.SIMPLE_DATE_FORMAT_TO_MINUTE.format(new Date()));
        if (!tempTableService.add(tempTable)) {
            log.info("Add temp table failed. The compute already started by another middle for day");
            return;
        }
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(new Date());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date endDate = new Date(cal.getTimeInMillis());
        cal.add(Calendar.HOUR, -1);
        Date startDate = new Date(cal.getTimeInMillis());
        compute(startDate, endDate);
    }

    public void compute(Date startDate, Date endDate) {
        log.info("Running NEW JOB for computing revenue at : START DATE {} AND END DATE : {}", startDate, endDate);
        Iterable<Operator> operators = operatorService.getAll(null);
        Map<String, Operator> operatorsMap = new HashMap<>();
        for (Operator operator : operators) {
            operatorsMap.put(operator.getOperatorId(), operator);
        }
        computeForOperator(startDate, endDate, operatorsMap);
    }

    public void computeForOperator(Date startDate, Date endDate, Map<String, Operator> operators) {

        try {
            Map<String, Revenue> revenues = new HashMap<>();
            for (Operator operator : operators.values()) {
                Revenue revenueEntity = Revenue.builder()
                        .date(startDate)
                        .endDate(endDate)
                        .operator(operator.getOperatorId())
                        .country(operator.getCountry())
                        .build();
                revenues.put(operator.getOperatorId(), revenueService.update(revenueEntity));
            }
            List<Map<String, String>> valuesList = bigQueryService.getSumBetweenDatesAllCategories(startDate, endDate, operators.keySet(), null, Boolean.TRUE);
            log.info("Revenue Computed :: {}", new Gson().toJson(valuesList));
            if (valuesList == null || valuesList.isEmpty()) {
                log.warn("No revenue found for operators");
                return;
            }

            for (Map<String, String> values : valuesList) {
                log.info("Processing operator revenue : {}", values.get("operateur_id"));
                Operator operator = operators.get(values.get("operateur_id"));
                if (operator == null) {
                    log.warn("No operator found in operators. id : {}", values.get("operateur_id"));
                    continue;
                }

                Revenue revenueEntity = getRevenueEntity(values, operator, revenues.get(operator.getOperatorId()));
                if (revenueEntity == null) return;

                revenueService.update(revenueEntity);
            }
        } catch (Exception e) {
            log.error("An error occurred while computing revenue :: ", e);
        }
    }

    private Revenue getRevenueEntity(Map<String, String> values, Operator operator, Revenue opRevenue) {
        if (opRevenue == null) {
            log.warn("No revenue found for operator : {}", operator.getOperatorId());
            return null;
        }
        Double misesOverallVolume = Double.valueOf(values.get("mises"));
        Double gainsOverallVolume = Double.valueOf(values.get("gain"));
        Double bonusOverallVolume = Double.valueOf(values.get("bonus"));
        Double payinOverallVolume = Double.valueOf(values.get("payin"));
        Double payoutOverallVolume = Double.valueOf(values.get("payout"));
        Double withholding = Double.valueOf(values.get("withholding"));

        Double grossGamingProduct;
        Double integratorRemuneration;
        Double revenue;
        Double royalties;

        ComputeParameter computeParameter = computeParameterService.getParameterByOperator(operator.getOperatorId());
        if (computeParameter == null) {
            log.info("Computing default revenue for country : {}", operator.getCountry());
            if (CI_COUNTRY_CODE.equals(operator.getCountry())) {
                grossGamingProduct = Math.abs(misesOverallVolume) - (Math.abs(gainsOverallVolume) + Math.abs(bonusOverallVolume));
                integratorRemuneration = 0.04 * Math.abs(payinOverallVolume) + 0.02 * Math.abs(payoutOverallVolume);
                revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                royalties = 0.5 * revenue;
            } else if (BF_COUNTRY_CODE.equals(operator.getCountry())) {
                log.warn("No revenue default computation for country : {}", operator.getCountry());
                return null;
            } else {
                log.warn("Country {} is not supported for computing revenue.", operator.getCountry());
                return null;
            }
        } else {
            log.info("Computing revenue for operator : {}", operator.getOperatorId());
            if (CI_COUNTRY_CODE.equals(operator.getCountry())) {
                grossGamingProduct = Math.abs(misesOverallVolume) - (Math.abs(gainsOverallVolume) + Math.abs(bonusOverallVolume));
                ;
                integratorRemuneration = computeParameter.getPaymentRate() * (computeParameter.getPaymentFees() * Math.abs(payinOverallVolume))
                        + computeParameter.getCashinRate() * (computeParameter.getCashinFees() * Math.abs(payoutOverallVolume));
                revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                royalties = computeParameter.getRoyaltyRate() * revenue;
            } else if (Boolean.TRUE.equals(operator.getNotified())) {
                grossGamingProduct = computeParameter.getPaymentRate() * (Math.abs(misesOverallVolume) - Math.abs(bonusOverallVolume))
                        - computeParameter.getCashinRate() * Math.abs(gainsOverallVolume);
                ;
                integratorRemuneration = computeParameter.getPaymentFees() * Math.abs(payinOverallVolume)
                        + computeParameter.getCashinFees() * Math.abs(payoutOverallVolume);
                revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                royalties = computeParameter.getRoyaltyRate() * revenue;
            } else {
                grossGamingProduct = computeParameter.getPaymentRate() * Math.abs(payinOverallVolume)
                        - computeParameter.getCashinRate() * Math.abs(payoutOverallVolume);
                ;
                integratorRemuneration = computeParameter.getPaymentFees() * Math.abs(payinOverallVolume)
                        + computeParameter.getCashinFees() * Math.abs(payoutOverallVolume);
                revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                royalties = computeParameter.getRoyaltyRate() * revenue;
                grossGamingProduct = 0D;
            }
        }
        opRevenue.setGrossGamingProduct(grossGamingProduct);
        opRevenue.setIntegratorRemuneration(integratorRemuneration);
        opRevenue.setRevenue(revenue);
        opRevenue.setRoyalties(royalties);
        opRevenue.setPayin(payinOverallVolume);
        opRevenue.setPayout(payoutOverallVolume);
        opRevenue.setMises(misesOverallVolume);
        opRevenue.setGain(gainsOverallVolume);
        opRevenue.setBonus(bonusOverallVolume);
        opRevenue.setWithholding(withholding);
        return opRevenue;
    }



    // @Schedule(dayOfWeek = "*", hour = "*", minute = "*/2", second = "59", persistent = false)
    @Schedule(dayOfMonth = "14", hour = "3", minute = "0", persistent = true)
    public void launchEvery14h(Timer timer) {
        log.info("Starting monthly revenue reconciliation for missed transactions");
        TempTable tempTable = new TempTable("MONTHLY_" + DateUtil.SIMPLE_DATE_FORMAT_TO_MINUTE.format(new Date()));
        if (!tempTableService.add(tempTable)) {
            log.info("Add temp table failed. The compute already started by another middle for month");
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date endDate = new Date(cal.getTimeInMillis());

        // Go back 30 days
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = new Date(cal.getTimeInMillis());
        reComputeAllBetweenDates(startDate, endDate, null);
    }

    public void reComputeAllBetweenDates(Date startDate, Date endDate, Map<String, Operator> operatorsMap) {
        log.info("Running MONTHLY JOB for computing revenue at : START DATE {} AND END DATE : {}", startDate, endDate);
        Iterable<Operator> operators = operatorService.getAll(null);
        if (operatorsMap == null) {
            operatorsMap = new HashMap<>();
            for (Operator operator : operators) {
                operatorsMap.put(operator.getOperatorId(), operator);
            }
        }
        log.info("Running MONTHLY JOB for computing revenue for operators : {} ", new Gson().toJson(operatorsMap.keySet()));
        try {
            List<Map<String, String>> valuesList = bigQueryService.getSumBetweenDatesGroupedByHour(startDate, endDate, operatorsMap.keySet(), null, Boolean.TRUE);
            log.info("Revenue Computed :: {}", new Gson().toJson(valuesList));
            if (valuesList == null || valuesList.isEmpty()) {
                log.warn("No revenue found for operators");
                return;
            }

            for (Map<String, String> values : valuesList) {
                try {
                    log.info("Processing operator revenue : {}", values.get("operateur_id"));
                    Operator operator = operatorsMap.get(values.get("operateur_id"));
                    if (operator == null) {
                        log.warn("No operator found in operators. id : {}", values.get("operateur_id"));
                        continue;
                    }

                    Date date = Utils.toDate(values.get("hour_timestamp"));
                    Revenue revenueEntity = Revenue.builder()
                            .date(date)
                            .endDate(Utils.addOneHour(date))
                            .operator(operator.getOperatorId())
                            .country(operator.getCountry())
                            .build();
                    revenueEntity = getRevenueEntity(values, operator, revenueEntity);
                    if (revenueEntity == null) return;

                    revenueService.updateRevenueRow(revenueEntity);
                } catch (Exception e) {
                    log.error("An error occurred while saving revenue :: ", e);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while computing revenue :: ", e);
        }
    }
}
