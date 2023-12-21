package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;

import javax.ejb.Local;

@Local
public interface OperatorService {

	Operator findByOperatorID(String opid);
	Operator findByID(Long id);
	Operator save(Operator operator);
	Operator delete(Operator operator);

	Iterable<Operator> getAll();
}
