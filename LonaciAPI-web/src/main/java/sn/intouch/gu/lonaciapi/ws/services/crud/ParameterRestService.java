package sn.intouch.gu.lonaciapi.ws.services.crud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.config.RessourceNotFoundCustomException;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

@RestController
@RequestMapping(value = {"/api/v1/param", "/api/v2/param"})
public class ParameterRestService {

    private final ParameterService parameterService;

    public ParameterRestService() {
        this.parameterService = (ParameterService) JNDIUtils
                .lookUpEJB(EJBRegistry.ParameterServiceBean);
    }

    /**
     * Récupère l'ensemble de l'entité Parameter pour un code donné.
     * GET /api/v1/parameters/{code}
     */
    @GetMapping(value = "/{code}", produces = "application/json")
    public ResponseEntity<APIResponse<Parameter>> getParameterByCode(
            @PathVariable("code") String code) {

        Parameter param = parameterService.getParameterByCode(code);
        if (param == null) {
            throw new RessourceNotFoundCustomException("Parameter not found for code: " + code);
        }

        return ResponseEntity.ok(APIResponse.<Parameter>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(param)
                .build());
    }

    /**
     * Récupère la seule chaîne prmStringValue pour un code donné.
     * GET /api/v1/parameters/{code}/value
     */
    @GetMapping(value = "/{code}/value", produces = "application/json")
    public ResponseEntity<APIResponse<String>> getStringValue(
            @PathVariable("code") String code) {

        String value = parameterService.getStringValue(code);
        if (value == null) {
            throw new RessourceNotFoundCustomException("String value not found for code: " + code);
        }

        return ResponseEntity.ok(APIResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(value)
                .build());
    }
}