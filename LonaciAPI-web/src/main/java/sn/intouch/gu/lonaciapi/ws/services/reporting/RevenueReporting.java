package sn.intouch.gu.lonaciapi.ws.services.reporting;


import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.dto.RevenueDTO;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.services.RevenueService;
import sn.intouch.gu.lonaciapi.ejb.schedules.ComputeRevenueSchedule;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;
import sn.intouch.gu.lonaciapi.ws.dto.RevenueReformattedResponse;
import sn.intouch.gu.lonaciapi.ws.dto.RevenueResponse;
import sn.intouch.gu.lonaciapi.ws.dto.TimedResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.Date;
import java.util.List;

@RestController
@Log4j2
public class RevenueReporting {

    private final RevenueService revenueService = (RevenueService) JNDIUtils.lookUpEJB(EJBRegistry.RevenueServiceBean);

    @RequestMapping(value = {"/api/v1/aggregation/revenue", "/api/v2/aggregation/revenue"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<RevenueResponse>> revenue(
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator
    ) throws RuntimeException {
        Date startDate;
        Date endDate;
        try {
            startDate = new Date(Long.parseLong(start_date));
            endDate = new Date(Long.parseLong(end_date));
            if (endDate.before(startDate))
                throw new RuntimeException("Bad date format");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Object[]> dayRevenue = revenueService.sumByDateAndOperator(startDate, endDate, operator);
        if (dayRevenue == null || dayRevenue.isEmpty()) {
            return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", new RevenueResponse()));
        }

        log.info("Revenue :: " + new Gson().toJson(dayRevenue));
        RevenueResponse response = RevenueResponse.builder()
                .startDate(DateUtil.SIMPLE_DATE_FORMAT.format(startDate))
                .endDate(DateUtil.SIMPLE_DATE_FORMAT.format(endDate))
                .operator(operator)
                .grossGamingProduct(Double.parseDouble(getStringOr0(dayRevenue.get(0)[0])))
                .integratorRemuneration(Double.parseDouble(getStringOr0(dayRevenue.get(0)[1])))
                .revenue(Double.parseDouble(getStringOr0(dayRevenue.get(0)[2])))
                .royalties(Double.parseDouble(getStringOr0(dayRevenue.get(0)[3])))
                .payin(Double.parseDouble(getStringOr0(dayRevenue.get(0)[4])))
                .payout(Double.parseDouble(getStringOr0(dayRevenue.get(0)[5])))
                .build();
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", response));
    }

    @RequestMapping(value = {"/api/v2/aggregation/revenue-timed"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<RevenueReformattedResponse>> revenueTimed(
            @RequestParam(value = "operator", required = false) String operator
    ) throws RuntimeException {
        Date startDate, endDate;

        startDate = DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY);
        endDate = DateUtil.getEndOfDay();
        // Date yesterday = new Date(startDate.getTime() - 24 * 3600 * 1000);
        RevenueResponse dayResponse = buildRevenueResponse(operator, revenueService.sumByDateAndOperator(startDate, endDate, operator), startDate, endDate);

        startDate = DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK);
        RevenueResponse weekResponse = buildRevenueResponse(operator, revenueService.sumByDateAndOperator(startDate, endDate, operator), startDate, endDate);

        startDate = DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH);
        RevenueResponse monthResponse = buildRevenueResponse(operator, revenueService.sumByDateAndOperator(startDate, endDate, operator), startDate, endDate);

        RevenueReformattedResponse reformattedResponse = RevenueReformattedResponse.builder()
                .grossGamingProduct(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getGrossGamingProduct())
                                .week(weekResponse.getGrossGamingProduct())
                                .month(monthResponse.getGrossGamingProduct())
                                .build()
                )
                .integratorRemuneration(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getIntegratorRemuneration())
                                .week(weekResponse.getIntegratorRemuneration())
                                .month(monthResponse.getIntegratorRemuneration())
                                .build()
                )
                .revenue(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getRevenue())
                                .week(weekResponse.getRevenue())
                                .month(monthResponse.getRevenue())
                                .build()
                )
                .royalties(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getRoyalties())
                                .week(weekResponse.getRoyalties())
                                .month(monthResponse.getRoyalties())
                                .build()
                )
                .payin(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getPayin())
                                .week(weekResponse.getPayin())
                                .month(monthResponse.getPayin())
                                .build()
                )
                .payout(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getPayout())
                                .week(weekResponse.getPayout())
                                .month(monthResponse.getPayout())
                                .build()
                )
                .mises(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getMises())
                                .week(weekResponse.getMises())
                                .month(monthResponse.getMises())
                                .build()
                ).gain(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getGain())
                                .week(weekResponse.getGain())
                                .month(monthResponse.getGain())
                                .build()
                ).bonus(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getBonus())
                                .week(weekResponse.getBonus())
                                .month(monthResponse.getBonus())
                                .build()
                )
                .build();

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", reformattedResponse));
    }

    private RevenueResponse buildRevenueResponse(String operator, List<Object[]> dayRevenue, Date startDate, Date endDate) {
        if (dayRevenue == null || dayRevenue.isEmpty()) {
            return null;
        }
        return RevenueResponse.builder()
                .startDate(DateUtil.SIMPLE_DATE_FORMAT.format(startDate))
                .endDate(DateUtil.SIMPLE_DATE_FORMAT.format(endDate))
                .operator(operator)
                .grossGamingProduct(Double.parseDouble(getStringOr0(dayRevenue.get(0)[0])))
                .integratorRemuneration(Double.parseDouble(getStringOr0(dayRevenue.get(0)[1])))
                .revenue(Double.parseDouble(getStringOr0(dayRevenue.get(0)[2])))
                .royalties(Double.parseDouble(getStringOr0(dayRevenue.get(0)[3])))
                .payin(Double.parseDouble(getStringOr0(dayRevenue.get(0)[4])))
                .payout(Double.parseDouble(getStringOr0(dayRevenue.get(0)[5])))
                .build();
    }

    private String getStringOr0(Object o) {
        if (o != null)
            return o.toString();
        return "0";
    }

    @RequestMapping(value = {"/api/v1/aggregation/revenue-curve","/api/v2/aggregation/revenue-curve"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<List<RevenueDTO>>> revenueCurve(
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator
    ) throws RuntimeException {
        Date startDate;
        Date endDate;
        try {
            startDate = new Date(Long.parseLong(start_date));
            endDate = new Date(Long.parseLong(end_date));
            if (endDate.before(startDate))
                throw new RuntimeException("Bad date format");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Revenue> revenues = revenueService.curveByDateAndOperator(startDate, endDate, operator);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", Revenue.toDTOs(revenues)));
    }

    @RequestMapping(value = {"/api/v2/aggregation/launch-schedule"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<String>> test(
            @RequestParam(value = "date") String date
    ) throws RuntimeException {
        Date startDate;
        Date endDate;
        try {

            startDate = new Date(Long.parseLong(date));
            startDate.setMinutes(0);
            startDate.setSeconds(0);

            endDate = new Date(startDate.getTime() + (3600*1000));

            if (endDate.before(startDate))
                throw new RuntimeException("Bad date format");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("Start Date :: " + startDate);
        log.info("End Date :: " + endDate);
        new ComputeRevenueSchedule().compute(startDate, endDate);
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", ""));
    }

}
