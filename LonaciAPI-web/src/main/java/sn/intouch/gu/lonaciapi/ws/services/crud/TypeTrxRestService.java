package sn.intouch.gu.lonaciapi.ws.services.crud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.dto.TypeTrxDTO;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.services.TypeTrxService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TypeTrxRestService {
    private final TypeTrxService typeTrxService = (TypeTrxService) JNDIUtils
            .lookUpEJB(EJBRegistry.TypeTrxServiceBean);

    @RequestMapping(value = "/api/v1/typeTrx/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getTypeTrx(@PathVariable String id) {
        TypeTrx typeTrx = typeTrxService.getByCode(id);
        if (typeTrx != null)
            return ResponseEntity.ok(APIResponse.<TypeTrxDTO>builder()
                            .code(200)
                            .reason("SUCCESS")
                            .data(typeTrx.toDTO())
                    .build());
        return new ResponseEntity<>(APIResponse.<TypeTrxDTO>builder()
                .code(404)
                .reason("TypeTrx not found")
                .build(), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/api/v1/typeTrx", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getTypeTrxs() {
        Iterable<TypeTrx> typeTrxs = typeTrxService.findAll();

        return ResponseEntity.ok(APIResponse.<List<TypeTrxDTO>>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(typeTrxsToList(typeTrxs))
                .build());
    }

    private List<TypeTrxDTO> typeTrxsToList(Iterable<TypeTrx> typeTrxs) {
        if (typeTrxs == null)
            return null;
        List<TypeTrxDTO> list =new ArrayList<>();
        typeTrxs.forEach(op -> list.add(op.toDTO()));
        return list;
    }

    @RequestMapping(value = "/api/v1/typeTrx", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> createTypeTrx(@RequestBody TypeTrxDTO dto) {
        TypeTrx typeTrx = typeTrxService.getByCode(dto.getCode());
        if (typeTrx != null) {
            return new ResponseEntity<>(APIResponse.<TypeTrxDTO>builder()
                    .code(201)
                    .reason("CREATED")
                    .build(), HttpStatus.CREATED);
        }
        typeTrx = dto.fromDTO();
        typeTrx = typeTrxService.save(typeTrx);
        return ResponseEntity.ok(APIResponse.<TypeTrxDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(typeTrx.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v1/typeTrx/{id}", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> updateTypeTrx(@RequestBody TypeTrxDTO dto, @PathVariable String id) {
        TypeTrx typeTrx = typeTrxService.getByCode(id);
        if (typeTrx == null)
            return new ResponseEntity<>(APIResponse.<TypeTrxDTO>builder()
                    .code(404)
                    .reason("TypeTrx not found.")
                    .build(), HttpStatus.NOT_FOUND);

        transposeUpdate(typeTrx, dto);
        typeTrx = typeTrxService.save(typeTrx);
        return ResponseEntity.ok(APIResponse.<TypeTrxDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(typeTrx.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v1/typeTrx/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> deleteTypeTrx(@PathVariable String id ) {
        TypeTrx typeTrx = typeTrxService.getByCode(id);
        if (typeTrx == null)
            return new ResponseEntity<>(APIResponse.<TypeTrxDTO>builder()
                    .code(404)
                    .reason("TypeTrx not found.")
                    .build(), HttpStatus.NOT_FOUND);

        typeTrxService.delete(typeTrx);
        return ResponseEntity.ok(APIResponse.<TypeTrxDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                .build());
    }

    private void transposeUpdate(TypeTrx typeTrx, TypeTrxDTO dto) {
        if (dto.getCode() != null) typeTrx.setCode(dto.getCode());
        if (dto.getLabel() != null) typeTrx.setLabel(dto.getLabel());
    }
}
