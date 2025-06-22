package sn.intouch.gu.lonaciapi.ejb.notification.services;

import lombok.extern.log4j.Log4j2;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;


@Log4j2
@Stateless
public class RevenueServiceBean implements RevenueService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    @Override
    public void add(Revenue revenue) {
        em.persist(revenue);
    }

    @Override
    public Revenue update(Revenue revenue) {
        return em.merge(revenue);
    }


    @Override
    public List<Object[]> sumByDateAndOperator(Date startDate, Date endDate, String operator, String country) {
        String sql = "SELECT SUM(grossGamingProduct), SUM(integratorRemuneration), SUM(revenue), SUM(royalties), SUM(payin), " +
                " SUM(payout), SUM(mises), SUM(gain), SUM(bonus) , SUM(withholding)  FROM revenue WHERE date BETWEEN :startDate AND :endDate ";

        if (operator != null)
            sql += " AND operator = :operator";
        if (country != null)
            sql += " AND country = :country";

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        if (operator != null)
            query.setParameter("operator", operator);
        if (country != null)
            query.setParameter("country", country);

        return query.getResultList();
    }

    @Override
    public List<Revenue> curveByDateAndOperator(Date startDate, Date endDate, String operator, String country) {
        String sql = "SELECT new sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue(DATE(date), SUM(grossGamingProduct), " +
                "SUM(integratorRemuneration), SUM(revenue), SUM(royalties), SUM(payin), SUM(payout), SUM(mises), SUM(gain), SUM(bonus), SUM(withholding))" +
                " FROM Revenue WHERE date BETWEEN :startDate AND :endDate ";
        if (operator != null)
            sql += " AND operator = :operator";
        if (country != null)
            sql += " AND country = :country";

        sql += " GROUP BY DATE(date) ORDER BY DATE(date) ASC";
        Query query = em.createQuery(sql, Revenue.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        if (operator != null)
            query.setParameter("operator", operator);
        if (country != null)
            query.setParameter("country", country);

        return query.getResultList();
    }
}
