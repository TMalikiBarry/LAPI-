package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.dto.CountryDTO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="country")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timeZone")
    private String timeZone;

    @Column(name = "indicatif")
    private String indicatif;

    @Column(name = "devise")
    private String devise;

    @Column(name = "langue")
    private String langue;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "code")
    private String code;

    @Column(name="supprime")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "creation_date")
    private Date creationDate;
    @Column(name = "modification_date")
    private Date modificationDate;

    @PreUpdate
    private void updatedDate() {
        this.modificationDate = new Date();
    }
    @PrePersist
    private void createdDate() {
        this.creationDate = new Date();
        this.modificationDate = new Date();
    }

    public CountryDTO toDTO(){
        return CountryDTO.builder()
                .timeZone(timeZone)
                .indicatif(indicatif)
                .devise(devise)
                .langue(langue)
                .libelle(libelle)
                .code(code)
                .build();
    }
}
