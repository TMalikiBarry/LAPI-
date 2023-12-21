package sn.intouch.gu.lonaciapi.ws.services.reporting;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.ejb.bigquery.services.BigQueryService;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.models.PaginationResponse;
import sn.intouch.gu.lonaciapi.ejb.notification.services.LonaciTrxService;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parametre;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;
import sn.intouch.gu.lonaciapi.ws.constants.AppConstants;
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

    private Long getDateIntervalInMillis() {
        Parametre parametre = parameterService.getParameterByCode("PARAM_CURVE_DATES_INTERVAL_IN_DAYS");
        if (parametre != null) {
            return ((long) parametre.getPrmValue() * 24 * 3600 * 1000);
        }
        return AppConstants.THIRTY_DAYS_IN_MILLIS;
    }

}
