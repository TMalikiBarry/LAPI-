package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import sn.intouch.gu.lonaciapi.ejb.parameter.entities.ComputeParameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ComputeParameterService {

    ComputeParameter getParameterByOperator(String code);
}
