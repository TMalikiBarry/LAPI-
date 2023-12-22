package sn.intouch.gu.lonaciapi.ws.services.reporting;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.HeaderTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.bigquery.services.BigQueryService;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;
import sn.intouch.gu.lonaciapi.ws.constants.AppConstants;
import sn.intouch.gu.lonaciapi.ws.dto.HeaderResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class AggregationReportingRestService {
    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);
    private final ParameterService parameterService = (ParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ParameterServiceBean);


    @RequestMapping(value = {"/api/v1/aggregation/curve"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<List<Map<String, String>>>> findAllWithPagination(
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
            if (endDate.before(startDate) || (endDate.getTime() - startDate.getTime() > getDateIntervalInMillis()))
                throw new RuntimeException("Bad date format");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Map<String, String>> notifications = bigQueryService.getAggregation(startDate, endDate, operator, type);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }
    @RequestMapping(value = {"/api/v1/aggregation/header"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<HeaderResponse>> findAllWithPagination(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type
    ) throws RuntimeException {

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                HeaderResponse.builder()
                        .day(bigQueryService.getHeader(new Date(), operator, type, HeaderTimeEnum.DAY))
                        .week(bigQueryService.getHeader(new Date(), operator, type, HeaderTimeEnum.WEEK))
                        .month(bigQueryService.getHeader(new Date(), operator, type, HeaderTimeEnum.MONTH))
                        .build()
                ));
    }

    private Long getDateIntervalInMillis() {
        Parameter parameter = parameterService.getParameterByCode("PARAM_CURVE_DATES_INTERVAL_IN_DAYS");
        if (parameter != null) {
            return ((long) parameter.getPrmValue() * 24 * 3600 * 1000);
        }
        return AppConstants.THIRTY_DAYS_IN_MILLIS;
    }

}
