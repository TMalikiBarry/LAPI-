package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface LonaciTrxTempService {
	LonaciTrxTemp saveTransaction(LonaciTrxTemp transaction);
	LonaciTrxTemp getTrxTempByIdFromPartnerBetweenDates(String idFromPartner, Date dateDeb, Date dateFin);
}
