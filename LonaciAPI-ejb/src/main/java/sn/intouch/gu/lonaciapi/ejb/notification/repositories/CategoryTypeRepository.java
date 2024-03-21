package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Service;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CategoryType;

import java.util.Optional;

@Service
public interface CategoryTypeRepository extends JpaRepository<CategoryType, Long>, QueryByExampleExecutor<CategoryType> {
    Optional<CategoryType> findByCodeAndDeleted(String code, Boolean deleted);
    Iterable<CategoryType> findByDeleted(Boolean deleted);
}
