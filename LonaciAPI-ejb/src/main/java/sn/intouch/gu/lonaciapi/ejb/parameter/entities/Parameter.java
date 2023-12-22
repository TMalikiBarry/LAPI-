package sn.intouch.gu.lonaciapi.ejb.parameter.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "parametre")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameter implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prm_id")
    private int prmId;
    @Column(name = "prm_value")
    private int prmValue = 1;
    @Column(name = "prm_code")
    private String prmCode;
    @Column(name = "prm_stringvalue")
    private String prmStringValue;
    @Column(name = "prm_statut")
    @Builder.Default
    private int prmStatut = 1;

    @Override
    public String toString() {
        return "Parametre [prmId=" + prmId + ", prmValue=" + prmValue + ", prmCode=" + prmCode + ", prmStringValue="
                + prmStringValue + "]";
    }

}
