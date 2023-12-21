package sn.intouch.gu.lonaciapi.ws.services.crud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.dto.OperatorDTO;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OperatorRestService {
    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);

    @RequestMapping(value = "/api/v1/operator/{id}", method = RequestMethod.GET, produces = "application/json")
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

    @RequestMapping(value = "/api/v1/operator", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getOperators() {
        Iterable<Operator> operators = operatorService.getAll();

        return ResponseEntity.ok(APIResponse.<List<OperatorDTO>>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(operatorsToList(operators))
                .build());
    }

    private List<OperatorDTO> operatorsToList(Iterable<Operator> operators) {
        if (operators == null)
            return null;
        List<OperatorDTO> list =new ArrayList<>();
        operators.forEach(op -> list.add(op.toDTO()));
        return list;
    }

    @RequestMapping(value = "/api/v1/operator", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> createOperator(@RequestBody OperatorDTO dto) {
        Operator operator = operatorService.findByOperatorID(dto.getIdentifier());
        if (operator != null) {
            return new ResponseEntity<>(APIResponse.<OperatorDTO>builder()
                    .code(201)
                    .reason("CREATED")
                    .build(), HttpStatus.CREATED);
        }
        operator = dto.fromDTO();
        operator = operatorService.save(operator);
        return ResponseEntity.ok(APIResponse.<OperatorDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(operator.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v1/operator/{id}", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
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

    @RequestMapping(value = "/api/v1/operator/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
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

    private void transposeUpdate(Operator operator, OperatorDTO dto) {
        if (dto.getIdentifier() != null) operator.setOperatorId(dto.getIdentifier());
        if (dto.getLabel() != null) operator.setOperatorLabel(dto.getLabel());
        if (dto.getToken() != null) operator.setOperatorToken(dto.getToken());
        if (dto.getMerchantCode() != null) operator.setMerchantCode(dto.getMerchantCode());
        if (dto.getNetworkCode() != null) operator.setNetworkCode(dto.getNetworkCode());
        if (dto.getStatus() != null) operator.setStatut(dto.getStatus());
    }
}
