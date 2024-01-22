package sn.intouch.gu.lonaciapi.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubHeaderResponse implements Serializable {
    private Integer operationsNumber;
    private Integer activeClients;
    private Double averageCart;
    private Double overallVolume;
}
