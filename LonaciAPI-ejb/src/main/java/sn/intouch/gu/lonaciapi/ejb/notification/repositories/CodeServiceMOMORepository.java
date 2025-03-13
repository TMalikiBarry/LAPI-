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
            "WHERE (:code_momo IS NULL OR LOWER(c.codeMomo) LIKE LOWER(CONCAT('%', :code_momo, '%'))) " +
            "  AND (:operateur_service_momo IS NULL OR LOWER(c.operateurServiceMomo) LIKE LOWER(CONCAT('%', :operateur_service_momo, '%'))) " +
            "  AND (:service_nom IS NULL OR LOWER(c.serviceNom) LIKE LOWER(CONCAT('%', :service_nom, '%'))) " +
            "  AND (:type IS NULL OR c.type = :type) " +
//            "  AND ((:startDate IS NULL AND :endDate IS NULL) OR (c.creationDate BETWEEN :startDate AND :endDate))" +
            "  AND (:start_date IS NULL OR c.creationDate >= :start_date) " +
            "  AND (:end_date IS NULL OR c.creationDate <= :end_date)" +
            "ORDER BY c.creationDate DESC")
    List<CodeServiceMOMO> findByOptionalParams(
            @Param("code_momo") String codeMomo,
            @Param("operateur_service_momo") String operateurServiceMomo,
            @Param("service_nom") String serviceNom,
            @Param("type") String type,
            @Param("start_date") Date startDate,
            @Param("end_date") Date endDate);

    // Méthode générique paginée
    @Query("SELECT c FROM CodeServiceMOMO c " +
            "WHERE (:code_momo IS NULL OR LOWER(c.codeMomo) LIKE LOWER(CONCAT('%', :code_momo, '%'))) " +
            "  AND (:operateur_service_momo IS NULL OR LOWER(c.operateurServiceMomo) LIKE LOWER(CONCAT('%', :operateur_service_momo, '%'))) " +
            "  AND (:service_nom IS NULL OR LOWER(c.serviceNom) LIKE LOWER(CONCAT('%', :service_nom, '%'))) " +
            "  AND (:type IS NULL OR c.type = :type) " +
//            "  AND ((:startDate IS NULL AND :endDate IS NULL) OR (c.creationDate BETWEEN :startDate AND :endDate))" +
            "  AND (:start_date IS NULL OR c.creationDate >= :start_date) " +
            "  AND (:end_date IS NULL OR c.creationDate <= :end_date)" +
            "ORDER BY c.creationDate DESC")
    Page<CodeServiceMOMO> findByOptionalParamsPaged(
            @Param("code_momo") String codeMomo,
            @Param("operateur_service_momo") String operateurServiceMomo,
            @Param("service_nom") String serviceNom,
            @Param("type") String type,
            @Param("start_date") Date startDate,
            @Param("end_date") Date endDate,
            Pageable pageable);

    Optional<CodeServiceMOMO> findByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
    // Retrouve l'entité par son code unique
    List<CodeServiceMOMO> findByCodeMomo(String codeServiceMomo);

    // Retrouve la liste de toutes les entités associées à un opérateur
    @Query("SELECT c FROM CodeServiceMOMO c WHERE LOWER(c.operateurServiceMomo) = LOWER(:operateurServiceMomo)")
    List<CodeServiceMOMO> findByOperateurServiceMomo(String operateurServiceMomo);

    // Vérifie l'existence d'une association spécifique entre un code et un opérateur
    boolean existsByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
}
