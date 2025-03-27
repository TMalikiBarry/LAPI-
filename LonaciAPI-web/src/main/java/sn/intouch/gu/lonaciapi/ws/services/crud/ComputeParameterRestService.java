package sn.intouch.gu.lonaciapi.ws.services.crud;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.ComputeParameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ComputeParameterService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.List;

@RestController
@RequestMapping(value = {"/api/v1/compute-parameter", "/api/v2/compute-parameter"}, produces = "application/json")
public class ComputeParameterRestService {

    // Récupération du service via JNDI (selon votre architecture)
    private final ComputeParameterService computeParameterService =
            (ComputeParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ComputeParameterServiceBean);

    // Endpoint paginé utilisant la méthode findByCriteria et enveloppant le résultat dans un PageImpl
    @GetMapping("/filter_paged")
    public ResponseEntity<APIResponse<Page<ComputeParameter>>> searchPaged(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "country", required = false) String country,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // Récupère la liste en fonction des critères
        List<ComputeParameter> list = computeParameterService.getParameterByOperatorAndCountry(operator, country);

        // Convertit la liste en PageImpl pour respecter la pagination
        Page<ComputeParameter> pageResult = new PageImpl<>(list, pageable, list.size());

        return ResponseEntity.ok(APIResponse.<Page<ComputeParameter>>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(pageResult)
                .build());
    }

    // Endpoint non paginé
    @GetMapping("/filter")
    public ResponseEntity<APIResponse<List<ComputeParameter>>> search(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "country", required = false) String country) {

        List<ComputeParameter> results = computeParameterService.getParameterByOperatorAndCountry(operator, country);

        return ResponseEntity.ok(APIResponse.<List<ComputeParameter>>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(results)
                .build());
    }

    // Récupérer un ComputeParameter par ID
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ComputeParameter>> getParameterById(@PathVariable Long id) {
        ComputeParameter cp = computeParameterService.getById(id);
        return ResponseEntity.ok(APIResponse.<ComputeParameter>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(cp)
                .build());
    }


    // Création d'un nouveau ComputeParameter
    @PostMapping(consumes = "application/json")
    public ResponseEntity<APIResponse<ComputeParameter>> createParameter(@RequestBody ComputeParameter cp) {
        ComputeParameter saved = computeParameterService.saveComputeParameter(cp);
        return ResponseEntity.ok(APIResponse.<ComputeParameter>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(saved)
                .build());
    }

    // Mise à jour d'un ComputeParameter
    @PatchMapping(consumes = "application/json")
    public ResponseEntity<APIResponse<ComputeParameter>> updateParameter(@RequestBody ComputeParameter cp) {
        ComputeParameter updated = computeParameterService.updateComputeParameter(cp);
        return ResponseEntity.ok(APIResponse.<ComputeParameter>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(updated)
                .build());
    }

    // Désactivation d'un ComputeParameter (set isActive à false)
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<APIResponse<String>> deactivateParameter(@PathVariable Long id) {
        computeParameterService.deactivateComputeParameter(id);
        return ResponseEntity.ok(APIResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data("ComputeParameter deactivated successfully")
                .build());
    }
}