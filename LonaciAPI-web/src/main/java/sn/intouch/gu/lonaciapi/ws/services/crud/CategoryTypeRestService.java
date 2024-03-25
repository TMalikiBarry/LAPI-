package sn.intouch.gu.lonaciapi.ws.services.crud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.dto.CategoryTypeDTO;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CategoryType;
import sn.intouch.gu.lonaciapi.ejb.notification.services.CategoryTypeService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CategoryTypeRestService {
    private final CategoryTypeService categoryTypeService = (CategoryTypeService) JNDIUtils
            .lookUpEJB(EJBRegistry.CategoryTypeServiceBean);

    @RequestMapping(value = "/api/v2/categoryType/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getCategoryType(@PathVariable String id) {
        CategoryType categoryType = categoryTypeService.getByCode(id);
        if (categoryType != null)
            return ResponseEntity.ok(APIResponse.<CategoryTypeDTO>builder()
                            .code(200)
                            .reason("SUCCESS")
                            .data(categoryType.toDTO())
                    .build());
        return new ResponseEntity<>(APIResponse.<CategoryTypeDTO>builder()
                .code(404)
                .reason("CategoryType not found")
                .build(), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/api/v2/categoryType", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getCategoryTypes() {
        Iterable<CategoryType> categoryTypes = categoryTypeService.findAll();

        return ResponseEntity.ok(APIResponse.<List<CategoryTypeDTO>>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(categoryTypesToList(categoryTypes))
                .build());
    }

    private List<CategoryTypeDTO> categoryTypesToList(Iterable<CategoryType> categoryTypes) {
        if (categoryTypes == null)
            return null;
        List<CategoryTypeDTO> list =new ArrayList<>();
        categoryTypes.forEach(op -> list.add(op.toDTO()));
        return list;
    }

    @RequestMapping(value = "/api/v2/categoryType", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> createCategoryType(@RequestBody CategoryTypeDTO dto) {
        CategoryType categoryType = categoryTypeService.getByCode(dto.getCode());
        if (categoryType != null) {
            return new ResponseEntity<>(APIResponse.<CategoryTypeDTO>builder()
                    .code(201)
                    .reason("CREATED")
                    .build(), HttpStatus.CREATED);
        }
        categoryType = dto.fromDTO();
        categoryType = categoryTypeService.save(categoryType);
        return ResponseEntity.ok(APIResponse.<CategoryTypeDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(categoryType.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v2/categoryType/{id}", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> updateCategoryType(@RequestBody CategoryTypeDTO dto, @PathVariable String id) {
        CategoryType categoryType = categoryTypeService.getByCode(id);
        if (categoryType == null)
            return new ResponseEntity<>(APIResponse.<CategoryTypeDTO>builder()
                    .code(404)
                    .reason("CategoryType not found.")
                    .build(), HttpStatus.NOT_FOUND);

        transposeUpdate(categoryType, dto);
        categoryType = categoryTypeService.save(categoryType);
        return ResponseEntity.ok(APIResponse.<CategoryTypeDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                        .data(categoryType.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v2/categoryType/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> deleteCategoryType(@PathVariable String id ) {
        CategoryType categoryType = categoryTypeService.getByCode(id);
        if (categoryType == null)
            return new ResponseEntity<>(APIResponse.<CategoryTypeDTO>builder()
                    .code(404)
                    .reason("CategoryType not found.")
                    .build(), HttpStatus.NOT_FOUND);

        categoryTypeService.delete(categoryType);
        return ResponseEntity.ok(APIResponse.<CategoryTypeDTO>builder()
                        .code(200)
                        .reason("SUCCESS")
                .build());
    }

    private void transposeUpdate(CategoryType categoryType, CategoryTypeDTO dto) {
        if (dto.getCode() != null) categoryType.setCode(dto.getCode());
        if (dto.getLabel() != null) categoryType.setLabel(dto.getLabel());
    }
}
