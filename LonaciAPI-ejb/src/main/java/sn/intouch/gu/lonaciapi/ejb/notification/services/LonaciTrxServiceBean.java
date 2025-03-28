package sn.intouch.gu.lonaciapi.ejb.notification.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.StringUtils;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.models.LonaciTrxDTO;
import sn.intouch.gu.lonaciapi.ejb.notification.models.PaginationResponse;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.CodeServiceMOMORepository;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ComputeParameterService;

import javax.annotation.PostConstruct;
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

	private final ComputeParameterService computeParameterService = (ComputeParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ComputeParameterServiceBean);

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
	public PaginationResponse<List<LonaciTrxDTO>> customFindByDateBetweenAndOperateurIDAndTypeTransaction(
            String country, Date startDate, Date endDate, String operatorId, String typeTransaction, String codeService,
			String momoOperator, Double amount, String sortBy, String sortDir, int pageSize, int page
	) {
//		String sqlQuery = "SELECT t FROM LonaciTrx t ";
		String sqlQuery = "SELECT t, " +
				"       csm.operateurServiceMomo, " +
				"       cp.paymentFees, " +
				"       cp.cashinFees " +
				"FROM LonaciTrx t " +
				"LEFT JOIN CodeServiceMOMO csm ON t.codeService = csm.codeMomo AND csm.codeIso = :country " +
				"LEFT JOIN ComputeParameter cp ON t.operateurID = cp.operator AND t.country = cp.country " +
				"WHERE t.date BETWEEN :startDate AND :endDate";

		String aggSqlQuery = "SELECT COUNT(*), sum(t.montant) from lonaci_trx t WHERE t.date BETWEEN :startDate AND :endDate ";



		// Si le filtre opérateur est fourni, on recherche les codes services associés à cet opérateur
//		List<String> codeServicesForOperator = null;
//		if (StringUtils.hasText(operateurMomo)) {
//			List<CodeServiceMOMO> codeServiceList = codeServiceRepository.findByOperateurServiceMomo(operateurMomo, country);
//			if (!codeServiceList.isEmpty()) {
//				codeServicesForOperator = codeServiceList.stream()
//						.map(CodeServiceMOMO::getCodeMomo)
//						.collect(Collectors.toList());
//				// Filtrer les transactions dont le codeService figure dans la liste trouvée
//				sqlQuery += " AND t.codeService IN :codeServices";
//				aggSqlQuery += " AND t.code_service IN (:codeServices)";
//			}
//		}

		/*if (StringUtils.hasText(momoOperator)) {
			sqlQuery += " JOIN CodeServiceMOMO c WITH t.codeService = c.codeMomo ";
			aggSqlQuery += " JOIN code_service_momo c ON t.code_service = c.code_momo ";
		}*/

		if (StringUtils.hasText(momoOperator)) {
			// Filtre sur l'opérateur depuis CodeServiceMOMO
			sqlQuery += " AND LOWER(c.operateurServiceMomo) = LOWER(:operateurMomo) ";
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

		sqlQuery += " AND t.country = :country";
		aggSqlQuery += " AND t.country = :country";

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

/*		if (codeServicesForOperator != null) {
			query.setParameter("codeServices", codeServicesForOperator);
			aggQuery.setParameter("codeServices", codeServicesForOperator);
		}*/


		query.setParameter("country", country);
		aggQuery.setParameter("country", country);


		List<Object[]> aggResult = aggQuery.getResultList();
		if (page != -1) {
			query.setFirstResult(page * pageSize);
			query.setMaxResults(pageSize);
		}
		long totalsize = Long.parseLong((aggResult.get(0)[0]).toString());
		Double sum = (aggResult.get(0)[1]) != null ? Double.parseDouble((aggResult.get(0)[1]).toString()) : 0;

		/*// Récupérer la liste des transactions
		List<LonaciTrx> transactions = query.getResultList();

		// Mapping des entités LonaciTrx vers LonaciTrxDTO
		List<LonaciTrxDTO> dtoList = transactions.stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());*/

		List<Object[]> results = query.getResultList();

		List<LonaciTrxDTO> dtoList = results.stream()
				.map(result -> {
					LonaciTrx trx = (LonaciTrx) result[0];
					String operateurServiceMomo = (String) result[1];
					Double paymentFees = (Double) result[2];
					Double cashinFees = (Double) result[3];

					double commissionValue = 0;
					if (DEPOT_MOMO_TYPE_TRX.equalsIgnoreCase(trx.getTypeTransaction())) {
						commissionValue = trx.getMontant() * (paymentFees != null ? paymentFees : (CI_COUNTRY_CODE.equals(country) ? 0.04 : 0));
					} else if (RETRAIT_MOMO_TYPE_TRX.equalsIgnoreCase(trx.getTypeTransaction())) {
						commissionValue = trx.getMontant() * (cashinFees != null ? cashinFees : (CI_COUNTRY_CODE.equals(country) ? 0.04 : 0));
					}

					return LonaciTrxDTO.builder()
							.transactionId(trx.getTransactionId())
							.operateurID(trx.getOperateurID())
							.operateurLibelle(trx.getOperateurLibelle())
							.idFromPartner(trx.getIdFromPartner())
							.codeService(trx.getCodeService())
							.typeTransaction(trx.getTypeTransaction())
							.montant(trx.getMontant())
							.intouchCommission(commissionValue == 0 ? " -- " : this.formatLabelAmount(commissionValue))
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


	/*private LonaciTrxDTO mapToDTO(LonaciTrx trx) {
		LonaciTrxDTO dto = LonaciTrxDTO.builder()
				.transactionId(trx.getTransactionId())
				.operateurID(trx.getOperateurID())
				.operateurLibelle(trx.getOperateurLibelle())
				.idFromPartner(trx.getIdFromPartner())
				.codeService(trx.getCodeService())
				.typeTransaction(trx.getTypeTransaction())
				.montant(trx.getMontant())
				.intouchCommission(computeIntouchCommission(trx))
				.destinataire(trx.getDestinataire())
				.date(trx.getDate())
				.lonaciTransactionID(trx.getLonaciTransactionID())
				.country(trx.getCountry())
				.build();

		// Récupérer le CodeServiceMOMO correspondant à partir du code_service.
		// Ici, on utilise "country" comme codeIso pour filtrer.
		*//*Optional<CodeServiceMOMO> cs = codeServiceRepository.findByCodeMomo(dto.getCodeService(), trx.getCountry());
		cs.ifPresent(value -> dto.setOperateurServiceMomo(value.getOperateurServiceMomo()));

		return dto;*//*

		// Utiliser la méthode du repository qui renvoie une liste pour récupérer le codeServiceMOMO correspondant
		List<CodeServiceMOMO> codeServices = codeServiceRepository.findByCodeMomo(dto.getCodeService(), trx.getCountry());
		if (!codeServices.isEmpty()) {
			// On prend le premier élément trouvé (ou appliquez une autre logique si nécessaire)
			dto.setOperateurServiceMomo(codeServices.get(0).getOperateurServiceMomo());
		} else {
			dto.setOperateurServiceMomo(null);
		}

		return dto;
	}*/

	/*public String computeIntouchCommission(LonaciTrx trx) {
		double commissionValue = 0;

		String typeTx = (trx.getTypeTransaction() != null) ? trx.getTypeTransaction().toLowerCase() : "";

		// Vérifier que le type est soit "depot_momo" soit "retrait"
		if (DEPOT_MOMO_TYPE_TRX.equals(typeTx) || RETRAIT_MOMO_TYPE_TRX.equals(typeTx)) {
			// Récupérer le ComputeParameter pour l'opérateur et le pays
			List<ComputeParameter> paramList = computeParameterService.getParameterByOperatorAndCountry(trx.getOperateurID(), trx.getCountry());
			ComputeParameter param = (paramList != null && !paramList.isEmpty()) ? paramList.get(0) : null;

			if (param != null) {
				// Si type est "depot_momo", appliquer paymentFees, sinon cashinFees pour "retrait"
				commissionValue = DEPOT_MOMO_TYPE_TRX.equals(typeTx)
						? trx.getMontant() * param.getPaymentFees()
						: trx.getMontant() * param.getCashinFees();
			} else if (CI_COUNTRY_CODE.equals(trx.getCountry())) {
				// Taux par défaut pour la Côte d'Ivoire
				commissionValue = trx.getMontant() * 0.04;
			} else {
				log.error("No compute parameter found for operator {} and country {}", trx.getOperateurID(), trx.getCountry());
			}
		}

		// Si commissionValue est 0, retourner " -- ", sinon formater le montant.
		return commissionValue == 0 ? " -- " : this.formatLabelAmount(commissionValue);
	}*/


	public String formatLabelAmount(double amount) {

//		NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
//		nf.setGroupingUsed(true); // Active le séparateur de milliers (espace en France)
//		nf.setMaximumFractionDigits(2); // Limite aux 2 décimales
//		return nf.format(amount);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
		symbols.setDecimalSeparator('.');      // Remplacer la virgule par un point
		symbols.setGroupingSeparator(' ');       // Garder l'espace comme séparateur de milliers
		DecimalFormat df = new DecimalFormat("#,##0.##", symbols);
		return df.format(amount);
	}
}
