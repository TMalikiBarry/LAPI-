package sn.intouch.gu.lonaciapi.ejb.notification.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface CodeServiceMOMORepository extends JpaRepository<CodeServiceMOMO, Long>, QueryByExampleExecutor<CodeServiceMOMO> {

    // Méthode générique non paginée
    @Query("SELECT c FROM CodeServiceMOMO c " +
            "WHERE (:codeMomo IS NULL OR c.codeMomo = :codeMomo) " +
            "  AND (:operateurServiceMomo IS NULL OR c.operateurServiceMomo = :operateurServiceMomo) " +
            "  AND (:serviceNom IS NULL OR c.serviceNom = :serviceNom) " +
            "  AND (:type IS NULL OR c.type = :type) " +
//            "  AND ((:startDate IS NULL AND :endDate IS NULL) OR (c.creationDate BETWEEN :startDate AND :endDate))" +
            "  AND (:startDate IS NULL OR c.creationDate >= :startDate) " +
            "  AND (:endDate IS NULL OR c.creationDate <= :endDate)" +
            " ORDER BY c.creationDate DESC")
    List<CodeServiceMOMO> findByOptionalParams(
            @Param("codeMomo") String codeMomo,
            @Param("operateurServiceMomo") String operateurServiceMomo,
            @Param("serviceNom") String serviceNom,
            @Param("type") String type,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    // Méthode générique paginée
    @Query("SELECT c FROM CodeServiceMOMO c " +
            "WHERE (:codeMomo IS NULL OR c.codeMomo = :codeMomo) " +
            "  AND (:operateurServiceMomo IS NULL OR c.operateurServiceMomo = :operateurServiceMomo) " +
            "  AND (:serviceNom IS NULL OR c.serviceNom = :serviceNom) " +
            "  AND (:type IS NULL OR c.type = :type) " +
//            "  AND ((:startDate IS NULL AND :endDate IS NULL) OR (c.creationDate BETWEEN :startDate AND :endDate))" +
            "  AND (:startDate IS NULL OR c.creationDate >= :startDate) " +
            "  AND (:endDate IS NULL OR c.creationDate <= :endDate)" +
            "ORDER BY c.creationDate DESC")
    Page<CodeServiceMOMO> findByOptionalParamsPaged(
            @Param("codeMomo") String codeMomo,
            @Param("operateurServiceMomo") String operateurServiceMomo,
            @Param("serviceNom") String serviceNom,
            @Param("type") String type,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable);

    Optional<CodeServiceMOMO> findByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
    // Retrouve l'entité par son code unique
    List<CodeServiceMOMO> findByCodeMomo(String codeServiceMomo);

    // Retrouve la liste de toutes les entités associées à un opérateur
    List<CodeServiceMOMO> findByOperateurServiceMomo(String operateurServiceMomo);

    // Vérifie l'existence d'une association spécifique entre un code et un opérateur
    boolean existsByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
}
