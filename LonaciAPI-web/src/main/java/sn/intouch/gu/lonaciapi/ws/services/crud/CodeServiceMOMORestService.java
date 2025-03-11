package sn.intouch.gu.lonaciapi.ws.services.crud;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.config.BadRequestException;
import sn.intouch.gu.lonaciapi.config.DuplicateEntryException;
import sn.intouch.gu.lonaciapi.config.EntityNotFoundCustomException;
import sn.intouch.gu.lonaciapi.ejb.dto.SaveAllResponse;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;
import sn.intouch.gu.lonaciapi.ejb.notification.services.CodeServiceMOMOService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = {"/api/v2/service-momo", "/api/v1/service-momo"})
public class CodeServiceMOMORestService {

    private final CodeServiceMOMOService codeServiceMOMOService = (CodeServiceMOMOService) JNDIUtils.
            lookUpEJB(EJBRegistry.CodeServiceMOMOServiceBean);

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> getCodeServiceById(@PathVariable Long id) {
        try {
            CodeServiceMOMO entity = codeServiceMOMOService.findById(id);
            return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                    .code(200)
                    .reason("SUCCESS")
                    .data(entity)
                    .build());
        } catch (EntityNotFoundCustomException e) {
            return new ResponseEntity<>(APIResponse.<CodeServiceMOMO>builder()
                    .code(404)
                    .reason(e.getMessage())
                    .build(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<CodeServiceMOMO>>> getAllCodeServices() {
        List<CodeServiceMOMO> list = codeServiceMOMOService.findAll();
        return ResponseEntity.ok(APIResponse.<List<CodeServiceMOMO>>builder()
                .code(200)
                .reason("SUCCESS")
                .data(list)
                .build());
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> createCodeService(@RequestBody CodeServiceMOMO codeServiceMOMO) {
        Optional<CodeServiceMOMO> existing = codeServiceMOMOService
                .findByCodeMomoAndOperateurServiceMomo(codeServiceMOMO.getCodeMomo(), codeServiceMOMO.getOperateurServiceMomo());
        if (existing.isPresent()) {
            return new ResponseEntity<>(APIResponse.<CodeServiceMOMO>builder()
                    .code(409)
                    .reason("Ce code pour cet opérateur existe déjà.")
                    .build(), HttpStatus.CONFLICT);
        }
        CodeServiceMOMO saved = codeServiceMOMOService.save(codeServiceMOMO);
        return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(saved)
                .build());
    }

    @PostMapping(value = "/batch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<SaveAllResponse>> createCodeServices(@RequestBody List<CodeServiceMOMO> codeServiceMOMOList) {
        SaveAllResponse response = codeServiceMOMOService.saveAll(codeServiceMOMOList);
        return ResponseEntity.ok(APIResponse.<SaveAllResponse>builder()
                .code(200)
                .reason("SUCCESS")
                .data(response)
                .build());
    }

    @PatchMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> updateCodeService(@PathVariable Long id,
                                                                          @RequestBody CodeServiceMOMO codeServiceMOMO) {
        try {
            codeServiceMOMO.setId(id);
            CodeServiceMOMO updated = codeServiceMOMOService.update(codeServiceMOMO);
            return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                    .code(200)
                    .reason("SUCCESS")
                    .data(updated)
                    .build());
        } catch (BadRequestException | EntityNotFoundCustomException | DuplicateEntryException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e instanceof EntityNotFoundCustomException) {
                status = HttpStatus.NOT_FOUND;
            } else if (e instanceof DuplicateEntryException) {
                status = HttpStatus.CONFLICT;
            }
            return new ResponseEntity<>(APIResponse.<CodeServiceMOMO>builder()
                    .code(status.value())
                    .reason(e.getMessage())
                    .build(), status);
        }
    }

    @PatchMapping(value = "/by-code/{code}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> updateCodeServiceByCode(@PathVariable("code") String code,
                                                                                @RequestBody CodeServiceMOMO codeServiceMOMO) {
        try {
            CodeServiceMOMO updated = codeServiceMOMOService.updateByCodeService(codeServiceMOMO, code);
            return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                    .code(200)
                    .reason("SUCCESS")
                    .data(updated)
                    .build());
        } catch (EntityNotFoundCustomException | DuplicateEntryException e) {
            HttpStatus status = e instanceof EntityNotFoundCustomException ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
            return new ResponseEntity<>(APIResponse.<CodeServiceMOMO>builder()
                    .code(status.value())
                    .reason(e.getMessage())
                    .build(), status);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<CodeServiceMOMO>> searchByCodeAndOperator(
            @RequestParam("codeServiceMomo") String codeServiceMomo,
            @RequestParam("operateurServiceMomo") String operateurServiceMomo) {
        Optional<CodeServiceMOMO> result = codeServiceMOMOService
                .findByCodeMomoAndOperateurServiceMomo(codeServiceMomo, operateurServiceMomo);
        if (result.isPresent()) {
            return ResponseEntity.ok(APIResponse.<CodeServiceMOMO>builder()
                    .code(200)
                    .reason("SUCCESS")
                    .data(result.get())
                    .build());
        } else {
            return new ResponseEntity<>(APIResponse.<CodeServiceMOMO>builder()
                    .code(404)
                    .reason("Aucune entrée trouvée pour cette combinaison.")
                    .build(), HttpStatus.NOT_FOUND);
        }
    }

}
