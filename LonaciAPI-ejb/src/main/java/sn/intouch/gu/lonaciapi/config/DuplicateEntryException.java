package sn.intouch.gu.lonaciapi.config;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT) // Renvoie automatiquement le code 409
public class DuplicateEntryException extends RuntimeException {

    private final int errorCode;

    public DuplicateEntryException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DuplicateEntryException(String message) {
        this(message, HttpStatus.CONFLICT.value());
    }

}
