package sn.intouch.gu.lonaciapi.ws.services.crud;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.config.BadRequestException;
import sn.intouch.gu.lonaciapi.ejb.dto.SaveAllResponse;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;
import sn.intouch.gu.lonaciapi.ejb.notification.services.CodeServiceMOMOService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = {"/api/v2/service-momo", "/api/v1/service-momo"})
public class CodeServiceMOMORestService {

    private final CodeServiceMOMOService codeServiceMOMOService = (CodeServiceMOMOService) JNDIUtils.
            lookUpEJB(EJBRegistry.CodeServiceMOMOServiceBean);

    @GetMapping(value = "/filter_paged", produces = "application/json")
    public ResponseEntity<APIResponse<Page<CodeServiceMOMO>>> searchPaged(
            @RequestParam(value = "code_momo", required = false) String codeMomo,
            @RequestParam(value = "operateur_service_momo", required = false) String operateurServiceMomo,
            @RequestParam(value = "service_nom", required = false) String serviceNom,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "start_date", required = false) String startDateStr,
            @RequestParam(value = "end_date", required = false) String endDateStr,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Date startDate = parseDate(startDateStr, true);
        Date endDate = parseDate(endDateStr, false);

        Page<CodeServiceMOMO> results = codeServiceMOMOService.findByOptionalParamsPaged(
                codeMomo, operateurServiceMomo, serviceNom, type, startDate, endDate, pageable);

        return ResponseEntity.ok(APIResponse.<Page<CodeServiceMOMO>>builder()
                .code(200)
                .reason("SUCCESS")
                .data(results)
                .build());
    }

    @GetMapping(value = "/filter", produces = "application/json")
    public ResponseEntity<APIResponse<List<CodeServiceMOMO>>> search(
            @RequestParam(value = "code_momo", required = false) String codeMomo,
            @RequestParam(value = "operateur_service_momo", required = false) String operateurServiceMomo,
            @RequestParam(value = "service_nom", required = false) String serviceNom,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "start_date", required = false) String startDateStr,
            @RequestParam(value = "end_date", required = false) String endDateStr) {

        Date startDate = parseDate(startDateStr, true);
        Date endDate = parseDate(endDateStr, false);

        List<CodeServiceMOMO> results = codeServiceMOMOService.findByOptionalParams(
                codeMomo, operateurServiceMomo, serviceNom, type, startDate, endDate);

        return ResponseEntity.ok(APIResponse.<List<CodeServiceMOMO>>builder()
                .code(200)
                .reason("SUCCESS")
                .data(results)
                .build());
    }


    // Récupérer une entrée par ID
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> getCodeServiceById(@PathVariable Long id) {
        CodeServiceMOMO entity = codeServiceMOMOService.findById(id);
        return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(entity)
                .build());
    }

    // Récupérer toutes les entrées
    @GetMapping(produces = "application/json")
    public ResponseEntity<APIResponse<List<CodeServiceMOMO>>> getAllCodeServices() {
        List<CodeServiceMOMO> list = codeServiceMOMOService.findAll();
        return ResponseEntity.ok(APIResponse.<List<CodeServiceMOMO>>builder()
                .code(200)
                .reason("SUCCESS")
                .data(list)
                .build());
    }

    // Création d'une nouvelle entrée
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> createCodeService(@RequestBody CodeServiceMOMO codeServiceMOMO) {

        CodeServiceMOMO saved = codeServiceMOMOService.save(codeServiceMOMO);
        return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(saved)
                .build());
    }

    // Création par lot avec la méthode saveAll
    @PostMapping(value = "/batch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<SaveAllResponse>> createCodeServices(@RequestBody List<CodeServiceMOMO> codeServiceMOMOList) {
        SaveAllResponse response = codeServiceMOMOService.saveAll(codeServiceMOMOList);
        return ResponseEntity.ok(APIResponse.<SaveAllResponse>builder()
                .code(200)
                .reason("SUCCESS")
                .data(response)
                .build());
    }

    // Mise à jour par ID (PATCH)
    @PatchMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> updateCodeService(@RequestBody CodeServiceMOMO codeServiceMOMO) {
        CodeServiceMOMO updated = codeServiceMOMOService.update(codeServiceMOMO);
        return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(updated)
                .build());
    }

    // Mise à jour par code de service (PATCH)
    @PatchMapping(value = "/by-code/{code}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> updateCodeServiceByCode(@PathVariable("code") String code,
                                                                                @RequestBody CodeServiceMOMO codeServiceMOMO) {
        CodeServiceMOMO updated = codeServiceMOMOService.updateByCodeService(codeServiceMOMO, code);
        return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(updated)
                .build());
    }

    // Recherche par codeServiceMomo et operateurServiceMomo
    @GetMapping(value = "/search", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> searchByCodeAndOperator(
            @RequestParam("codeServiceMomo") String codeServiceMomo,
            @RequestParam("operateurServiceMomo") String operateurServiceMomo) {
        CodeServiceMOMO result = codeServiceMOMOService
                .findByCodeMomoAndOperateurServiceMomo(codeServiceMomo, operateurServiceMomo);

        return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(result)
                .build());
    }

    // Vérifier l'existence d'une association
    @GetMapping(value = "/exists", produces = "application/json")
    public ResponseEntity<APIResponse<Boolean>> existsByCodeAndOperator(
            @RequestParam("codeServiceMomo") String codeServiceMomo,
            @RequestParam("operateurServiceMomo") String operateurServiceMomo) {
        boolean exists = codeServiceMOMOService.existsByCodeMomoAndOperateurServiceMomo(codeServiceMomo, operateurServiceMomo);
        return ResponseEntity.ok(APIResponse.<Boolean>builder()
                .code(200)
                .reason("SUCCESS")
                .data(exists)
                .build());
    }

    @GetMapping("/operateurs")
    public ResponseEntity<APIResponse<List<String>>> getDistinctOperateurs() {
        List<String> operateurs = codeServiceMOMOService.findDistinctOperateurs();

        APIResponse<List<String>> response = APIResponse.<List<String>>builder()
                .code(HttpStatus.OK.value())
                .reason("Liste des opérateurs de service MOMO distincts")
                .data(operateurs)
                .build();

        return ResponseEntity.ok(response);
    }

    private Date parseDate(String dateStr, boolean isStart) {
        if (!StringUtils.hasText(dateStr)) return null;

        try {
            if (dateStr.length() == 10) { // yyyy-MM-dd (sans l'heure)
                LocalDate localDate = LocalDate.parse(dateStr);
                LocalDateTime dateTime = isStart ? localDate.atStartOfDay() : localDate.atTime(LocalTime.MAX);
                return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            } else { // yyyy-MM-dd'T'HH:mm:ss
                LocalDateTime dateTime = LocalDateTime.parse(dateStr);
                return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
        } catch (DateTimeParseException e) {
            throw new BadRequestException(String.format("Format de date invalide : %s. " +
                    "Veuillez fournir ce format yyyy-MM-dd ou yyyy-MM-dd'T'HH:mm:ss", dateStr));
        }
    }
}
