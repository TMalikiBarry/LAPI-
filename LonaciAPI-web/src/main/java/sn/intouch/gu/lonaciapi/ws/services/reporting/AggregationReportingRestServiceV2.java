package sn.intouch.gu.lonaciapi.ws.services.reporting;

import lombok.extern.log4j.Log4j2;
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
import sn.intouch.gu.lonaciapi.ws.dto.CategorisationResponse;
import sn.intouch.gu.lonaciapi.ws.dto.SubHeaderResponse;
import sn.intouch.gu.lonaciapi.ws.dto.TimedResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
public class AggregationReportingRestServiceV2 {
    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);

    @RequestMapping(value = {"/api/v2/aggregation/curve"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<List<Map<String, String>>>> curve(
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category
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

        List<Map<String, String>> notifications = bigQueryService.getAggregation(startDate, endDate, AggregationTimeEnum.MONTH, operator, type, true, null, category);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }

    @RequestMapping(value = {"/api/v2/aggregation/header"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<CategorisationResponse<TimedResponse<Map<String, String>>>>> header(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type
    ) throws RuntimeException {
        
        CategorisationResponse<TimedResponse<Map<String, String>>> response = CategorisationResponse.<TimedResponse<Map<String, String>>>builder()
                .payin(getHeaderResponse(operator, type, "PAY_IN"))
                .payout(getHeaderResponse(operator, type, "PAY_OUT"))
                .bonus(getHeaderResponse(operator, type, "BONUS"))
                .mises(getHeaderResponse(operator, type, "MISES"))
                .gain(getHeaderResponse(operator, type, "GAIN"))
                .build();
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", response));
    }

    private TimedResponse<Map<String, String>> getHeaderResponse(String operator, String type, String category) {
        return TimedResponse.<Map<String, String>>builder()
                .day(bigQueryService.getSumBetweenDatesV2(DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY), DateUtil.getEndOfDay(), operator, type, null, category))
                .week(bigQueryService.getSumBetweenDatesV2(DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK), DateUtil.getEndOfDay(), operator, type, null, category))
                .month(bigQueryService.getSumBetweenDatesV2(DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH), DateUtil.getEndOfDay(), operator, type, null, category))
                .build();
    }

    @RequestMapping(value = {"/api/v2/aggregation/sub-header"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<CategorisationResponse<SubHeaderResponse>>> subHeader(
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
        CategorisationResponse<SubHeaderResponse> response = CategorisationResponse.<SubHeaderResponse>builder()
                .payin(getSubHeaderResponse(operator, type, startDate, endDate, "PAY_IN"))
                .payout(getSubHeaderResponse(operator, type, startDate, endDate, "PAY_OUT"))
                .bonus(getSubHeaderResponse(operator, type, startDate, endDate, "BONUS"))
                .mises(getSubHeaderResponse(operator, type, startDate, endDate, "MISES"))
                .gain(getSubHeaderResponse(operator, type, startDate, endDate, "GAIN"))
                .build();
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                response
        ));
    }

    private SubHeaderResponse getSubHeaderResponse(String operator, String type, Date startDate, Date endDate, String category) {
        Map<String, String> sumBetweenDates = bigQueryService.getSumBetweenDatesV2(startDate, endDate, operator, type, null, category);
        Integer activeClients = bigQueryService.getActiveClients(startDate, endDate, operator, type);
        Long operationsNumber = sumBetweenDates.get("number") != null ? Long.parseLong(sumBetweenDates.get("number")) : 0L;
        Double overallVolume = sumBetweenDates.get("sum") != null ? Double.parseDouble(sumBetweenDates.get("sum")) : 0D;
        Double averageCart = overallVolume / operationsNumber;
        return SubHeaderResponse.builder()
                .activeClients(activeClients)
                .averageCart(averageCart)
                .operationsNumber(operationsNumber)
                .overallVolume(overallVolume)
                .build();
    }

    @RequestMapping(value = {"/api/v2/aggregation/trend"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<CategorisationResponse<TimedResponse<List<Map<String, String>>>>>> trend(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type
    ) throws RuntimeException {
        CategorisationResponse<TimedResponse<List<Map<String, String>>>> response = CategorisationResponse.<TimedResponse<List<Map<String, String>>>>builder()
                .payin(getTrendResponse(operator, type, "PAY_IN"))
                .payout(getTrendResponse(operator, type, "PAY_OUT"))
                .bonus(getTrendResponse(operator, type, "BONUS"))
                .mises(getTrendResponse(operator, type, "MISES"))
                .gain(getTrendResponse(operator, type, "GAIN"))
                .build();
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                response
        ));
    }

    private TimedResponse<List<Map<String, String>>> getTrendResponse(String operator, String type, String category) {
        return TimedResponse.<List<Map<String, String>>>builder()
                .day(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY), DateUtil.getEndOfDay(), AggregationTimeEnum.DAY, operator, type, false, null, category))
                .week(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK), DateUtil.getEndOfDay(), AggregationTimeEnum.WEEK, operator, type, false, null, category))
                .month(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH), DateUtil.getEndOfDay(), AggregationTimeEnum.MONTH, operator, type, false, null, category))
                .build();
    }

    /**
     * REPORT ENDPOINT
     *
     * @param start_date
     * @param end_date
     * @param operator
     * @param type
     * @return
     * @throws RuntimeException
     */
    @RequestMapping(value = {"/api/v2/aggregation/report"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<List<Map<String, String>>>> report(
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category
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
        long dateDiff = endDate.getTime() - startDate.getTime();

        AggregationTimeEnum timeEnum;

        if (dateDiff <= DateUtil.DAY_INTERVAL_IN_MILLIS)
            timeEnum = AggregationTimeEnum.DAY;
        else if (dateDiff <= DateUtil.MONTH_INTERVAL_IN_MILLIS)
            timeEnum = AggregationTimeEnum.MONTH;
        else
            timeEnum = AggregationTimeEnum.YEAR;

        List<Map<String, String>> notifications = bigQueryService.getAggregation(startDate, endDate, timeEnum, operator, type, true, null, category);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }
}
