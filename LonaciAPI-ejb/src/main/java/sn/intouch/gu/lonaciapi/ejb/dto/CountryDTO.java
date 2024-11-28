package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Country;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String timeZone;
    private String indicatif;
    private String devise;
    private String langue;
    private String libelle;
    private String code;

    public Country fromDTO(){
        return Country.builder()
                .timeZone(timeZone)
                .indicatif(indicatif)
                .devise(devise)
                .langue(langue)
                .libelle(libelle)
                .code(code)
                .build();
    }

}
