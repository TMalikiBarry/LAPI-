package sn.intouch.gu.lonaciapi.ws.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sn.intouch.gu.lonaciapi.config.*;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.ForbiddenException;
import java.util.List;
import java.util.stream.Collectors;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        String errorMessage = String.join(", ", errors);
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .reason(errorMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.toList());
        String errorMessage = String.join(", ", errors);
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .reason(errorMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<APIResponse<Object>> handleBadRequest(BadRequestException ex) {
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .reason(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundCustomException.class)
    public ResponseEntity<APIResponse<Object>> handleNotFound(EntityNotFoundCustomException ex) {
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .reason(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<APIResponse<Object>> handleDuplicateEntry(DuplicateEntryException ex) {
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.CONFLICT.value())
                .reason(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<APIResponse<Object>> handleInvalidDateException(InvalidDateException ex) {
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.NOT_ACCEPTABLE.value()) // 406
                .reason(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<APIResponse<Object>> handleUnauthorizedException(UnauthorizedException ex) {
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value()) // 401
                .reason("Accès non autorisé. Veuillez vous authentifier.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<APIResponse<Object>> handleForbiddenException(ForbiddenException ex) {
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.FORBIDDEN.value()) // 403
                .reason("Accès interdit. Vous n'avez pas les permissions requises.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // Gestion des exceptions non prévues ou générales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGenericException(Exception ex) {
        APIResponse<Object> response = APIResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .reason("Internal Server Error: " + ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}