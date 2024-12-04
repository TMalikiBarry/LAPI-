package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Country;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> , QueryByExampleExecutor<Country> {
    Optional<Country> findByCodeAndDeletedFalse(String code);
    Iterable<Country> findByDeleted(Boolean deleted);
}
