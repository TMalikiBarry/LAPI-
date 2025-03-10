package sn.intouch.gu.lonaciapi.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Code HTTP 400
public class BadRequestException extends RuntimeException {
    private final int errorCode;

    public BadRequestException(String message) {
        super(message);
        this.errorCode = HttpStatus.BAD_REQUEST.value();
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = HttpStatus.BAD_REQUEST.value();
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
