package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Service;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;

import java.util.Date;

@Service
public interface RevenueRepository extends JpaRepository<Revenue, Long>, QueryByExampleExecutor<Revenue> {
    Iterable<Revenue> findByDateBetween(Date start, Date end);
    Iterable<Revenue> findByDateBetweenAndOperatorOrderByDateAsc(Date start, Date end, String operator);
}
