package sn.intouch.gu.lonaciapi.ejb.schedules;

import lombok.extern.log4j.Log4j2;
import sn.intouch.gu.lonaciapi.ejb.bigquery.services.BigQueryService;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.RevenueService;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timer;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Singleton
@Log4j2
public class ComputeRevenueSchedule {

    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);
    private final RevenueService revenueService = (RevenueService) JNDIUtils
            .lookUpEJB(EJBRegistry.RevenueServiceBean);

    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);

    // @Schedule(dayOfWeek = "*", hour = "*", minute = "*/2", second = "59", persistent = false)
    @Schedule(dayOfWeek = "*", hour = "*/1", minute = "15", persistent = true)
    public void launch(Timer timer) {

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
        log.info("Running JOB for computing revenue at : START DATE " + startDate + " AND END DATE : " + endDate);
        Iterable<Operator> operators = operatorService.getAll();
        for (Operator operator : operators) {
            computeForOperator(startDate, endDate, operator.getOperatorId());
        }
    }

    public void computeForOperator(Date startDate, Date endDate, String operator) {

        try {
            Revenue revenueEntity = Revenue.builder()
                    .date(startDate)
                    .endDate(endDate)
                    .operator(operator)
                    .build();
            revenueEntity = revenueService.update(revenueEntity);
            Map<String, String> values = bigQueryService.getSumBetweenDatesAllCategories(startDate, endDate, operator, null, Boolean.TRUE);

            Double misesOverallVolume = Double.valueOf(values.get("mises"));
            Double gainsOverallVolume = Double.valueOf(values.get("gains"));
            Double bonusOverallVolume = Double.valueOf(values.get("bonus"));
            Double payinOverallVolume = Double.valueOf(values.get("payin"));
            Double payoutOverallVolume = Double.valueOf(values.get("payout"));

            Double grossGamingProduct = misesOverallVolume - Math.abs(gainsOverallVolume)  + Math.abs(bonusOverallVolume);
            Double integratorRemuneration = 0.04 * payinOverallVolume - 0.02 * payoutOverallVolume;
            Double revenue = grossGamingProduct - integratorRemuneration;
            Double royalties = 0.5 * revenue;

            revenueEntity.setGrossGamingProduct(grossGamingProduct);
            revenueEntity.setIntegratorRemuneration(integratorRemuneration);
            revenueEntity.setRevenue(revenue);
            revenueEntity.setRoyalties(royalties);
            revenueEntity.setPayin(payinOverallVolume);
            revenueEntity.setPayout(payoutOverallVolume);
            revenueEntity.setMises(misesOverallVolume);
            revenueEntity.setGain(gainsOverallVolume);
            revenueEntity.setBonus(bonusOverallVolume);

            revenueService.update(revenueEntity);
        } catch (Exception e) {
            log.error("An error occurred while computing revenue :: ", e);
        }
    }
}
