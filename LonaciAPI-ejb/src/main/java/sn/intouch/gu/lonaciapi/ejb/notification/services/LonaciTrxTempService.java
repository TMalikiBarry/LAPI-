package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface LonaciTrxTempService {
	LonaciTrxTemp saveTransaction(LonaciTrxTemp transaction);
	LonaciTrxTemp getTrxTempByIdFromPartnerAndTypeBetweenDates(String idFromPartner, String type, Date dateDeb, Date dateFin);
}
