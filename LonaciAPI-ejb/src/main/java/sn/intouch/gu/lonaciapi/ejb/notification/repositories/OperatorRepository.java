package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Service;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;

import java.util.Optional;

@Service
public interface OperatorRepository extends JpaRepository<Operator, Long>, QueryByExampleExecutor<Operator> {
    Optional<Operator> findByOperatorIdAndSupprime(String operatorID, Boolean deleted);
    Iterable<Operator> findBySupprime(Boolean deleted);
    Iterable<Operator> findByCountry(String country);
}
