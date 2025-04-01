package sn.intouch.gu.lonaciapi.ws.services.crud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.config.BadRequestException;
import sn.intouch.gu.lonaciapi.config.EntityNotFoundCustomException;
import sn.intouch.gu.lonaciapi.ejb.dto.OperatorDTO;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OperatorRestService {
    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);

    @RequestMapping(value = "/api/v2/operator/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getOperator(@PathVariable String id) {
        Operator operator = operatorService.findByOperatorID(id);
        if (operator != null)
            return ResponseEntity.ok(APIResponse.<OperatorDTO>builder()
                            .code(200)
                            .reason("SUCCESS")
                            .data(operator.toDTO())
                    .build());
        return new ResponseEntity<>(APIResponse.<OperatorDTO>builder()
                .code(404)
                .reason("Operator not found")
                .build(), HttpStatus.NOT_FOUND);
    }

    private List<OperatorDTO> operatorsToList(Iterable<Operator> operators) {
        if (operators == null)
            return null;
        List<OperatorDTO> list =new ArrayList<>();
        operators.forEach(op -> list.add(op.toDTO()));
        return list;
    }

    @RequestMapping(value = "/api/v2/operator", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> createOperator(@RequestBody OperatorDTO dto) {
        Operator operator = operatorService.findByOperatorID(dto.getIdentifier());
        if (operator != null) {
            return new ResponseEntity<>(APIResponse.<OperatorDTO>builder()
                    .code(201)
                    .reason("CREATED")
                    .build(), HttpStatus.CREATED);
        }
        operator = dto.fromDTO();
        if(operator.getCountry() == null)
            return new ResponseEntity<>(APIResponse.<OperatorDTO>builder()
                    .code(400)
                    .reason("Country is null.")
                    .build(), HttpStatus.BAD_REQUEST);

        operator = operatorService.save(operator);
        return ResponseEntity.ok(APIResponse.<OperatorDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(operator.toDTO())
                .build());
    }

    // Récupérer une liste d'opérateurs par leur identifiant
    @PostMapping(value = "/api/v2/operator/some_ops", consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse<List<OperatorDTO>>> getOperatorsByIds(@RequestBody List<String> operatorIds) {
        if (operatorIds == null || operatorIds.isEmpty()) {
            throw new BadRequestException("Operator IDs list is empty");
        }
        List<Operator> operators = operatorService.findByOperatorIds(operatorIds);
        if (operators.isEmpty()) {
            throw new EntityNotFoundCustomException("No operators found");
        }
        List<OperatorDTO> dtoList = operators.stream()
                .map(Operator::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(APIResponse.<List<OperatorDTO>>builder()
                .code(HttpStatus.OK.value())
                .reason("SUCCESS")
                .data(dtoList)
                .build());
    }
    @RequestMapping(value = "/api/v2/operator/{id}", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> updateOperator(@RequestBody OperatorDTO dto, @PathVariable String id) {
        Operator operator = operatorService.findByOperatorID(id);
        if (operator == null)
            return new ResponseEntity<>(APIResponse.<OperatorDTO>builder()
                    .code(404)
                    .reason("Operator not found.")
                    .build(), HttpStatus.NOT_FOUND);

        transposeUpdate(operator, dto);
        operator = operatorService.save(operator);
        return ResponseEntity.ok(APIResponse.<OperatorDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(operator.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v2/operator/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> deleteOperator(@PathVariable String id ) {
        Operator operator = operatorService.findByOperatorID(id);
        if (operator == null)
            return new ResponseEntity<>(APIResponse.<OperatorDTO>builder()
                    .code(404)
                    .reason("Operator not found.")
                    .build(), HttpStatus.NOT_FOUND);

        operatorService.delete(operator);
        return ResponseEntity.ok(APIResponse.<OperatorDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                .build());
    }

    @RequestMapping(value = "/api/v2/operator", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> getOperatorByCountry(@RequestParam(required = false) String country){
        Iterable<Operator> operators;
        if(country == null){
            operators = operatorService.getAll();
        }else {
            operators = operatorService.findByCountry(country);
        }
        return ResponseEntity.ok(APIResponse.<List<OperatorDTO>>builder()
                .code(200)
                .reason("SUCCESS")
                .data(operatorsToList(operators)).build()
        );
    }

    private void transposeUpdate(Operator operator, OperatorDTO dto) {
        if (StringUtils.hasText(dto.getIdentifier() )) operator.setOperatorId(dto.getIdentifier());
        if (StringUtils.hasText(dto.getLabel() )) operator.setOperatorLabel(dto.getLabel());
        if (StringUtils.hasText(dto.getToken() )) operator.setOperatorToken(dto.getToken());
        if (StringUtils.hasText(dto.getMerchantCode() )) operator.setMerchantCode(dto.getMerchantCode());
        if (StringUtils.hasText(dto.getNetworkCode() )) operator.setNetworkCode(dto.getNetworkCode());
        if (StringUtils.hasText(dto.getStatus() )) operator.setStatut(dto.getStatus());
        if (StringUtils.hasText(dto.getCountry() ) ) operator.setCountry(dto.getCountry());
    }
}
