package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.Country;

import javax.ejb.Local;

@Local
public interface CountryService {

    Iterable<Country> findAll();
    Country save(Country country);
    Country getByCode(String code);
    Country delete(Country country);
}
