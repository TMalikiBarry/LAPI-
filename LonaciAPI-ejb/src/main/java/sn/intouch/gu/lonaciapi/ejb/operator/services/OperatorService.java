package sn.intouch.gu.lonaciapi.ejb.operator.services;

import sn.intouch.gu.lonaciapi.ejb.operator.entities.Operator;

import javax.ejb.Local;
import javax.ejb.Remote;
import java.util.List;

@Local
public interface OperatorService {

	List<Operator> listerOperator(String networkCode);
	Operator ajouterOperator(Operator op);
	Operator supprimerOperator(Operator op);
	Operator modifierOperator(Operator op);
	Operator find(Operator u);
	List<Operator> filterOp(String codeMarchand );
	Operator findByLibelle(String libelle);
	Operator findByOperatorID(String opid);
	List<Operator> listerOperators();
}
