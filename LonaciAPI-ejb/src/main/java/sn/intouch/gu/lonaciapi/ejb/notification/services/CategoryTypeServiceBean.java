package sn.intouch.gu.lonaciapi.ejb.notification.services;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CategoryType;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.CategoryTypeRepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class CategoryTypeServiceBean implements CategoryTypeService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    private CategoryTypeRepository typeTrxRepository;
    @PostConstruct
    private void init() {
        RepositoryFactorySupport factorySupport = new JpaRepositoryFactory(em);
        this.typeTrxRepository = factorySupport.getRepository(CategoryTypeRepository.class);
    }

    @Override
    public Iterable<CategoryType> findAll() {
        return typeTrxRepository.findByDeleted(false);
    }


    @Override
    public CategoryType save(CategoryType typeTrx) {
        return em.merge(typeTrx);
    }

    @Override
    public CategoryType getByCode(String code) {
        if (code == null) return null;
        return typeTrxRepository.findByCodeAndDeleted(code, false).orElseGet(() -> null);
    }

    @Override
    public CategoryType delete(CategoryType typeTrx) {
        typeTrx.setDeleted(true);
        return em.merge(typeTrx);
    }
}
