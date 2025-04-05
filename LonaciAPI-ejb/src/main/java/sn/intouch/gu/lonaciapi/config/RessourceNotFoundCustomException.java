package sn.intouch.gu.lonaciapi.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // Code HTTP 404
public class RessourceNotFoundCustomException extends RuntimeException {
    private final int errorCode;

    public RessourceNotFoundCustomException(String message) {
        super(message);
        this.errorCode = HttpStatus.NOT_FOUND.value();
    }

    public RessourceNotFoundCustomException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = HttpStatus.NOT_FOUND.value();
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}