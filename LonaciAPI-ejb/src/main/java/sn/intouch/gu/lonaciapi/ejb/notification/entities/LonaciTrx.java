package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.intouch.gu.lonaciapi.ejb.notification.models.LonaciNotification;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name="lonaci_trx")
@Data
@SuperBuilder
@AllArgsConstructor
public class LonaciTrx extends SuperLonaciTrx {
	
	public static LonaciTrx buildTrxFromNotification(LonaciNotification notification) {
		LonaciTrx lonaciTrx = new LonaciTrx();
		lonaciTrx.setOperateurID(notification.getOperateurID());
		lonaciTrx.setOperateurLibelle(notification.getOperateurLibelle());
		lonaciTrx.setIdFromPartner(notification.getIdFromPartner());
		lonaciTrx.setCodeService(notification.getCodeService());
		lonaciTrx.setTypeTransaction(notification.getTypeTransaction());
		lonaciTrx.setMontant(notification.getMontant());
		lonaciTrx.setDestinataire(notification.getDestinataire());
		lonaciTrx.setDate(notification.getDateTransaction());
		lonaciTrx.setLonaciTransactionID(notification.getLonaciTransactionID());
		return lonaciTrx;
	}
}
