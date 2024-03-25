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
import sn.intouch.gu.lonaciapi.ws.dto.HeaderResponse;
import sn.intouch.gu.lonaciapi.ws.dto.SubHeaderResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.Date;
import java.util.Map;

@RestController
@Log4j2
public class AggregationReportingRestService {
    private final BigQueryService bigQueryService = (BigQueryService) JNDIUtils.lookUpEJB(EJBRegistry.BigQueryServiceBean);


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
        Map<String, String> sumBetweenDates = bigQueryService.getSumBetweenDatesV2(startDate, endDate, operator, type, null, null);
        Integer activeClients = bigQueryService.getActiveClients(startDate, endDate, operator, type);
        Long operationsNumber = Long.valueOf(sumBetweenDates.get("number"));
        Double overallVolume = Double.valueOf(sumBetweenDates.get("sum"));

        Double averageCart = overallVolume / operationsNumber;
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS",
                SubHeaderResponse.builder()
                        .activeClients(activeClients)
                        .averageCart(averageCart)
                        .operationsNumber(operationsNumber)
                        .overallVolume(overallVolume)
                        .build()
        ));
    }

}
