package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ParameterService {

    Parameter getParameterByCode(String code);

    void saveParameter(Parameter parameter);

    String getStringValue(String code);

    List<Parameter> getAll();
}
