package sn.intouch.gu.lonaciapi.ejb.notification.services;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.StringUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.models.PaginationResponse;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.CodeServiceMOMORepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class LonaciTrxServiceBean implements LonaciTrxService {

	@PersistenceContext(unitName = "lonaciPU")
	EntityManager em;

	private CodeServiceMOMORepository codeServiceRepository;

	@PostConstruct
	private void init() {
		RepositoryFactorySupport factorySupport = new JpaRepositoryFactory(em);
		this.codeServiceRepository = factorySupport.getRepository(CodeServiceMOMORepository.class);
	}

	public LonaciTrx getTransactionById(Long id) {
		LonaciTrx transaction = em.find(LonaciTrx.class, id);
		if(transaction==null)
			throw new RuntimeException("No transaction found for this ID");
		return transaction;
	}

	@Override
	public LonaciTrx saveTransaction(LonaciTrx transaction) {
		try {
			return em.merge(transaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public LonaciTrx getTrxByIdFromPartner(String idFromPartner) {
		String sql = "SELECT p FROM LonaciTrx p where p.idFromPartner = :idFromPartner";
		try {
			Query query = em.createQuery(sql, LonaciTrx.class);
			query.setParameter("idFromPartner", idFromPartner);
			List<LonaciTrx> lonaciTrxs = (List<LonaciTrx>) query.getResultList();
			if (lonaciTrxs != null && !lonaciTrxs.isEmpty()) return lonaciTrxs.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public LonaciTrx getTrxByIdFromPartnerBetweenDates(String idFromPartner, Date dateDeb, Date dateFin) {
		String sql = "SELECT p FROM LonaciTrx p where p.idFromPartner = :idFromPartner and date BETWEEN :dateDeb AND :dateFin";
		try {
			Query query = em.createQuery(sql, LonaciTrx.class);
			query.setParameter("idFromPartner", idFromPartner);
			query.setParameter("dateDeb", dateDeb);
			query.setParameter("dateFin", dateFin);
			List<LonaciTrx> lonaciTrxs = (List<LonaciTrx>) query.getResultList();
			if (lonaciTrxs != null && !lonaciTrxs.isEmpty()) return lonaciTrxs.get(0);
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	@Override
	public boolean saveTrx(LonaciTrx transaction) {
		try {
			em.merge(transaction);
			return true;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public PaginationResponse<List<LonaciTrx>> customFindByDateBetweenAndOperateurIDAndTypeTransaction(
            String country, Date startDate, Date endDate, String operatorId, String typeTransaction, String codeService,
			String operateurMomo, Double montant, String sortBy, String sortDir, int pageSize, int page
	) {
		String sqlQuery = "SELECT t FROM LonaciTrx t WHERE t.date BETWEEN :startDate AND :endDate ";
		String aggSqlQuery = "SELECT COUNT(*), sum(t.montant) from lonaci_trx t WHERE t.date BETWEEN :startDate AND :endDate ";

		// Si le filtre opérateur est fourni, on recherche les codes services associés à cet opérateur
		List<String> codeServicesForOperator = null;
		if (StringUtils.hasText(operateurMomo)) {
			List<CodeServiceMOMO> codeServiceList = codeServiceRepository.findByOperateurServiceMomo(operateurMomo);
			if (!codeServiceList.isEmpty()) {
				codeServicesForOperator = codeServiceList.stream()
						.map(CodeServiceMOMO::getCodeMomo)
						.collect(Collectors.toList());
				// Filtrer les transactions dont le codeService figure dans la liste trouvée
				sqlQuery += " AND t.codeService IN :codeServices";
				aggSqlQuery += " AND t.code_service IN (:codeServices)";
			}
		}

		if (StringUtils.hasText(operatorId)) {
			sqlQuery += " AND t.operateurID = :operatorId";
			aggSqlQuery += " AND t.operateur_id = :operatorId";
		}

		if (StringUtils.hasText(codeService)) {
			sqlQuery += " AND t.codeService = :codeService";
			aggSqlQuery += " AND t.code_service = :codeService";
		}

		if (montant != null && montant != 0D) {
			sqlQuery += " AND t.montant = :montant";
			aggSqlQuery += " AND t.montant = :montant";
		}

		if (StringUtils.hasText(typeTransaction )) {
			sqlQuery += " AND t.typeTransaction = :typeTransaction";
			aggSqlQuery += " AND t.type_transaction = :typeTransaction";
		}
		if (country != null) {
			sqlQuery += " AND t.country = :country";
			aggSqlQuery += " AND t.country = :country";
		}
		if (sortBy != null && sortDir != null) {
			sqlQuery += " ORDER BY " + " " + sortBy + " " + sortDir;
		} else {
			sqlQuery += " ORDER BY t.date DESC";
		}


		Query query = em.createQuery(sqlQuery, LonaciTrx.class);
		Query aggQuery = em.createNativeQuery(aggSqlQuery);

		query.setParameter("startDate", startDate)
				.setParameter("endDate", endDate);
		aggQuery.setParameter("startDate", startDate)
				.setParameter("endDate", endDate);
		if (StringUtils.hasText(operatorId)) {
			query.setParameter("operatorId", operatorId);
			aggQuery.setParameter("operatorId", operatorId);
		}
		if (StringUtils.hasText(typeTransaction)) {
			query.setParameter("typeTransaction", typeTransaction);
			aggQuery.setParameter("typeTransaction", typeTransaction);
		}

		if (StringUtils.hasText(codeService)) {
			query.setParameter("codeService", codeService);
			aggQuery.setParameter("codeService", codeService);
		}

		if (montant != null && montant != 0D) {
			query.setParameter("montant", montant);
			aggQuery.setParameter("montant", montant);
		}

		if (codeServicesForOperator != null) {
			query.setParameter("codeServices", codeServicesForOperator);
			aggQuery.setParameter("codeServices", codeServicesForOperator);
		}

		if (country != null) {
			query.setParameter("country", country);
			aggQuery.setParameter("country", country);
		}

		List<Object[]> aggResult = aggQuery.getResultList();
		if (page != -1) {
			query.setFirstResult(page * pageSize);
			query.setMaxResults(pageSize);
		}
		long totalsize = Long.parseLong((aggResult.get(0)[0]).toString());
		Double sum = (aggResult.get(0)[1]) != null ? Double.parseDouble((aggResult.get(0)[1]).toString()) : 0;

		return PaginationResponse.<List<LonaciTrx>>builder()
				.totalSize(totalsize)
				.sum(sum)
				.pageSize(pageSize)
				.data(query.getResultList())
				.build();
	}
}
