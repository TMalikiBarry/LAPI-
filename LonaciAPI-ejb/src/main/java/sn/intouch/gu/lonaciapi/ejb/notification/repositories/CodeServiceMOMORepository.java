package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;

import java.util.List;
import java.util.Optional;


@Repository
public interface CodeServiceMOMORepository extends JpaRepository<CodeServiceMOMO, Long>, QueryByExampleExecutor<CodeServiceMOMO> {

    Optional<CodeServiceMOMO> findByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
    // Retrouve l'entité par son code unique
    List<CodeServiceMOMO> findByCodeMomo(String codeServiceMomo);

    // Retrouve la liste de toutes les entités associées à un opérateur
    List<CodeServiceMOMO> findByOperateurServiceMomo(String operateurServiceMomo);

    // Vérifie l'existence d'une association spécifique entre un code et un opérateur
    boolean existsByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
}
