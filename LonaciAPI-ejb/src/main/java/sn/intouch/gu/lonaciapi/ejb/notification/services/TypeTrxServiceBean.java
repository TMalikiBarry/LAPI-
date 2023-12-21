package sn.intouch.gu.lonaciapi.ejb.notification.services;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.TypeTrxRepository;
import sn.intouch.gu.lonaciapi.ejb.operator.repositories.OperatorRepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;


@Stateless
public class TypeTrxServiceBean implements TypeTrxService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    private TypeTrxRepository typeTrxRepository;
    @PostConstruct
    private void init() {
        RepositoryFactorySupport factorySupport = new JpaRepositoryFactory(em);
        this.typeTrxRepository = factorySupport.getRepository(TypeTrxRepository.class);
    }

    @Override
    public Iterable<TypeTrx> findAll() {
        return typeTrxRepository.findByDeleted(false);
    }


    @Override
    public TypeTrx save(TypeTrx typeTrx) {
        return em.merge(typeTrx);
    }

    @Override
    public TypeTrx getByCode(String code) {
        return typeTrxRepository.findByCodeAndDeleted(code, false).orElseGet(() -> null);
    }

    @Override
    public TypeTrx delete(TypeTrx typeTrx) {
        typeTrx.setDeleted(true);
        return em.merge(typeTrx);
    }
}
