package sn.intouch.gu.lonaciapi.ejb.schedules;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import sn.intouch.gu.lonaciapi.ejb.bigquery.services.BigQueryService;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.RevenueService;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.ComputeParameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ComputeParameterService;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timer;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Singleton
@Log4j2
public class ComputeRevenueSchedule {

    private static final String CI_COUNTRY_CODE = "CI";
    private static final String BF_COUNTRY_CODE = "BF";

    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);
    private final RevenueService revenueService = (RevenueService) JNDIUtils
            .lookUpEJB(EJBRegistry.RevenueServiceBean);

    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);
    private final ComputeParameterService computeParameterService = (ComputeParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ComputeParameterServiceBean);

    // @Schedule(dayOfWeek = "*", hour = "*", minute = "*/2", second = "59", persistent = false)
    @Schedule(dayOfWeek = "*", hour = "*/12", minute = "15", persistent = true)
    public void launch(Timer timer) {

        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(new Date());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date endDate = new Date(cal.getTimeInMillis());
        cal.add(Calendar.HOUR, -12);
        Date startDate = new Date(cal.getTimeInMillis());
        compute(startDate, endDate);
    }

    public void compute(Date startDate, Date endDate) {
        log.info("Running JOB for computing revenue at : START DATE {} AND END DATE : {}", startDate, endDate);
        Iterable<Operator> operators = operatorService.getAll(null);
        for (Operator operator : operators) {
            computeForOperator(startDate, endDate, operator);
        }
    }

    public void computeForOperator(Date startDate, Date endDate, Operator operator) {
        if (Boolean.TRUE.equals(operator.getIsProvider())) {
            log.info("Revenue not computed for operator : {} because it is a provider", operator.getOperatorId());
            return;
        }
        try {
            Revenue revenueEntity = Revenue.builder()
                    .date(startDate)
                    .endDate(endDate)
                    .operator(operator.getOperatorId())
                    .country(operator.getCountry())
                    .build();
            revenueEntity = revenueService.update(revenueEntity);
            Map<String, String> values = bigQueryService.getSumBetweenDatesAllCategories(startDate, endDate, operator.getOperatorId(), null, Boolean.TRUE, operator.getCountry());
            log.info("Revenue Computed :: {}", new Gson().toJson(values));
            if (values == null || values.isEmpty()) {
                log.warn("No revenue found for operator : {}", operator.getOperatorId());
                return;
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
                    grossGamingProduct = Math.abs(misesOverallVolume) - (Math.abs(gainsOverallVolume)  + Math.abs(bonusOverallVolume));
                    integratorRemuneration = 0.04 * Math.abs(payinOverallVolume) + 0.02 * Math.abs(payoutOverallVolume);
                    revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                    royalties = 0.5 * revenue;
                } else if (BF_COUNTRY_CODE.equals(operator.getCountry())) {
                    log.warn("No revenue default computation for country : {}", operator.getCountry());
                    return;
                } else {
                    log.warn("Country {} is not supported for computing revenue.", operator.getCountry());
                    return;
                }
            } else {
                log.info("Computing revenue for operator : {}", operator.getOperatorId());
                if (CI_COUNTRY_CODE.equals(operator.getCountry())) {
                    grossGamingProduct = Math.abs(misesOverallVolume) - (Math.abs(gainsOverallVolume)  + Math.abs(bonusOverallVolume));;
                    integratorRemuneration = computeParameter.getPaymentRate() * (computeParameter.getPaymentFees() * Math.abs(payinOverallVolume))
                            + computeParameter.getCashinRate() * (computeParameter.getCashinFees() * Math.abs(payoutOverallVolume));
                    revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                    royalties = computeParameter.getRoyaltyRate() * revenue;
                } else if(Boolean.TRUE.equals(operator.getNotified())){
                    grossGamingProduct = computeParameter.getPaymentRate() * (Math.abs(misesOverallVolume) - Math.abs(bonusOverallVolume))
                            - computeParameter.getCashinRate() * Math.abs(gainsOverallVolume);;
                    integratorRemuneration = computeParameter.getPaymentFees() * Math.abs(payinOverallVolume)
                            + computeParameter.getCashinFees() * Math.abs(payoutOverallVolume);
                    revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                    royalties = computeParameter.getRoyaltyRate() * revenue;
                } else {
                    grossGamingProduct = computeParameter.getPaymentRate() * Math.abs(payinOverallVolume)
                            - computeParameter.getCashinRate() * Math.abs(payoutOverallVolume);;
                    integratorRemuneration = computeParameter.getPaymentFees() * Math.abs(payinOverallVolume)
                            + computeParameter.getCashinFees() * Math.abs(payoutOverallVolume);
                    revenue = grossGamingProduct - Math.abs(integratorRemuneration);
                    royalties = computeParameter.getRoyaltyRate() * revenue;
                    grossGamingProduct = 0D;
                }
            }

            revenueEntity.setGrossGamingProduct(grossGamingProduct);
            revenueEntity.setIntegratorRemuneration(integratorRemuneration);
            revenueEntity.setRevenue(revenue);
            revenueEntity.setRoyalties(royalties);
            revenueEntity.setPayin(payinOverallVolume);
            revenueEntity.setPayout(payoutOverallVolume);
            revenueEntity.setMises(misesOverallVolume);
            revenueEntity.setGain(gainsOverallVolume);
            revenueEntity.setBonus(bonusOverallVolume);
            revenueEntity.setWithholding(withholding);

            revenueService.update(revenueEntity);
        } catch (Exception e) {
            log.error("An error occurred while computing revenue :: ", e);
        }
    }
}
