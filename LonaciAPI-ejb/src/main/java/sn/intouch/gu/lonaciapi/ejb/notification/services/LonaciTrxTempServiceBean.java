package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Stateless
public class LonaciTrxTempServiceBean implements LonaciTrxTempService {

	@PersistenceContext(unitName = "lonaciPU")
	EntityManager em;


	public LonaciTrx getTransactionById(Long id) {
		LonaciTrx transaction = em.find(LonaciTrx.class, id);
		if(transaction==null) 
			throw new RuntimeException("Aucune transaction trouvée avec cet ID");
		return transaction;
	}

	@Override
	public LonaciTrxTemp saveTransaction(LonaciTrxTemp transaction) {
		try {
			return em.merge(transaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public LonaciTrxTemp getTrxTempByIdFromPartnerBetweenDates(String idFromPartner, Date dateDeb, Date dateFin) {
		String sql = "SELECT p FROM LonaciTrxTemp p where p.idFromPartner = :idFromPartner and date BETWEEN :dateDeb AND :dateFin";
		try {
			Query query = em.createQuery(sql, LonaciTrxTemp.class);
			query.setParameter("idFromPartner", idFromPartner);
			query.setParameter("dateDeb", dateDeb);
			query.setParameter("dateFin", dateFin);
			List<LonaciTrxTemp> lonaciTrxs = (List<LonaciTrxTemp>) query.getResultList();
			if (lonaciTrxs != null && !lonaciTrxs.isEmpty()) return lonaciTrxs.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return null;
	}

}
