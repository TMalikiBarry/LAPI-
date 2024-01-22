package sn.intouch.gu.lonaciapi.ws.services.reporting;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.bigquery.services.BigQueryService;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;
import sn.intouch.gu.lonaciapi.ws.dto.HeaderResponse;
import sn.intouch.gu.lonaciapi.ws.dto.SubHeaderResponse;
import sn.intouch.gu.lonaciapi.ws.dto.TrendResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class AggregationReportingRestService {
    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);


    @RequestMapping(value = {"/api/v1/aggregation/curve"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<List<Map<String, String>>>> curve(
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "grain", required = false) String grain
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

        List<Map<String, String>> notifications = bigQueryService.getAggregation(startDate, endDate, AggregationTimeEnum.MONTH , operator, type);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }
    @RequestMapping(value = {"/api/v1/aggregation/header"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<HeaderResponse>> header(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type
    ) throws RuntimeException {

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                HeaderResponse.builder()
                        .day(bigQueryService.getSumBetweenDates(DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY), DateUtil.getEndOfDay(), operator, type))
                        .week(bigQueryService.getSumBetweenDates(DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK), DateUtil.getEndOfDay(), operator, type))
                        .month(bigQueryService.getSumBetweenDates(DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH), DateUtil.getEndOfDay(), operator, type))
                        .build()
                ));
    }

    @RequestMapping(value = {"/api/v1/aggregation/sub-header"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<SubHeaderResponse>> subHeader(
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type
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
        List<Map<String, String>> sumsClients = bigQueryService.getSumClientsBetweenDates(startDate, endDate, operator, type);
        Integer activeClients = sumsClients.size();
        AtomicReference<Integer> operationsNumber = new AtomicReference<>(0);
        AtomicReference<Double> overallVolume = new AtomicReference<>(0D);
        if (!sumsClients.isEmpty()) {
            sumsClients.forEach(map -> {
                operationsNumber.updateAndGet(v -> v + Integer.parseInt(map.get("number")));
                overallVolume.updateAndGet(v -> v + Integer.parseInt(map.get("sum")));
            });
        }
        Double averageCart = overallVolume.get() / activeClients;
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                SubHeaderResponse.builder()
                        .activeClients(sumsClients.size())
                        .averageCart(averageCart)
                        .operationsNumber(operationsNumber.get())
                        .overallVolume(overallVolume.get())
                        .build()
                ));
    }
    @RequestMapping(value = {"/api/v1/aggregation/trend"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<TrendResponse>> trend(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type
    ) throws RuntimeException {

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                TrendResponse.builder()
                        .day(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY), DateUtil.getEndOfDay(), AggregationTimeEnum.DAY , operator, type))
                        .week(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK), DateUtil.getEndOfDay(), AggregationTimeEnum.WEEK , operator, type))
                        .month(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH), DateUtil.getEndOfDay(), AggregationTimeEnum.MONTH , operator, type))
                        .build()
                ));
    }

}
