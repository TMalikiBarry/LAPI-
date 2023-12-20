package sn.intouch.gu.lonaciapi.ws.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;
import sn.intouch.gu.lonaciapi.ejb.notification.models.LonaciNotification;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
public class NotificationExchange implements Serializable {

	private static final long serialVersionUID = 1L;

	private String operatorID;
	private String operatorTransactionID;
    private String type;
    private Double amount;
    private Long date;
    private String userID;
    
    public NotificationExchange() {
		super();
	}

	public LonaciNotification buildNotification() {
    	LonaciNotification notification = new LonaciNotification();
    	notification.setDate(new Date(date));
    	notification.setOperateurID(operatorID);
    	notification.setTypeTransaction(type);
    	notification.setMontant(amount);
    	notification.setUserID(userID);
    	notification.setOperatorTransactionID(operatorTransactionID);
    	return notification;
    }
	
	public LonaciTrxTemp buildTransactionTemp() {
		LonaciTrxTemp notification = new LonaciTrxTemp();
    	notification.setDate(new Date(date));
    	notification.setOperateurID(operatorID);
    	notification.setTypeTransaction(type);
    	notification.setMontant(amount);
    	notification.setDestinataire(userID);
    	notification.setIdFromPartner(operatorTransactionID);
    	return notification;
    }
}
