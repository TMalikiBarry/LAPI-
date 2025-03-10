package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.dto.SaveAllResponse;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;

import javax.ejb.Local;
import java.util.List;
import java.util.Optional;

@Local
public interface CodeServiceMOMOService {

    CodeServiceMOMO save(CodeServiceMOMO codeServiceMOMO);
    SaveAllResponse saveAll(List<CodeServiceMOMO> codeServiceMOMO);

    CodeServiceMOMO update(CodeServiceMOMO codeServiceMOMO);
    CodeServiceMOMO updateByCodeService(CodeServiceMOMO codeServiceMOMO, String code);

    CodeServiceMOMO findById(Long id);
    List<CodeServiceMOMO> findAll();
    Optional<CodeServiceMOMO> findByCodeServiceMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
    // Retrouve l'entité par son code unique
    List<CodeServiceMOMO> findByCodeServiceMomo(String codeServiceMomo);

    // Retrouve la liste de toutes les entités associées à un opérateur
    List<CodeServiceMOMO> findByOperateurServiceMomo(String operateurServiceMomo);

    // Vérifie l'existence d'une association spécifique entre un code et un opérateur
    boolean existsByCodeServiceMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo);
}
