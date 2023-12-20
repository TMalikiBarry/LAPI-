package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.intouch.gu.lonaciapi.ejb.notification.models.LonaciNotification;


@Entity
@Table(name="lonaci_trx_temp")
@Data
@SuperBuilder
@AllArgsConstructor
public class LonaciTrxTemp extends SuperLonaciTrx {

	public static LonaciTrxTemp buildTrxFromNotification(LonaciNotification notification) {
		LonaciTrxTemp lonaciTrx = new LonaciTrxTemp();
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
