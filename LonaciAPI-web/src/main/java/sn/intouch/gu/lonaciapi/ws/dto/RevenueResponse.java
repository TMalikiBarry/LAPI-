package sn.intouch.gu.lonaciapi.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueResponse implements Serializable {
    private String startDate;
    private String endDate;
    private String operator;
    private Double grossGamingProduct = 0D;
    private Double integratorRemuneration = 0D;
    private Double revenue = 0D;
    private Double royalties = 0D;
}
