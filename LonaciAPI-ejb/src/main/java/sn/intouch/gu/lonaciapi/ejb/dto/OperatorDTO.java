package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperatorDTO implements Serializable {
    private String identifier;
    private String label;
    private String token;
    private String merchantCode;
    private String networkCode;
    private Long creationDate, modificationDate;
    private String status;
    private String country;

    public Operator fromDTO() {
        return Operator.builder()
                .operatorId(identifier)
                .operatorLabel(label)
                .operatorToken(token)
                .merchantCode(merchantCode)
                .networkCode(networkCode)
                .statut(status)
                .country(country)
                .build();
    }
}
