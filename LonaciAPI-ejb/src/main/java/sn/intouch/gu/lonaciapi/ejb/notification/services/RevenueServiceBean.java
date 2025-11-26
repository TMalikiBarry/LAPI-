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

    @Override
    public void updateRevenueRow(Revenue revenueEntity) {
        try {
            String jpql = "UPDATE Revenue r SET " +
                    "r.grossGamingProduct = :ggp, " +
                    "r.integratorRemuneration = :integratorRem, " +
                    "r.revenue = :revenue, " +
                    "r.royalties = :royalties, " +
                    "r.payin = :payin, " +
                    "r.payout = :payout, " +
                    "r.mises = :mises, " +
                    "r.gain = :gain, " +
                    "r.bonus = :bonus, " +
                    "r.withholding = :withholding, " +
                    "r.country = :country " +
                    "WHERE r.operator = :operator " +
                    "AND r.date = :date " +
                    "AND r.endDate = :endDate";

            em.createQuery(jpql)
                    .setParameter("ggp", revenueEntity.getGrossGamingProduct())
                    .setParameter("integratorRem", revenueEntity.getIntegratorRemuneration())
                    .setParameter("revenue", revenueEntity.getRevenue())
                    .setParameter("royalties", revenueEntity.getRoyalties())
                    .setParameter("payin", revenueEntity.getPayin())
                    .setParameter("payout", revenueEntity.getPayout())
                    .setParameter("mises", revenueEntity.getMises())
                    .setParameter("gain", revenueEntity.getGain())
                    .setParameter("bonus", revenueEntity.getBonus())
                    .setParameter("withholding", revenueEntity.getWithholding())
                    .setParameter("country", revenueEntity.getCountry())
                    .setParameter("operator", revenueEntity.getOperator())
                    .setParameter("date", revenueEntity.getDate())
                    .setParameter("endDate", revenueEntity.getEndDate())
                    .executeUpdate();
            em.clear();
        } catch (Exception e) {
            log.error("Error while updating revenue row", e);
        }
    }

}
