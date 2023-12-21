package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Service;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;

import java.util.Optional;

@Service
public interface TypeTrxRepository extends JpaRepository<TypeTrx, Long>, QueryByExampleExecutor<TypeTrx> {
    Optional<TypeTrx> findByCodeAndDeleted(String code, Boolean deleted);
    Iterable<TypeTrx> findByDeleted(Boolean deleted);
}
