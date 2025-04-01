package sn.intouch.gu.lonaciapi.ws.services.reporting;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.bigquery.services.BigQueryService;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;
import sn.intouch.gu.lonaciapi.ws.dto.CategorisationResponse;
import sn.intouch.gu.lonaciapi.ws.dto.SubHeaderResponse;
import sn.intouch.gu.lonaciapi.ws.dto.TimedResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;
import sn.intouch.gu.lonaciapi.ws.utils.AuthUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
public class AggregationReportingRestServiceV2 {
    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);

    @RequestMapping(value = {"/api/v2/aggregation/curve"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<List<Map<String, String>>>> curve(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "country") String country
    ) throws RuntimeException {
        if (!AuthUtils.doesBookMakerHasAccessToOperator(authHeader, operator))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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

        List<Map<String, String>> notifications = bigQueryService.getAggregation(startDate, endDate,
                AggregationTimeEnum.MONTH, operator, type, true, null, category, country);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }

    @RequestMapping(value = {"/api/v2/aggregation/header"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<CategorisationResponse<TimedResponse<Map<String, String>>>>> header(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "country") String country
    ) throws RuntimeException {
        if (!AuthUtils.doesBookMakerHasAccessToOperator(authHeader, operator))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        CategorisationResponse<TimedResponse<Map<String, String>>> response = CategorisationResponse.<TimedResponse<Map<String, String>>>builder()
                .payin(getHeaderResponse(operator, type, "PAY_IN", country))
                .payout(getHeaderResponse(operator, type, "PAY_OUT", country))
                .bonus(getHeaderResponse(operator, type, "BONUS", country))
                .mises(getHeaderResponse(operator, type, "MISES", country))
                .gain(getHeaderResponse(operator, type, "GAIN", country))
                .build();
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", response));
    }

    private TimedResponse<Map<String, String>> getHeaderResponse(String operator, String type, String category, String country) {
        return TimedResponse.<Map<String, String>>builder()
                .day(bigQueryService.getSumBetweenDatesV2(DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY),
                        DateUtil.getEndOfDay(), operator, type, null, category, country))
                .week(bigQueryService.getSumBetweenDatesV2(DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK),
                        DateUtil.getEndOfDay(), operator, type, null, category, country))
                .month(bigQueryService.getSumBetweenDatesV2(DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH),
                        DateUtil.getEndOfDay(), operator, type, null, category, country))
                .build();
    }

    @RequestMapping(value = {"/api/v2/aggregation/sub-header"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<CategorisationResponse<SubHeaderResponse>>> subHeaderV2(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "country") String country
    ) throws RuntimeException {
        if (!AuthUtils.doesBookMakerHasAccessToOperator(authHeader, operator))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
        Integer activeClients = bigQueryService.getActiveClients(startDate, endDate, operator, null, country);
        Map<String, String> values = bigQueryService.getSumBetweenDatesAllCategories(startDate, endDate, operator,
                null, null, country);
        values.put("activeClients", activeClients + "");
        log.info("Flatten DATA :: " + values);
        CategorisationResponse<SubHeaderResponse> response = CategorisationResponse.<SubHeaderResponse>builder()
                .payin(getSubHeaderResponse(values, "payin"))
                .payout(getSubHeaderResponse(values, "payout"))
                .mises(getSubHeaderResponse(values, "mises"))
                .gain(getSubHeaderResponse(values, "gain"))
                .bonus(getSubHeaderResponse(values, "bonus"))
                .build();
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                response
        ));
    }

    private SubHeaderResponse getSubHeaderResponse(Map<String, String> values, String type) {

        Long operationsNumber = values.get(type + "Count") != null ? Long.parseLong(values.get(type + "Count")) : 0L;
        Double overallVolume = values.get(type) != null ? Double.parseDouble(values.get(type)) : 0D;
        Integer activeClients = values.get("activeClients") != null ? Integer.parseInt(values.get("activeClients")) : 0;
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
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "country") String country
    ) throws RuntimeException {
        if (!AuthUtils.doesBookMakerHasAccessToOperator(authHeader, operator))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        CategorisationResponse<TimedResponse<List<Map<String, String>>>> response = CategorisationResponse.<TimedResponse<List<Map<String, String>>>>builder()
                .payin(getTrendResponse(operator, type, "PAY_IN", country))
                .payout(getTrendResponse(operator, type, "PAY_OUT", country))
                .bonus(getTrendResponse(operator, type, "BONUS", country))
                .mises(getTrendResponse(operator, type, "MISES", country))
                .gain(getTrendResponse(operator, type, "GAIN", country))
                .build();
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                response
        ));
    }

    private TimedResponse<List<Map<String, String>>> getTrendResponse(String operator, String type, String category, String country) {
        return TimedResponse.<List<Map<String, String>>>builder()
                .day(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY),
                        DateUtil.getEndOfDay(), AggregationTimeEnum.DAY, operator, type, false, null, category, country))
                .week(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK),
                        DateUtil.getEndOfDay(), AggregationTimeEnum.WEEK, operator, type, false, null, category, country))
                .month(bigQueryService.getAggregation(DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH),
                        DateUtil.getEndOfDay(), AggregationTimeEnum.MONTH, operator, type, false, null, category, country))
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
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "country") String country
    ) throws RuntimeException {
        if (!AuthUtils.doesBookMakerHasAccessToOperator(authHeader, operator))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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

        List<Map<String, String>> notifications = bigQueryService.getAggregation(startDate, endDate, timeEnum, operator,
                type, true, null, category, country);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }
}
