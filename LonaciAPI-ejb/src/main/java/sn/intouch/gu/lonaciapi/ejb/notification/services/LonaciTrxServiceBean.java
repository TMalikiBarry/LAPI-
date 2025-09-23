package sn.intouch.gu.lonaciapi.ejb.notification.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import sn.intouch.gu.lonaciapi.ejb.dto.IntouchSummaryDTO;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.models.LonaciTrxDTO;
import sn.intouch.gu.lonaciapi.ejb.notification.models.PaginationResponse;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Stateless
public class LonaciTrxServiceBean implements LonaciTrxService {

	@PersistenceContext(unitName = "lonaciPU")
	EntityManager em;

	private static final String CI_COUNTRY_CODE = "CI";
	private static final String DEPOT_MOMO_TYPE_TRX = "depot_momo";
	private static final String RETRAIT_MOMO_TYPE_TRX = "retrait";

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
	public PaginationResponse<List<LonaciTrxDTO>> customFindByDateBetweenAndOperateurIDAndTypeTransaction(
            String country, Date startDate, Date endDate, String operatorId, String typeTransaction, String codeService,
			String momoOperator, Double amount, String sortBy, String sortDir, int pageSize, int page
	) {
		String sqlQuery = "SELECT DISTINCT t, " +
				"       csm.operateurServiceMomo, " +
				"       cp.paymentFees, " +
				"       cp.cashinFees " +
				"FROM LonaciTrx t " +
				"LEFT JOIN CodeServiceMOMO csm ON t.codeService = csm.codeMomo AND csm.codeIso = :country AND csm.deleted = false " +
				"LEFT JOIN ComputeParameter cp ON t.operateurID = cp.operator AND t.country = cp.country AND cp.isActive = true " +
				"WHERE t.date BETWEEN :startDate AND :endDate AND t.country = :country ";

/*
String sqlQuery = "SELECT t, " +
				"       csm.operateurServiceMomo, " +
				"       cp.paymentFees, " +
				"       cp.cashinFees " +
				"FROM LonaciTrx t " +
				"LEFT JOIN CodeServiceMOMO csm ON t.codeService = csm.codeMomo AND csm.codeIso = :country " +
				"LEFT JOIN ComputeParameter cp ON t.operateurID = cp.operator AND t.country = cp.country " +
				"WHERE t.date BETWEEN :startDate AND :endDate AND t.country = :country";
*/

//		String aggSqlQuery = "SELECT COUNT(*), sum(t.montant) from lonaci_trx t WHERE t.date BETWEEN :startDate AND :endDate ";
		String aggSqlQuery = "SELECT COUNT(*), SUM(t.montant) " +
				"FROM lonaci_trx t " +
				"LEFT JOIN code_service_momo c ON t.code_service = c.code_momo AND c.code_iso = :country " +
				"WHERE t.date BETWEEN :startDate AND :endDate AND t.country = :country";


		if (StringUtils.hasText(momoOperator)) {
			// Filtre sur l'opérateur depuis CodeServiceMOMO
			sqlQuery += " AND LOWER(csm.operateurServiceMomo) = LOWER(:operateurMomo) ";
			aggSqlQuery += " AND LOWER(c.operateur_service_momo) = LOWER(:operateurMomo) ";
		}

		if (StringUtils.hasText(operatorId)) {
			sqlQuery += " AND t.operateurID = :operatorId";
			aggSqlQuery += " AND t.operateur_id = :operatorId";
		}

		if (StringUtils.hasText(codeService)) {
			sqlQuery += " AND t.codeService = :codeService";
			aggSqlQuery += " AND t.code_service = :codeService";
		}

		if (amount != null && amount != 0D) {
			sqlQuery += " AND t.montant = :montant";
			aggSqlQuery += " AND t.montant = :montant";
		}

		if (StringUtils.hasText(typeTransaction )) {
			sqlQuery += " AND t.typeTransaction = :typeTransaction";
			aggSqlQuery += " AND t.type_transaction = :typeTransaction";
		}

		if (!StringUtils.hasText(sortBy) && !StringUtils.hasText(sortDir)) {
			sqlQuery += " ORDER BY " + " " + sortBy + " " + sortDir;
		} else {
			sqlQuery += " ORDER BY t.date DESC";
		}


		Query query = em.createQuery(sqlQuery);
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

		if (amount != null && amount != 0D) {
			query.setParameter("montant", amount);
			aggQuery.setParameter("montant", amount);
		}

		if (StringUtils.hasText(momoOperator)) {
			query.setParameter("operateurMomo", momoOperator);
			aggQuery.setParameter("operateurMomo", momoOperator);
		}


		query.setParameter("country", country);
		aggQuery.setParameter("country", country);


		List<Object[]> aggResult = aggQuery.getResultList();
		if (page != -1) {
			query.setFirstResult(page * pageSize);
			query.setMaxResults(pageSize);
		}
		long totalsize = Long.parseLong((aggResult.get(0)[0]).toString());
		Double sum = (aggResult.get(0)[1]) != null ? Double.parseDouble((aggResult.get(0)[1]).toString()) : 0;

		List<Object[]> results = query.getResultList();

		List<LonaciTrxDTO> dtoList = results.stream()
				.map(result -> {
					LonaciTrx trx = (LonaciTrx) result[0];
					String operateurServiceMomo = (String) result[1];
					Double paymentFees = (Double) result[2];
					Double cashinFees = (Double) result[3];

					double commissionValue = 0;

					if (StringUtils.hasText(operateurServiceMomo)) {
						if (DEPOT_MOMO_TYPE_TRX.equalsIgnoreCase(trx.getTypeTransaction())) {
							commissionValue = trx.getMontant() * (paymentFees != null ? paymentFees : (CI_COUNTRY_CODE.equals(country) ? 0.02 : 0));
						} else if (RETRAIT_MOMO_TYPE_TRX.equalsIgnoreCase(trx.getTypeTransaction())) {
							commissionValue = trx.getMontant() * (cashinFees != null ? cashinFees : (CI_COUNTRY_CODE.equals(country) ? 0.04 : 0));
						}
					}


					return LonaciTrxDTO.builder()
							.transactionId(trx.getTransactionId())
							.operateurID(trx.getOperateurID())
							.operateurLibelle(trx.getOperateurLibelle())
							.idFromPartner(trx.getIdFromPartner())
							.codeService(trx.getCodeService())
							.typeTransaction(trx.getTypeTransaction())
							.montant(trx.getMontant())
							.intouchCommission(this.formatLabelAmount(commissionValue))
							.destinataire(trx.getDestinataire())
							.date(trx.getDate())
							.lonaciTransactionID(trx.getLonaciTransactionID())
							.country(trx.getCountry())
							.operateurServiceMomo(operateurServiceMomo)
							.build();
				})
				.collect(Collectors.toList());


		return PaginationResponse.<List<LonaciTrxDTO>>builder()
				.totalSize(totalsize)
				.sum(sum)
				.pageSize(pageSize)
				.data(dtoList)
				.build();
	}

