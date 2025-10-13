package sn.intouch.gu.lonaciapi.ws.services.reporting;


import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.dto.RevenueDTO;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.services.LonaciTrxService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.RevenueService;
import sn.intouch.gu.lonaciapi.ejb.schedules.ComputeRevenueSchedule;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;
import sn.intouch.gu.lonaciapi.ws.constants.AppConstants;
import sn.intouch.gu.lonaciapi.ws.dto.RevenueReformattedResponse;
import sn.intouch.gu.lonaciapi.ws.dto.RevenueResponse;
import sn.intouch.gu.lonaciapi.ws.dto.TimedResponse;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;
import sn.intouch.gu.lonaciapi.ws.utils.AuthUtils;

import java.util.*;

@RestController
@Log4j2
public class RevenueReporting {

    private final RevenueService revenueService = (RevenueService) JNDIUtils.lookUpEJB(EJBRegistry.RevenueServiceBean);
    private final OperatorService operatorService = (OperatorService) JNDIUtils.lookUpEJB(EJBRegistry.OperatorServiceBean);
    private final LonaciTrxService lonaciTrxService = (LonaciTrxService) JNDIUtils.lookUpEJB(EJBRegistry.LonaciTrxServiceBean);

    @RequestMapping(value = {"/api/v1/aggregation/revenue", "/api/v2/aggregation/revenue"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<RevenueResponse>> revenue(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
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

        List<Object[]> dayRevenue = revenueService.sumByDateAndOperator(startDate, endDate, operator, country);
        if (dayRevenue == null || dayRevenue.isEmpty()) {
            return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", new RevenueResponse()));
        }

        log.info("Revenue :: " + new Gson().toJson(dayRevenue));

        Object[] row = dayRevenue.get(0);

        double grosJeuxProduct = Double.parseDouble(getStringOr0(row[0]));
        double integrRemun = Double.parseDouble(getStringOr0(row[1]));
        double revenuTotal = Double.parseDouble(getStringOr0(row[2]));
        double royalties = Double.parseDouble(getStringOr0(row[3]));
        double payin = Double.parseDouble(getStringOr0(row[4]));
        double payout = Double.parseDouble(getStringOr0(row[5]));
        double mises = Double.parseDouble(getStringOr0(row[6]));
        double gain = Double.parseDouble(getStringOr0(row[7]));
        double bonus = Double.parseDouble(getStringOr0(row[8]));
        double withholding = Double.parseDouble(getStringOr0(row[9]));

        // 5) Calcul des trois nouveaux champs, mais **seulement** si country = "BF".
        //    Sinon, on laisse 0.
        double grossRevenue = 0D;
        double gamblingTax = 0D;


        if (AppConstants.BF_COUNTRY_CODE.equalsIgnoreCase(country.trim())) {
            // 5a) grossRevenue = mises –  bonus
            grossRevenue = Math.abs(mises) - Math.abs(bonus);

            // 5b) gamblingTax = 5 % × grossRevenue
            gamblingTax = grossRevenue * 0.05;


        }

        // 6) Construction du DTO final en incluant les trois nouveaux champs
        RevenueResponse response = RevenueResponse.builder()
                .startDate(DateUtil.SIMPLE_DATE_FORMAT.format(startDate))
                .endDate(DateUtil.SIMPLE_DATE_FORMAT.format(endDate))
                .operator(operator)
                .grossGamingProduct(grosJeuxProduct)
                .integratorRemuneration(integrRemun)
                .revenue(revenuTotal)
                .royalties(royalties)
                .payin(payin)
                .payout(payout)
                .mises(mises)
                .gain(gain)
                .bonus(bonus)
                .grossRevenue(grossRevenue)
                .gamblingTax(gamblingTax)
                .withholding(withholding)
                .build();

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", response));

    }

    @RequestMapping(value = {"/api/v2/aggregation/revenue-timed"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<RevenueReformattedResponse>> revenueTimed(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "country") String country
    ) throws RuntimeException {
        if (!AuthUtils.doesBookMakerHasAccessToOperator(authHeader, operator)) {
            log.warn("Accès refusé : l’utilisateur n’a pas le rôle nécessaire (operator = {})", operator);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        log.info("Appel API revenueTimed – operator='{}', country='{}'", operator, country);

        // 2) Calcul des dates pour JOURS
        Date startDateDay = DateUtil.getStartDateFromDateString(AggregationTimeEnum.DAY);
        Date endDate = DateUtil.getEndOfDay(); // même fin pour toutes les fenêtres
        log.warn("  ####### revenueTimed  Fenêtre DAY : start = {}, end = {}", startDateDay, endDate);

        RevenueResponse dayResponse = buildRevenueResponse(
                operator,
                revenueService.sumByDateAndOperator(startDateDay, endDate, operator, country),
                startDateDay,
                endDate,
                country
        );
        log.warn("  ####### revenueTimed  Résultat DAY RevenueResponse = {}", dayResponse);


        // 3) Fenêtre WEEK
        Date startDateWeek = DateUtil.getStartDateFromDateString(AggregationTimeEnum.WEEK);
        log.warn("  ####### revenueTimed  Fenêtre WEEK : start = {}, end = {}", startDateWeek, endDate);
        RevenueResponse weekResponse = buildRevenueResponse(
                operator,
                revenueService.sumByDateAndOperator(startDateWeek, endDate, operator, country),
                startDateWeek,
                endDate,
                country
        );
        log.warn("  ####### revenueTimed  Résultat WEEK RevenueResponse = {}", weekResponse);

        // 4) Fenêtre MONTH
        Date startDateMonth = DateUtil.getStartDateFromDateString(AggregationTimeEnum.MONTH);
        log.warn("  ####### revenueTimed  Fenêtre MONTH : start = {}, end = {}", startDateMonth, endDate);
        RevenueResponse monthResponse = buildRevenueResponse(
                operator,
                revenueService.sumByDateAndOperator(startDateMonth, endDate, operator, country),
                startDateMonth,
                endDate,
                country
        );
        log.warn("  ####### revenueTimed  Résultat MONTH RevenueResponse = {}", monthResponse);



        RevenueReformattedResponse reformattedResponse = RevenueReformattedResponse.builder()
                .grossRevenue(
                        TimedResponse.<Double>builder()
                                .day(computeGross(dayResponse, country))
                                .week(computeGross(weekResponse, country))
                                .month(computeGross(monthResponse, country))
                                .build()
                )
                .gamblingTax(
                        TimedResponse.<Double>builder()
                                .day(computeTax(dayResponse, country))
                                .week(computeTax(weekResponse, country))
                                .month(computeTax(monthResponse, country))
                                .build()
                )
                .withholding(
                        TimedResponse.<Double>builder()
                                .day(dayResponse.getWithholding())
                                .week(weekResponse.getWithholding())
                                .month(monthResponse.getWithholding())
                                .build()
                )
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

    private RevenueResponse buildRevenueResponse(String operator, List<Object[]> dataRevenue,
                                                 Date startDate, Date endDate, String country) {
        if (dataRevenue == null || dataRevenue.isEmpty()) {
            return null;
        }

        Object[] row = dataRevenue.get(0);

        return RevenueResponse.builder()
                .startDate(DateUtil.SIMPLE_DATE_FORMAT.format(startDate))
                .endDate(DateUtil.SIMPLE_DATE_FORMAT.format(endDate))
                .operator(operator)

                .grossGamingProduct(Double.parseDouble(getStringOr0(row[0])))

                // On laisse le reste tel quel :
                .integratorRemuneration(Double.parseDouble(getStringOr0(row[1])))
                .revenue(Double.parseDouble(getStringOr0(row[2])))
                .royalties(Double.parseDouble(getStringOr0(row[3])))
                .payin(Double.parseDouble(getStringOr0(row[4])))
                .payout(Double.parseDouble(getStringOr0(row[5])))
                .mises(Double.parseDouble(getStringOr0(row[6])))
                .gain(Double.parseDouble(getStringOr0(row[7])))
                .bonus(Double.parseDouble(getStringOr0(row[8])))
                .withholding(Double.parseDouble(getStringOr0(row[9])))
                .build();
    }


    private Double computeGross(RevenueResponse r, String country) {
        if (r == null) {
            return 0D;
        }
        if (AppConstants.BF_COUNTRY_CODE.equalsIgnoreCase(country)) {

            double mises = (r.getMises() != null ? r.getMises() : 0D);
            double bonus = (r.getBonus() != null ? r.getBonus() : 0D);
            return Math.abs(mises) - Math.abs(bonus);
        }

        return (r.getGrossGamingProduct() != null ? r.getGrossGamingProduct() : 0D);
    }


    private Double computeTax(RevenueResponse r, String country) {
        if (r == null) {
            return 0D;
        }
        if (AppConstants.BF_COUNTRY_CODE.equalsIgnoreCase(country)) {

            double grossRev = computeGross(r, country);
            return grossRev * 0.05;
        }

        return 0D;
    }
    

    private String getStringOr0(Object o) {
        if (o != null)
            return o.toString();
        return "0";
    }

    @RequestMapping(value = {"/api/v1/aggregation/revenue-curve","/api/v2/aggregation/revenue-curve"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<List<RevenueDTO>>> revenueCurve(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(value = "start_date") String start_date,
            @RequestParam(value = "end_date") String end_date,
            @RequestParam(value = "operator", required = false) String operator,
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
        List<Revenue> revenues = revenueService.curveByDateAndOperator(startDate, endDate, operator, country);

        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", Revenue.toDTOs(revenues)));
    }


    @RequestMapping(value = {"/api/v2/aggregation/launch-schedule"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse<String>> test(
            @RequestParam(value = "date") String date,
            @RequestParam(value = "operateur") String operateur
    ) throws RuntimeException {
        Date startDate;
        Date endDate;
        try {
            startDate = new Date(Long.parseLong(date));

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = new Date(cal.getTimeInMillis());
            cal.add(Calendar.HOUR, 1);
            endDate = new Date(cal.getTimeInMillis());

            if (endDate.before(startDate))
                throw new RuntimeException("Bad date format");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("Start Date :: {}", startDate);
        log.info("End Date :: {}", endDate);
        if (operateur != null) {
            Operator operatorEntity = operatorService.findByOperatorID(operateur);
            if (operatorEntity == null)
                return ResponseEntity.ok(new APIResponse<>(404, "Operator not found", ""));
            Map<String , Operator> operatorMap = new HashMap<>();
            operatorMap.put(operatorEntity.getOperatorId(), operatorEntity);
            new ComputeRevenueSchedule().computeForOperator(startDate, endDate, operatorMap);
        }else
            new ComputeRevenueSchedule().compute(startDate, endDate);
        return ResponseEntity.ok(new APIResponse<>(200, "SUCCESS", ""));
    }

}
