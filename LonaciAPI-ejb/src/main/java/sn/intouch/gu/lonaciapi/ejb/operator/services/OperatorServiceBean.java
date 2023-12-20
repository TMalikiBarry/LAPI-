package sn.intouch.gu.lonaciapi.ejb.operator.services;

import sn.intouch.gu.lonaciapi.ejb.operator.entities.Operator;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class OperatorServiceBean implements OperatorService {

	@PersistenceContext(unitName = "lonaciPU")
	EntityManager em;

	@Override
	public List<Operator> listerOperator(String networkCode) {
		List<Operator> Operators = new ArrayList<Operator>();
		String jpql = "from Operator a WHERE a.supprime = false ";
		if (networkCode != null)
			jpql += "AND a.code_reseau=:networkCode";
		try {
			Query query = em.createQuery(jpql);
			if (networkCode != null)
				query.setParameter("networkCode", networkCode);
			Operators= query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Operators;
	}

		@Override
	public Operator ajouterOperator(Operator Operator) {
		return em.merge(Operator);
	}

	@Override
	public Operator supprimerOperator(Operator Operator) {
		
		Operator.setSupprime(true);
		return em.merge(Operator);
	}

	@Override
	public Operator modifierOperator(Operator Operator) {
		
		return em.merge(Operator);
	}

	@Override
	public Operator find(Operator u) {
		return em.find(Operator.class, u.getOperateur_id());
	}

	@Override
	public List<Operator> filterOp(String code) {
		// TODO Auto-generated method stub
		List<Operator> Operators = new ArrayList<Operator>();
		try {
			String sql = "SELECT p FROM Operator p where p.statut = 'ACTIF' and p.supprime = false";

			if (code != null) {
				sql += " and p.Operator_id LIKE CONCAT('%',:code,'%')";
			}
			/*if (codenetwork != null) {
				sql += " and p.networkgroup_id.network_group_code = :codenetwork ";
			}*/

			Query query = em.createQuery(sql);

			if (code != null) {
				query.setParameter("code", code);
			}

			/*if (codenetwork != null) {
				query.setParameter("codenetwork", codenetwork);
			}*/

			Operators = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Operators;

	}

	@Override
	public Operator findByLibelle(String libelle){
		Operator Operator = null;
		try {
			Query query = em.createQuery("from Operator n where n.supprime = false and n.operateur_libelle = :libelle");
			Operator = (Operator) query.setParameter("libelle", libelle).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Operator;
	}

	@Override
	public Operator findByOperatorID(String opid){
		Operator Operator = null;
		try {
			Query query = em.createQuery("from Operator n where n.supprime = false and n.operateur_id = :opid");
			Operator = (Operator) query.setParameter("opid", opid).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Operator;
	}

	@Override
	public List<Operator> listerOperators() {
		try {
			Query query = em.createQuery("from Operator n where n.supprime = false");
			return (List<Operator>) query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
}
