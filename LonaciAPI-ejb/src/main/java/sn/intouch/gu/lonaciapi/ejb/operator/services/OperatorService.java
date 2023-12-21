package sn.intouch.gu.lonaciapi.ejb.operator.services;

import sn.intouch.gu.lonaciapi.ejb.operator.entities.Operator;

import javax.ejb.Local;
import javax.ejb.Remote;
import java.util.List;

@Local
public interface OperatorService {

	Operator findByOperatorID(String opid);
	Operator findByID(Long id);
	Operator save(Operator operator);
	Operator delete(Operator operator);

	Iterable<Operator> getAll();
}
