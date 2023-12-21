package sn.intouch.gu.lonaciapi.ejb.operator.services;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import sn.intouch.gu.lonaciapi.ejb.operator.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.operator.repositories.OperatorRepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class OperatorServiceBean implements OperatorService {

	@PersistenceContext(unitName = "lonaciPU")
	EntityManager em;

	private OperatorRepository operatorRepository;
	@PostConstruct
	private void init() {
		RepositoryFactorySupport factorySupport = new JpaRepositoryFactory(em);
		this.operatorRepository = factorySupport.getRepository(OperatorRepository.class);
	}
	
	@Override
	public Operator findByOperatorID(String opid){
		return operatorRepository.findByOperatorIdAndSupprime(opid, false).orElseGet(() -> null);
	}

	@Override
	public Operator save(Operator operator) {
		return em.merge(operator);
	}

	@Override
	public Operator findByID(Long id) {
		try {
			return operatorRepository.getById(id);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Operator delete(Operator operator) {
		operator.setSupprime(true);
		return em.merge(operator);
	}

	@Override
	public Iterable<Operator> getAll() {
		return operatorRepository.findBySupprime(false);
	}
}
