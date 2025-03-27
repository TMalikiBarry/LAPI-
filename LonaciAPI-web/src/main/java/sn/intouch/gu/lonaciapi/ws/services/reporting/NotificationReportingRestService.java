package sn.intouch.gu.lonaciapi.ws.services.reporting;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.models.PaginationResponse;
import sn.intouch.gu.lonaciapi.ejb.notification.services.LonaciTrxService;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;
import sn.intouch.gu.lonaciapi.ws.constants.AppConstants;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;
import sn.intouch.gu.lonaciapi.ws.utils.AuthUtils;

import java.util.Date;
import java.util.List;

@RestController
public class NotificationReportingRestService {
    private final LonaciTrxService lonaciNotifService = (LonaciTrxService) JNDIUtils.lookUpEJB(EJBRegistry.LonaciTrxServiceBean);
    private final ParameterService parameterService = (ParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ParameterServiceBean);

    @RequestMapping(value = {"/api/v1/filter", "/api/v2/filter"}, method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<PaginationResponse<List<LonaciTrx>>>> findAllWithPagination(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
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
            if (endDate.before(startDate) || (endDate.getTime() - startDate.getTime() > getDateIntervalInMillis()))
                throw new RuntimeException("Bad date format");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        PaginationResponse<List<LonaciTrx>> notifications = lonaciNotifService.customFindByDateBetweenAndOperateurIDAndTypeTransaction(country, startDate, endDate, operator, type, sortBy, sortDir, size, page);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }

    private Long getDateIntervalInMillis() {
        Parameter parameter = parameterService.getParameterByCode("PARAM_TWO_DATES_INTERVAL_IN_DAYS");
        if (parameter != null) {
            return (long) (parameter.getPrmValue() * 24 * 3600 * 1000);
        }
        return AppConstants.ONE_DAY_IN_MILLIS;
    }

}
