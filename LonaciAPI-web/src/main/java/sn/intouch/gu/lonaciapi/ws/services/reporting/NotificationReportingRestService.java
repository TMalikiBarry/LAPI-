package sn.intouch.gu.lonaciapi.ws.services.reporting;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.config.BadRequestException;
import sn.intouch.gu.lonaciapi.config.InvalidDateException;
import sn.intouch.gu.lonaciapi.ejb.dto.IntouchSummaryDTO;
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

@Slf4j
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
            @RequestParam(value = "start_date") Long startDateLong,
            @RequestParam(value = "end_date") Long endDateLong,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "code_service", required = false) String codeService,
            @RequestParam(value = "operateur_momo", required = false) String operateurMomo,
            @RequestParam(value = "montant", required = false) Double montant,
            @RequestParam(value = "country", defaultValue = AppConstants.CI_COUNTRY_CODE) String country

    ) {
        if (!AuthUtils.doesBookMakerHasAccessToOperator(authHeader, operator))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        validatePageSize(size);

        Date startDate = parseDate(startDateLong, "Invalid date format for start_date.");
        Date endDate = parseDate(endDateLong, "Invalid date format for end_date.");

        validateDateRange(startDate, endDate);

        PaginationResponse<List<LonaciTrx>> notifications = lonaciNotifService
                .customFindByDateBetweenAndOperateurIDAndTypeTransaction(country, startDate, endDate, operator, type, codeService, operateurMomo, montant, sortBy, sortDir, size, page);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", notifications));
    }

    @RequestMapping(value = {"/api/v1/intouch-summary", "/api/v2/intouch-summary"}, method = RequestMethod.GET,
            consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<PaginationResponse<List<IntouchSummaryDTO>>>> getIntouchSummaryPaginated(
            @RequestParam("start_date") Long startDateLong,
            @RequestParam("end_date") Long endDateLong,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "operator_id", required = false) String operatorId,
            @RequestParam(value = "momo_operator", required = false) String momoOperator
    ) {
        validatePageSize(pageSize);

        Date startDate = parseDate(startDateLong, "Invalid date format for start_date.");
        Date endDate = parseDate(endDateLong, "Invalid date format for end_date.");

        validateDateRange(startDate, endDate);

        PaginationResponse<List<IntouchSummaryDTO>> response = lonaciNotifService
                .getGroupedIntouchSummaryPaginated(startDate, endDate, page, pageSize,
                        country, operatorId, momoOperator);

        return ResponseEntity.ok(APIResponse.<PaginationResponse<List<IntouchSummaryDTO>>>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(response)
                .build());
    }

    private void validatePageSize(int size) {
        int MAX_SIZE = AppConstants.MAX_PAGE_SIZE;
        Parameter param = parameterService.getParameterByCode("PARAM_MAX_PAGE_SIZE_AUTHORIZED");

        if (param != null && param.getPrmValue() != 0) MAX_SIZE = param.getPrmValue();

        if (size > MAX_SIZE)
            throw new BadRequestException(String.format("Page size (param size) must not exceed %d !!!", MAX_SIZE));
    }

    private Date parseDate(Long dateLong, String errorMessage) {
        try {
            return new Date(dateLong);
        } catch (NumberFormatException e) {
            throw new InvalidDateException(errorMessage);
        }
    }

    private void validateDateRange(Date startDate, Date endDate) {
        long maxInterval = getDateIntervalInMillis();
        long maxIntervalInDays = maxInterval / (24 * 3600 * 1000);
        if (endDate.before(startDate)) {
            throw new InvalidDateException("End date cannot be before start date.");
        }

//        log.warn(String.format("#############  On a comme intervalle %d", maxIntervalInDays));
        if (endDate.getTime() - startDate.getTime() > maxInterval) {
            long providedInterval = (endDate.getTime() - startDate.getTime()) / (24 * 3600 * 1000); // Convertir en jours

            throw new InvalidDateException(
                    "The interval between the two dates is " + providedInterval +
                            " days, which exceeds the allowed limit of " + maxIntervalInDays + " days."
            );
        }
    }

    private Long getDateIntervalInMillis() {
        Parameter parameter = parameterService.getParameterByCode("PARAM_TWO_DATES_INTERVAL_IN_DAYS");

//        log.warn("########## " + parameter);
        if (parameter != null && parameter.getPrmValue() != 0) {
            return parameter.getPrmValue() * 24 * 3600 * 1000L;
        }

        return AppConstants.ONE_DAY_IN_MILLIS;
    }

}
