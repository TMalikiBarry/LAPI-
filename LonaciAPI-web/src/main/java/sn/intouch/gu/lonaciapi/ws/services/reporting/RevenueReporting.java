package sn.intouch.gu.lonaciapi.ws.services.reporting;


import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.services.RevenueService;
import sn.intouch.gu.lonaciapi.ejb.schedules.ComputeRevenueSchedule;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;
import sn.intouch.gu.lonaciapi.ws.dto.RevenueResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.Date;
import java.util.List;

@RestController
@Log4j2
public class RevenueReporting {

    private final RevenueService revenueService = (RevenueService) JNDIUtils.lookUpEJB(EJBRegistry.RevenueServiceBean);

    @RequestMapping(value = {"/api/v1/revenue"}, method = RequestMethod.GET, produces = "application/json")
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
        List<Object[]> totalRevenue = revenueService.sumByDateAndOperator(startDate, endDate, operator);
        if (totalRevenue == null || totalRevenue.isEmpty()) {
            return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", new RevenueResponse()));
        }

        RevenueResponse response = RevenueResponse.builder()
                .startDate(DateUtil.SIMPLE_DATE_FORMAT.format(startDate))
                .endDate(DateUtil.SIMPLE_DATE_FORMAT.format(endDate))
                .operator(operator)
                .grossGamingProduct(Double.parseDouble(totalRevenue.get(0)[0].toString()))
                .integratorRemuneration(Double.parseDouble(totalRevenue.get(0)[1].toString()))
                .revenue(Double.parseDouble(totalRevenue.get(0)[2].toString()))
                .royalties(Double.parseDouble(totalRevenue.get(0)[3].toString()))
                .build();

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", response));
    }

    @RequestMapping(value = {"/api/v1/revenue-curve"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<Iterable<Revenue>>> revenueCurve(
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
        Iterable<Revenue> revenues = revenueService.findByDateAndOperator(startDate, endDate, operator);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", revenues));
    }

    @RequestMapping(value = {"/api/v1/aggregation/launch-schedule"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<String>> test(
            @RequestParam(value = "date") String date
    ) throws RuntimeException {
        Date startDate;
        Date endDate;
        try {

            startDate = new Date(Long.parseLong(date));
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);

            endDate = new Date(Long.parseLong(date));
            endDate.setHours(23);
            endDate.setMinutes(59);
            endDate.setSeconds(59);
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
