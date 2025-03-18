package sn.intouch.gu.lonaciapi.ejb.notification.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LonaciTrxDTO implements Serializable {
    private Long transactionId;
    private String operateurID;
    private String operateurLibelle;
    private String idFromPartner;
    private String codeService;
    private String typeTransaction;
    private Double montant;
    private String destinataire;
    private Date date;
    private String lonaciTransactionID;
    private String country;
    // Nouveau champ provenant de CodeServiceMOMO
    private String operateurServiceMomo;
}