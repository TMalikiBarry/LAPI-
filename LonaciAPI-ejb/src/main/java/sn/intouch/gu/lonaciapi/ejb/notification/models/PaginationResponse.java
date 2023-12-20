package sn.intouch.gu.lonaciapi.ejb.notification.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PaginationResponse<T> implements Serializable{
    private long totalSize;
    private int pageSize;
    private Double sum;
    private T data;
}
