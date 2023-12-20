package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;


@Stateless
public class TypeTrxServiceBean implements TypeTrxService{
	
	@PersistenceContext(unitName="lonaciPU")
	EntityManager em;

	@Override
	public  TypeTrx getTypeTrxById(Long id) {
		TypeTrx type = em.find(TypeTrx.class, id);
		
		if(type==null) 
			throw new RuntimeException("No Transaction Type was found");
		return type;
	}
	
	@Override
	public TypeTrx getTypeTrxByCode(String code) {
		TypeTrx type = null;

		try {
			Query query = em.createQuery("from TypeTrx n where n.supprime = false and n.code = :code");
			type = (TypeTrx) query.setParameter("code", code).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return type;
	}
	
	
	@Override
	public TypeTrx saveTypeTrx(TypeTrx type) {
		return em.merge(type);
	}
	
	
	
	@Override
	public TypeTrx updateTypeTrx(TypeTrx typeTrx) {
		return em.merge(typeTrx);
	}
	
	@Override
	public List<TypeTrx> findAll(){
		List<TypeTrx> types = new ArrayList<TypeTrx>();
		try {
		Query query = em.createQuery("FROM TypeTrx a where a.supprime = false ");
		types = query.getResultList();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return types;
	}
	
	@Override
	public List<TypeTrx> filterTypeTrx(String code){
		
		List<TypeTrx> types = new ArrayList<TypeTrx>();
		try {
			String sql = "SELECT p FROM TypeTrx p where p.typeId IS NOT NULL AND p.supprime = false";
			if(code != null) {
				sql+=" AND p.code = :code";
			}
			
			Query query = em.createQuery(sql);
			
			if(code != null) {
				query.setParameter("code", code);
			}
			
			types = query.getResultList();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return types;
	}


}
