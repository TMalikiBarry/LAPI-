package sn.intouch.gu.lonaciapi.ejb.parameter.services;

import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parametre;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


@Stateless
public class ParameterServiceBean implements ParameterService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    @Override
    public int nextValue(String code) {

        Query query = em.createQuery("SELECT p FROM Parametre p WHERE p.prmCode = :code and p.prmStatut = 1  ");
        query.setParameter("code", code);
        Parametre p = (Parametre) query.getSingleResult();

        int i = p.getPrmValue();

        p.setPrmValue(i + 1);
        em.merge(p);

        return i;

    }

    @Override
    public int getValue(String code) {
        Query query = em.createQuery("SELECT p FROM Parametre p WHERE p.prmCode = :code and p.prmStatut = 1  ");
        query.setParameter("code", code);
        Parametre p = (Parametre) query.getSingleResult();

        int i = p.getPrmValue();


        return i;
    }

    @Override
    public String getStringValue(String code) {
        try {
            Query query = em.createQuery("SELECT p FROM Parametre p WHERE p.prmCode = :code and p.prmStatut = 1", Parametre.class);
            query.setParameter("code", code);
            Parametre p = (Parametre) query.getSingleResult();
            return p.getPrmStringValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public Parametre getParameterByCode(String code) {
        Parametre parametre = null;
        try {
            Query query = em.createQuery("SELECT p FROM Parametre p WHERE p.prmCode = :code and p.prmStatut = 1  ");
            query.setParameter("code", code);
            parametre = (Parametre) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("Pas de parametre de code " + code);
            e.printStackTrace();
        }
        return parametre;
    }

    @Override
    public int setValue(String code, String value) {

        Query query = em.createQuery("SELECT p FROM Parametre p WHERE p.prmCode = :code and p.prmStatut = 1  ");
        query.setParameter("code", code);
        Parametre p = (Parametre) query.getSingleResult();


        p.setPrmStringValue(value);
        em.merge(p);

        return 1;

    }

    @Override
    public void saveParameter(Parametre parametre) {
        em.merge(parametre);
    }

    @Override
    public void createParameter(Parametre parametre) {
        em.persist(parametre);
    }
}
