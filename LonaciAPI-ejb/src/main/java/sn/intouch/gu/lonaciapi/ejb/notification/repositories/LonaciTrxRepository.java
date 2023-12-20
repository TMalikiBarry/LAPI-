/*
package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Service;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrx;

import java.util.Date;

@Service
public interface LonaciTrxRepository extends JpaRepository<LonaciTrx, Long>, QueryByExampleExecutor<LonaciTrx> {
    Page<LonaciTrx> findByDateBetween(Date startDate, Date endDate, Pageable pageable);
    Page<LonaciTrx> findByDateBetweenAndOperateurID(Date startDate, Date endDate, String operatorId, Pageable pageable);
    Page<LonaciTrx> findByDateBetweenAndOperateurIDAndTypeTransaction(Date startDate, Date endDate, String operatorId, String type, Pageable pageable);
}
*/
