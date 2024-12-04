package sn.intouch.gu.lonaciapi.ejb.notification.services;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Country;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.CountryRepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class CountryServiceBean implements CountryService{

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    private CountryRepository countryRepository;

    @PostConstruct
    private void init() {
        RepositoryFactorySupport factorySupport = new JpaRepositoryFactory(em);
        this.countryRepository = factorySupport.getRepository(CountryRepository.class);
    }
    @Override
    public Iterable<Country> findAll() {
        return countryRepository.findByDeleted(false);
    }

    @Override
    public Country save(Country country) {
        return em.merge(country);
    }

    @Override
    public Country getByCode(String code) {
        if(code == null) return null;
        return countryRepository.findByCodeAndDeletedFalse(code).orElseGet(() -> null);
    }

    @Override
    public Country delete(Country country) {
        country.setDeleted(true);
        return em.merge(country);
    }
}
