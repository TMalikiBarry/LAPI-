package sn.intouch.gu.lonaciapi.ejb.parameter.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "parametre")
public class Parametre implements Serializable {
    private int prmId, prmValue = 1;
    private String prmCode;
    private String prmStringValue = "";
    private int prmStatut = 1;

    @Id
    @Column(name = "prm_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getPrmId() {
        return prmId;
    }

    public void setPrmId(int prmId) {
        this.prmId = prmId;
    }

    @Column(name = "prm_code")
    public String getPrmCode() {
        return prmCode;
    }

    public void setPrmCode(String prmCode) {
        this.prmCode = prmCode;
    }

    @Column(name = "prm_value")
    public int getPrmValue() {
        return prmValue;
    }

    public void setPrmValue(int prmValue) {
        this.prmValue = prmValue;
    }

    @Column(name = "prm_stringvalue")
    public String getPrmStringValue() {
        return prmStringValue;
    }

    public void setPrmStringValue(String prmStringValue) {
        this.prmStringValue = prmStringValue;
    }

    @Column(name = "prm_statut")
    public int getPrmStatut() {
        return prmStatut;
    }

    public void setPrmStatut(int prmStatut) {
        this.prmStatut = prmStatut;
    }

    @Override
    public String toString() {
        return "Parametre [prmId=" + prmId + ", prmValue=" + prmValue + ", prmCode=" + prmCode + ", prmStringValue="
                + prmStringValue + "]";
    }

}
