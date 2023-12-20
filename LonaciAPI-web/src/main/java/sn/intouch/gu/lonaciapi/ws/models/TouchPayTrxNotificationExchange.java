package sn.intouch.gu.lonaciapi.ws.models;

import lombok.Data;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;

import java.io.Serializable;
import java.util.Date;


@Data
public class TouchPayTrxNotificationExchange implements Serializable {

    private Long id;

    private String operateurID;

    private String idFromPartner;

    private String codeService;

    private String typeTransaction;  //valeurs:  hors paiement et paiement

    private Double montant;
    private String destinataire;
    private Long dateTransaction;
    
    private Boolean supprime = false;

    private String lonaciTransactionID;

    public LonaciTrx buildLonaciTrx() {
    	LonaciTrx trx = new LonaciTrx();
    	trx.setOperateurID(operateurID);
    	trx.setIdFromPartner(idFromPartner);
    	trx.setCodeService(codeService);
    	trx.setMontant(montant);
    	trx.setDestinataire(destinataire);
    	trx.setDate(new Date(dateTransaction));
    	trx.setLonaciTransactionID(lonaciTransactionID);
    	
    	return trx;
    }
    
    public LonaciTrxTemp buildLonaciTrxTemp() {
    	LonaciTrxTemp trx = new LonaciTrxTemp();
    	trx.setOperateurID(operateurID);
    	trx.setIdFromPartner(idFromPartner);
    	trx.setCodeService(codeService);
    	trx.setMontant(montant);
    	trx.setDestinataire(destinataire);
    	trx.setDate(new Date(dateTransaction));
    	trx.setLonaciTransactionID(lonaciTransactionID);
    	
    	return trx;
    }

}