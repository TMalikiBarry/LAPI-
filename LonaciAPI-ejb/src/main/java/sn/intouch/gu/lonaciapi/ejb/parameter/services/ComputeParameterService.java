package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import sn.intouch.gu.lonaciapi.ejb.parameter.entities.ComputeParameter;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ComputeParameterService {

    ComputeParameter getById(Long id);
    ComputeParameter getParameterByOperator(String code);

    List<ComputeParameter> getParameterByOperatorAndCountry(String operator, String country);

    ComputeParameter saveComputeParameter(ComputeParameter parameter);

    ComputeParameter updateComputeParameter(ComputeParameter parameter);

    void deactivateComputeParameter(Long id);
}
