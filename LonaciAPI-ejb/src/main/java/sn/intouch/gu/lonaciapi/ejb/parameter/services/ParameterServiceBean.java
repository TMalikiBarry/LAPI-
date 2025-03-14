package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;


@Stateless
public class ParameterServiceBean implements ParameterService {

    private static final Logger log = LoggerFactory.getLogger(ParameterServiceBean.class);
    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    @Override
    public String getStringValue(String code) {
        try {
            Query query = em.createQuery("SELECT p FROM Parameter p WHERE p.prmCode = :code and p.prmStatut = 1", Parameter.class);
            query.setParameter("code", code);
            Parameter p = (Parameter) query.getSingleResult();
            return p.getPrmStringValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public Parameter getParameterByCode(String code) {
        Parameter parameter = null;
        try {
            Query query = em.createQuery("SELECT p FROM Parameter p WHERE p.prmCode = :code and p.prmStatut = 1  ");
            query.setParameter("code", code);
            parameter = (Parameter) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("Pas de parametre de code " + code);
            log.error("##### getParameterByCode ERREUR  " + e);
            e.printStackTrace();
        }
        return parameter;
    }

    @Override
    public void saveParameter(Parameter parameter) {
        em.merge(parameter);
    }

    @Override
    public List<Parameter> getAll() {
        Query query = em.createQuery("SELECT p FROM Parameter p WHERE p.prmStatut = 1", Parameter.class);
        return query.getResultList();
    }
}
