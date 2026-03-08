package sn.intouch.gu.lonaciapi.ejb.notification.services;

import lombok.extern.log4j.Log4j2;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TempTable;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Log4j2
@Stateless
public class TempTableServiceBean implements TempTableService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    @Override
    public boolean add(TempTable tempTable) {
        try {
            em.persist(tempTable);
            return true;
        } catch (Exception e) {
            log.error("An error occurred while saving temp table", e);
            return false;
        }
    }
}
