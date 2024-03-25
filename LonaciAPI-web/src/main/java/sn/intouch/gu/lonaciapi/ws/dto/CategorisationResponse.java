package sn.intouch.gu.lonaciapi.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorisationResponse<T> {
    private T payin;
    private T payout;
    private T gain;
    private T bonus;
    private T mises;

}
