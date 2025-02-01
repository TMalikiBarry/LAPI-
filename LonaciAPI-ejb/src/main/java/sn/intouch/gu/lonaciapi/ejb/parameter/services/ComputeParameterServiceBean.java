package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import lombok.extern.log4j.Log4j2;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.ComputeParameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;


@Stateless
@Log4j2
public class ComputeParameterServiceBean implements ComputeParameterService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    @Override
    public ComputeParameter getParameterByOperator(String code) {
        ComputeParameter parameter = null;
        try {
            Query query = em.createQuery("SELECT p FROM ComputeParameter p WHERE p.operator = :code and p.isActive = true ");
            query.setParameter("code", code);
            parameter = (ComputeParameter) query.getSingleResult();
        } catch (Exception e) {
            log.error("An error occurred while getting compute parameter : {}", e.getMessage());
        }
        return parameter;
    }
}
