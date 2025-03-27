package sn.intouch.gu.lonaciapi.ws.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<?> handleException(APIException ex) {
        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }
}
