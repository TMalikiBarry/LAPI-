package sn.intouch.gu.lonaciapi.ejb.notification.services;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.RevenueRepository;

import javax.annotation.PostConstruct;
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

    private RevenueRepository revenueRepository;
    @PostConstruct
    private void init() {
        RepositoryFactorySupport factorySupport = new JpaRepositoryFactory(em);
        this.revenueRepository = factorySupport.getRepository(RevenueRepository.class);
    }

    @Override
    public void add(Revenue revenue) {
        em.persist(revenue);
    }

    @Override
    public Revenue update(Revenue revenue) {
        return em.merge(revenue);
    }

    @Override
    public Iterable<Revenue> findByDateAndOperator(Date start, Date end, String operator) {
        if (operator != null)
            return revenueRepository.findByDateBetweenAndOperatorOrderByDateAsc(start, end, operator);

        return revenueRepository.findByDateBetween(start, end);
    }

    @Override
    public List<Object[]> sumByDateAndOperator(Date startDate, Date endDate, String operator) {
        String sql = "SELECT SUM(grossGamingProduct), SUM(integratorRemuneration), SUM(revenue), SUM(royalties), SUM(payin), " +
                " SUM(payout), SUM(mises), SUM(gain), SUM(bonus) FROM revenue WHERE date BETWEEN :startDate AND :endDate ";

        if (operator != null)
            sql += " AND operator = :operator";

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        if (operator != null)
            query.setParameter("operator", operator);

        return query.getResultList();
    }

    @Override
    public List<Revenue> curveByDateAndOperator(Date startDate, Date endDate, String operator) {
        String sql = "SELECT new sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue(DATE(date), SUM(grossGamingProduct), " +
                "SUM(integratorRemuneration), SUM(revenue), SUM(royalties), SUM(payin), SUM(payout), SUM(mises), SUM(gain), SUM(bonus))" +
                " FROM Revenue WHERE date BETWEEN :startDate AND :endDate ";
        if (operator != null)
            sql += " AND operator = :operator";
        sql += " GROUP BY DATE(date) ORDER BY DATE(date) ASC";
        Query query = em.createQuery(sql, Revenue.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        if (operator != null)
            query.setParameter("operator", operator);

        return query.getResultList();
    }
}
