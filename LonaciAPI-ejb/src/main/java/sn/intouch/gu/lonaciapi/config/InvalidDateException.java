package sn.intouch.gu.lonaciapi.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE) // 406
public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String message) {
        super(message);
    }
}