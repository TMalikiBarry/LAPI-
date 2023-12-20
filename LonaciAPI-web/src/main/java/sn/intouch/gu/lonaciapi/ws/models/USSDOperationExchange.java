package sn.intouch.gu.lonaciapi.ws.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class USSDOperationExchange implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private Double montant;
	private String destinataire;
	private String token;
	private String operationType;
	private String tag;
	private String codePDA;
	private String codeSalePoint;
	private String codeNetwork;
	private String codeService;
	private String loginAgent;
	private String paymentMode;
	private String idFromPartenaire;
	private Double commission;
	private Double frais;
	private Date date;
	private String sms;
	private String codeProxy;
	private String userDoneAction;
	private String actionDone;
	private String callBackURL;
	private String partnerDistTransactionId;
	private String network_groupe_code;
	/**
	 * CASHOUTOMCODE pour spareOp1
	 * baseCalculCommission pour spareOp2
	 */
	private String spareOp1, spareOp2, spareOp3, spareOp4, spareOp5;
	String statutPartenaire = null;
	private Date sentDate;

	public LonaciTrx buildLonaciTrxFromOperation() {
		LonaciTrx trx = new LonaciTrx();
		trx.setOperateurID(codeSalePoint);
		trx.setIdFromPartner(partnerDistTransactionId);
		trx.setCodeService(codeService);
		trx.setMontant(montant);
		trx.setDestinataire(destinataire);
		trx.setDate(date);
		trx.setLonaciTransactionID(token);
		return trx;
	}
	
	public LonaciTrxTemp buildLonaciTrxTempFromOperation() {
		LonaciTrxTemp trx = new LonaciTrxTemp();
		trx.setOperateurID(codeSalePoint);
		trx.setIdFromPartner(partnerDistTransactionId);
		trx.setCodeService(codeService);
		trx.setMontant(montant);
		trx.setDestinataire(destinataire);
		trx.setDate(date);
		trx.setLonaciTransactionID(token);
		return trx;
	}

}
