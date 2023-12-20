package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.models.PaginationResponse;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface LonaciTrxService {

	LonaciTrx getTransactionById(Long id);
	LonaciTrx saveTransaction(LonaciTrx transaction);

	boolean saveTrx(LonaciTrx transaction);
	LonaciTrx getTrxByIdFromPartner(String idFromPartner);
	LonaciTrx getTrxByIdFromPartnerBetweenDates(String idFromPartner, Date dateDeb, Date dateFin);

	// Page<LonaciTrx> findByDateBetweenAndOperateurIDAndTypeTransaction(Date startDate, Date endDate, String operatorId, String type, Pageable pageable);

	// Page<LonaciTrx> findByExample(Example<LonaciTrx> example, Pageable pageable);

	PaginationResponse customFindByDateBetweenAndOperateurIDAndTypeTransaction(Date startDate, Date endDate, String operatorId, String type, String sortBy, String sortDir, int pageSize, int page);

}
