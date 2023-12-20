package sn.intouch.gu.lonaciapi.ws.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class APIResponse<T> implements Serializable{
    private String code;
    private String reason;
    private T data;
}
