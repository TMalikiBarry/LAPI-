package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;


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
                " SUM(payout), SUM(mises), SUM(gain), SUM(bonus) FROM revenue WHERE date BETWEEN :startDate AND :endDate ";

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

    /**
     * Calcule, pour le pays BF uniquement, la somme des retenues
     * (15 % du total des gains de chaque opérateur qui a au moins 500 000 de gains)
     * en une seule requête.
     *
     * @param startDate début de la période (inclus)
     * @param endDate   fin de la période   (inclus)
     * @param country   code ISO du pays ; doit être "BF"
     * @return la retenue totale (= Σ [ total_gain_opérateur >= 500k ? total_gain_opérateur * 0.15 : 0 ]).
     */
    @Override
    public Double sumWithholdingForAllOperatorsBF(Date startDate,
                                                  Date endDate,
                                                  String country) {
        if (country == null || !"BF".equalsIgnoreCase(country.trim())) {
            // Si le pays n'est pas "BF", on ne calcule rien.
            return 0D;
        }

        String sql =
                "SELECT COALESCE(SUM( " +
                        "         CASE " +
                        "           WHEN sub.total_gain >= 500000 THEN sub.total_gain * 0.15 " +
                        "           ELSE 0 " +
                        "         END " +
                        "       ), 0) AS total_withholding " +
                        "FROM ( " +
                        "  SELECT operator, COALESCE(SUM(gain), 0) AS total_gain " +
                        "  FROM revenue " +
                        "  WHERE date BETWEEN :startDate AND :endDate " +
                        "    AND country = :country " +
                        "  GROUP BY operator " +
                        ") AS sub";

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("country", country);

        // getSingleResult() retourne un objet unique (SUM du CASE ci-dessus)
        Number resultNum = (Number) query.getSingleResult();
        return (resultNum != null) ? resultNum.doubleValue() : 0D;
    }

    @Override
    public Double sumWithholdingByDateAndOperator(Date startDate,
                                                  Date endDate,
                                                  String operator,
                                                  String country) {
        if (operator == null || operator.isEmpty()) {
            throw new IllegalArgumentException("Le paramètre 'operator' est obligatoire pour calculer la retenue.");
        }
        if (country == null || country.isEmpty()) {
            throw new IllegalArgumentException("Le paramètre 'country' est obligatoire pour calculer la retenue.");
        }

        if (!"BF".equalsIgnoreCase(country.trim())) {
            return 0D;
        }

        String sql = "SELECT COALESCE(SUM(gain), 0) "
                + "FROM revenue "
                + "WHERE date BETWEEN :startDate AND :endDate "
                + "  AND operator = :operator "
                + "  AND country  = :country";

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("operator", operator)
                .setParameter("country", country);

        Number totalGainNum = (Number) query.getSingleResult();
        double totalGain = (totalGainNum != null) ? totalGainNum.doubleValue() : 0D;

        if (totalGain >= 500_000D) {
            return totalGain * 0.15;
        }
        return 0D;
    }

    @Override
    public List<Revenue> curveByDateAndOperator(Date startDate, Date endDate, String operator, String country) {
        String sql = "SELECT new sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue(DATE(date), SUM(grossGamingProduct), " +
                "SUM(integratorRemuneration), SUM(revenue), SUM(royalties), SUM(payin), SUM(payout), SUM(mises), SUM(gain), SUM(bonus))" +
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
