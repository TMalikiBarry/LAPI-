package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;

import javax.ejb.Local;
import java.util.List;

@Local
public interface OperatorService {

	Operator findByOperatorID(String opid);
	Operator findByID(Long id);

	public List<Operator> findByOperatorIds(List<String> operatorIds);

	Operator save(Operator operator);
	Operator delete(Operator operator);

	Iterable<Operator> getAll();
	Iterable<Operator> findByCountry(String country);
}
