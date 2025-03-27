package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import sn.intouch.gu.lonaciapi.config.BadRequestException;
import sn.intouch.gu.lonaciapi.config.ResourceAlreadyExistsException;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.ComputeParameter;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;


@Stateless
@Log4j2
public class ComputeParameterServiceBean implements ComputeParameterService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    @Override
    public ComputeParameter getById(Long id) {
        if (id == null) {
            throw new BadRequestException("L'ID est requis pour la mise à jour.");
        }
        return em.find(ComputeParameter.class, id);
    }

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

    @Override
    public List<ComputeParameter> getParameterByOperatorAndCountry(String operator, String country) {
        List<ComputeParameter> parameters = new ArrayList<>();
        try {
            // Construire la requête dynamiquement
            String jpql = "SELECT p FROM ComputeParameter p WHERE p.isActive = true";
            boolean hasOperator = operator != null && !operator.trim().isEmpty();
            boolean hasCountry = country != null && !country.trim().isEmpty();

            if (hasOperator) {
                jpql += " AND p.operator = :operator";
            }
            if (hasCountry) {
                jpql += " AND p.country = :country";
            }

            Query query = em.createQuery(jpql);
            if (hasOperator) {
                query.setParameter("operator", operator);
            }
            if (hasCountry) {
                query.setParameter("country", country);
            }

            parameters = query.getResultList();
        } catch (Exception e) {
            log.error("##### ComputeParameterServiceBean #### Error getting compute parameter for operator {} and country {}: {}",
                    operator, country, e.getMessage());
        }
        return parameters;
    }

    @Override
    public ComputeParameter saveComputeParameter(ComputeParameter parameter) {
        // Vérification des champs obligatoires
        if (!StringUtils.hasText(parameter.getCountry()) || !StringUtils.hasText(parameter.getOperator())
                || parameter.getCashinFees() == null || parameter.getPaymentFees() == null) {
            throw new BadRequestException("Les champs country, operator, cashinFees et paymentFees sont obligatoires.");
        }
        // Si un ID est fourni, vérifier s'il existe déjà en base
        if (parameter.getId() != null) {
            ComputeParameter existing = em.find(ComputeParameter.class, parameter.getId());
            if (existing != null) {
                throw new ResourceAlreadyExistsException("ComputeParameter avec l'ID " + parameter.getId() + " existe déjà.");
            }
        }
        try {
            em.persist(parameter);
            em.flush();
            log.info("ComputeParameter saved successfully: {}", parameter);
            return parameter;
        } catch (Exception e) {
            log.error("Error saving ComputeParameter: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public ComputeParameter updateComputeParameter(ComputeParameter parameter) {
        // L'ID est requis pour la mise à jour
        if (parameter.getId() == null) {
            throw new BadRequestException("L'ID est requis pour la mise à jour.");
        }
        // Vérification des champs obligatoires
        if (!StringUtils.hasText(parameter.getCountry()) || !StringUtils.hasText(parameter.getOperator())
                || parameter.getCashinFees() == null || parameter.getPaymentFees() == null) {
            throw new BadRequestException("Les champs country, operator, cashinFees et paymentFees sont obligatoires.");
        }
        ComputeParameter existing = em.find(ComputeParameter.class, parameter.getId());
        if (existing == null) {
            throw new EntityNotFoundException("ComputeParameter avec l'ID " + parameter.getId() + " est introuvable.");
        }
        try {
            ComputeParameter updated = em.merge(parameter);
            em.flush();
            log.info("ComputeParameter updated successfully: {}", updated);
            return updated;
        } catch (Exception e) {
            log.error("Error updating ComputeParameter: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void deactivateComputeParameter(Long id) {
        ComputeParameter parameter = em.find(ComputeParameter.class, id);
        if (parameter == null) {
            throw new EntityNotFoundException("ComputeParameter avec l'ID " + id + " est introuvable.");
        }
        parameter.setIsActive(false);
        try {
            em.merge(parameter);
            em.flush();
            log.info("ComputeParameter deactivated successfully: {}", parameter);
        } catch (Exception e) {
            log.error("Error deactivating ComputeParameter: {}", e.getMessage());
            throw e;
        }
    }
}
