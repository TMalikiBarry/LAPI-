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
            "  AND (:code_iso IS NULL OR c.codeIso = :code_iso) " +
//            "  AND ((:startDate IS NULL AND :endDate IS NULL) OR (c.creationDate BETWEEN :startDate AND :endDate))" +
            "  AND (:start_date IS NULL OR c.creationDate >= :start_date) " +
            "  AND (:end_date IS NULL OR c.creationDate <= :end_date)" +
            "ORDER BY c.creationDate DESC")
    List<CodeServiceMOMO> findByOptionalParams(
            @Param("code_momo") String codeMomo,
            @Param("operateur_service_momo") String operateurServiceMomo,
            @Param("service_nom") String serviceNom,
            @Param("type") String type,
            @Param("code_iso") String codeIso,
            @Param("start_date") Date startDate,
            @Param("end_date") Date endDate);

    // Méthode générique paginée
    @Query("SELECT c FROM CodeServiceMOMO c " +
            "WHERE (:code_momo IS NULL OR LOWER(c.codeMomo) LIKE LOWER(CONCAT('%', :code_momo, '%'))) " +
            "  AND (:operateur_service_momo IS NULL OR LOWER(c.operateurServiceMomo) LIKE LOWER(CONCAT('%', :operateur_service_momo, '%'))) " +
            "  AND (:service_nom IS NULL OR LOWER(c.serviceNom) LIKE LOWER(CONCAT('%', :service_nom, '%'))) " +
            "  AND (:type IS NULL OR c.type = :type) " +
            "  AND (:code_iso IS NULL OR c.codeIso = :code_iso) " +
//            "  AND ((:startDate IS NULL AND :endDate IS NULL) OR (c.creationDate BETWEEN :startDate AND :endDate))" +
            "  AND (:start_date IS NULL OR c.creationDate >= :start_date) " +
            "  AND (:end_date IS NULL OR c.creationDate <= :end_date)" +
            "ORDER BY c.creationDate DESC")
    Page<CodeServiceMOMO> findByOptionalParamsPaged(
            @Param("code_momo") String codeMomo,
            @Param("operateur_service_momo") String operateurServiceMomo,
            @Param("service_nom") String serviceNom,
            @Param("type") String type,
            @Param("code_iso") String codeISO,
            @Param("start_date") Date startDate,
            @Param("end_date") Date endDate,
            Pageable pageable);

    @Query("SELECT c FROM CodeServiceMOMO c " +
            "WHERE c.codeMomo = :codeMomo " +
            "  AND c.operateurServiceMomo = :operateurMomo " +
            "  AND (:codeIso IS NULL OR c.codeIso = :codeIso) ORDER BY c.creationDate DESC")
    Optional<CodeServiceMOMO> findByCodeMomoAndOperateurServiceMomo(
            @Param("codeMomo") String codeMomo,
            @Param("operateurMomo") String operateurMomo,
            @Param("codeIso") String codeIso);

    // Recherche par codeMomo uniquement avec codeIso
    @Query("SELECT c FROM CodeServiceMOMO c " +
            "WHERE c.codeMomo = :codeMomo " +
            "  AND (:codeIso IS NULL OR c.codeIso = :codeIso) ORDER BY c.creationDate DESC")
    List<CodeServiceMOMO> findByCodeMomo(
            @Param("codeMomo") String codeMomo,
            @Param("codeIso") String codeIso);

    // Recherche des services d'un opérateur spécifique avec codeIso
    @Query("SELECT c FROM CodeServiceMOMO c " +
            "WHERE LOWER(TRIM(c.operateurServiceMomo)) = LOWER(TRIM(:operateurMomo)) " +
            "  AND (:codeIso IS NULL OR c.codeIso = :codeIso)" +
            "ORDER BY c.creationDate DESC")
    List<CodeServiceMOMO> findByOperateurServiceMomo(
            @Param("operateurMomo") String operateurMomo,
            @Param("codeIso") String codeIso);

    // Vérification d'existence d'un service chez un opérateur avec codeIso
    @Query("SELECT COUNT(c) > 0 FROM CodeServiceMOMO c " +
            "WHERE c.codeMomo = :codeMomo " +
            "  AND c.operateurServiceMomo = :operateurMomo " +
            "  AND (:codeIso IS NULL OR c.codeIso = :codeIso)")
    boolean existsByCodeMomoAndOperateurServiceMomo(
            @Param("codeMomo") String codeMomo,
            @Param("operateurMomo") String operateurMomo,
            @Param("codeIso") String codeIso);

    // Liste des opérateurs distincts triés par ordre alphabétique avec codeIso
    @Query("SELECT DISTINCT c.operateurServiceMomo FROM CodeServiceMOMO c " +
            "WHERE (:codeIso IS NULL OR c.codeIso = :codeIso) " +
            "ORDER BY c.operateurServiceMomo ASC")
    List<String> findDistinctOperateurs(@Param("codeIso") String codeIso);
}
