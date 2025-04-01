package sn.intouch.gu.lonaciapi.ejb.notification.services;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.StringUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.OperatorRepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

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
        if (!StringUtils.hasText(opid)) return null;
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
	public List<Operator> findByOperatorIds(List<String> operatorIds) {
		return operatorRepository.findByOperatorIdIn(operatorIds);
	}

	@Override
	public Operator delete(Operator operator) {
		operator.setSupprime(true);
		return em.merge(operator);
	}

	@Override
	public Iterable<Operator> getAll(Set<String> operators) {
		if (operators != null) {
			return operatorRepository.findByOperatorIdInAndSupprime(operators, false);
		}
		return operatorRepository.findBySupprime(false);
	}

	@Override
	public Iterable<Operator> findByCountry(String country, Set<String> operators) {
		if (operators != null) {
			return operatorRepository.findByCountryAndOperatorIdInAndSupprime(country, operators, false);
		}
		return operatorRepository.findByCountry(country);
	}
}
