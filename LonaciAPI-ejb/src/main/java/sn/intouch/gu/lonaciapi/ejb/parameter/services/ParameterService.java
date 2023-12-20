package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parametre;

import javax.ejb.Local;

@Local
public interface ParameterService {

    public int nextValue(String code);

    public int getValue(String code);

    public Parametre getParameterByCode(String code);

    public int setValue(String code, String value);

    public void saveParameter(Parametre parametre);

    public void createParameter(Parametre parametre);

    String getStringValue(String code);

}