	@Override
	public PaginationResponse<List<IntouchSummaryDTO>> getGroupedIntouchSummaryPaginated(
			Date startDate,
			Date endDate,
			int page,
			int pageSize,
			String country,
			String operatorId,
			String momoOperator) {

		// 1) SQL de données avec CASE WHEN et taux par défaut corrigés
		String dataSql =
				"SELECT " +
						"  t.operateur_id            AS operatorId, " +
						"  t.type_transaction        AS typeTransaction, " +
						"  csm.operateur_service_momo AS momoOperator, " +
						"  SUM(t.montant)            AS totalAmount, " +
						"  SUM( " +
						"    CASE " +
						"      WHEN LOWER(t.type_transaction) = 'depot_momo' " +
						"        THEN t.montant * COALESCE(cp.payment_fees, CASE WHEN t.country = 'CI' THEN 0.02 ELSE 0 END) " +
						"      WHEN LOWER(t.type_transaction) = 'retrait' " +
						"        THEN t.montant * COALESCE(cp.cashin_fees, CASE WHEN t.country = 'CI' THEN 0.04 ELSE 0 END) " +
						"      ELSE 0 " +
						"    END " +
						"  )                         AS totalCommission " +
						"FROM lonaci_trx t " +
						"INNER JOIN code_service_momo csm " +
						"  ON t.code_service = csm.code_momo " +
						" AND csm.code_iso   = :country " +
						"LEFT JOIN compute_parameter cp " +
						"  ON t.operateur_id = cp.operator " +
						" AND t.country      = cp.country " +
						"WHERE t.date BETWEEN :startDate AND :endDate " +
						"  AND t.country = :country " +
						"  AND LOWER(t.type_transaction) IN ('depot_momo','retrait') ";

		if (StringUtils.hasText(operatorId)) {
			dataSql += " AND t.operateur_id = :operatorId ";
		}
		if (StringUtils.hasText(momoOperator)) {
			dataSql += " AND LOWER(csm.operateur_service_momo) = LOWER(:momoOperator) ";
		}

		dataSql +=
				" GROUP BY " +
						"   t.operateur_id, " +
						"   t.type_transaction, " +
						"   csm.operateur_service_momo " +
						" ORDER BY " +
						"   t.operateur_id ASC, " +
						"   t.type_transaction ASC";

		// 2) SQL de comptage total des groupes (sans pagination)
		String countSql =
				"SELECT COUNT(*) FROM ( " +
						"  SELECT t.operateur_id, t.type_transaction, csm.operateur_service_momo " +
						"  FROM lonaci_trx t " +
						"  INNER JOIN code_service_momo csm ON t.code_service = csm.code_momo AND csm.code_iso = :country " +
						"  LEFT JOIN compute_parameter cp ON t.operateur_id = cp.operator AND t.country = cp.country " +
						"  WHERE t.date BETWEEN :startDate AND :endDate " +
						"    AND t.country = :country " +
						"    AND LOWER(t.type_transaction) IN ('depot_momo','retrait') ";

		if (StringUtils.hasText(operatorId)) {
			countSql += " AND t.operateur_id = :operatorId ";
		}
		if (StringUtils.hasText(momoOperator)) {
			countSql += " AND LOWER(csm.operateur_service_momo) = LOWER(:momoOperator) ";
		}

		countSql +=
				"  GROUP BY t.operateur_id, t.type_transaction, csm.operateur_service_momo " +
						") AS count_sub";

		// 3) Exécution de la query paginée
		Query dataQuery = em.createNativeQuery(dataSql);
		dataQuery.setParameter("startDate", startDate);
		dataQuery.setParameter("endDate", endDate);
		dataQuery.setParameter("country", country);
		if (StringUtils.hasText(operatorId)) {
			dataQuery.setParameter("operatorId", operatorId);
		}
		if (StringUtils.hasText(momoOperator)) {
			dataQuery.setParameter("momoOperator", momoOperator);
		}
		dataQuery.setFirstResult(page * pageSize);
		dataQuery.setMaxResults(pageSize);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = dataQuery.getResultList();

		// 4) Exécution de la query de comptage
		Query countQuery = em.createNativeQuery(countSql);
		countQuery.setParameter("startDate", startDate);
		countQuery.setParameter("endDate", endDate);
		countQuery.setParameter("country", country);
		if (StringUtils.hasText(operatorId)) {
			countQuery.setParameter("operatorId", operatorId);
		}
		if (StringUtils.hasText(momoOperator)) {
			countQuery.setParameter("momoOperator", momoOperator);
		}
		Number totalGroups = (Number) countQuery.getSingleResult();

		// 5) Mapping vers DTO
		List<IntouchSummaryDTO> data = rows.stream().map(r -> {
			String opId = (String) r[0];
			String type = (String) r[1];
			String momo = (String) r[2];
			double amt = ((Number) r[3]).doubleValue();
			double comm = ((Number) r[4]).doubleValue();

			return IntouchSummaryDTO.builder()
					.operatorId(opId)
					.typeTransaction(type)
					.momoOperator(momo)
					.totalAmount(formatLabelAmount(amt))
					.totalCommission(formatLabelAmount(comm))
					.build();
		}).collect(Collectors.toList());

		// 6) Retour
		return PaginationResponse.<List<IntouchSummaryDTO>>builder()
				.totalSize(totalGroups.longValue())
				.pageSize(pageSize)
				.data(data)
				.build();
	}


	public String formatLabelAmount(double amount) {

		if (amount == 0) return " -- ";
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
		symbols.setDecimalSeparator('.');      // Remplacer la virgule par un point
		symbols.setGroupingSeparator(' ');       // Garder l'espace comme séparateur de milliers
		DecimalFormat df = new DecimalFormat("#,##0.##", symbols);
		return df.format(amount);
	}
}
