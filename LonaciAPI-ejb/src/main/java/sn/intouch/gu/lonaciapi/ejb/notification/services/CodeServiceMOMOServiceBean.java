package sn.intouch.gu.lonaciapi.ejb.notification.services;


import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sn.intouch.gu.lonaciapi.config.BadRequestException;
import sn.intouch.gu.lonaciapi.config.DuplicateEntryException;
import sn.intouch.gu.lonaciapi.config.EntityNotFoundCustomException;
import sn.intouch.gu.lonaciapi.ejb.dto.SaveAllResponse;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.CodeServiceMOMORepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Stateless
public class CodeServiceMOMOServiceBean implements CodeServiceMOMOService {

    @PersistenceContext(unitName = "lonaciPU")
    EntityManager em;

    private CodeServiceMOMORepository codeServiceMOMORepository;

    @PostConstruct
    private void init() {
        RepositoryFactorySupport factorySupport = new JpaRepositoryFactory(em);
        this.codeServiceMOMORepository = factorySupport.getRepository(CodeServiceMOMORepository.class);
    }

    @Override
    public CodeServiceMOMO save(CodeServiceMOMO codeServiceMOMO) {
        if (codeServiceMOMORepository.existsByCodeMomoAndOperateurServiceMomo(
                codeServiceMOMO.getCodeMomo(),
                codeServiceMOMO.getOperateurServiceMomo())) {
            throw new DuplicateEntryException("Ce code pour cet opérateur existe déjà.");
        }
        return codeServiceMOMORepository.save(codeServiceMOMO);
    }

    @Override
    public SaveAllResponse saveAll(List<CodeServiceMOMO> codeServiceMOMOList) {
        List<CodeServiceMOMO> toSave = new ArrayList<>();
        List<String> duplicateMessages = new ArrayList<>();

        // Parcours de la liste pour séparer les doublons des nouvelles entrées
        for (CodeServiceMOMO c : codeServiceMOMOList) {
            if (codeServiceMOMORepository.existsByCodeMomoAndOperateurServiceMomo(
                    c.getCodeMomo(), c.getOperateurServiceMomo())) {
                duplicateMessages.add("Le code " + c.getCodeMomo()
                        + " pour l'opérateur " + c.getOperateurServiceMomo() + " existe déjà.");
            } else {
                toSave.add(c);
            }
        }

        // Sauvegarde des entrées non dupliquées
        List<CodeServiceMOMO> savedEntries = codeServiceMOMORepository.saveAll(toSave);

        // Retourne le résultat : entrées sauvegardées et messages d'erreur pour les doublons
        return SaveAllResponse.builder()
                .savedEntries(savedEntries)
                .duplicateMessages(duplicateMessages)
                .build();
    }

    @Override
    public CodeServiceMOMO update(CodeServiceMOMO codeServiceMOMO) {
        if (codeServiceMOMO.getId() == null) {
            throw new BadRequestException("L'ID est requis pour la mise à jour.");
        }
        // Vérifier que l'entité existe
        if (!codeServiceMOMORepository.existsById(codeServiceMOMO.getId())) {
            throw new EntityNotFoundCustomException("CodeServiceMOMO introuvable pour l'ID " + codeServiceMOMO.getId());
        }
        // Vérifier que la nouvelle combinaison (code, opérateur) n'existe pas déjà sur un autre enregistrement
        Optional<CodeServiceMOMO> existingEntity = codeServiceMOMORepository.findByCodeMomoAndOperateurServiceMomo(
                codeServiceMOMO.getCodeMomo(), codeServiceMOMO.getOperateurServiceMomo());
        if (existingEntity.isPresent() && !existingEntity.get().getId().equals(codeServiceMOMO.getId())) {
            throw new DuplicateEntryException("Ce code pour cet opérateur existe déjà.");
        }
        return codeServiceMOMORepository.save(codeServiceMOMO);
    }

    @Override
    public CodeServiceMOMO updateByCodeService(CodeServiceMOMO codeServiceMOMO, String code) {
        // Recherche de l'entité à mettre à jour via le code
        List<CodeServiceMOMO> entities = codeServiceMOMORepository.findByCodeMomo(code);
        if (entities.isEmpty()) {
            throw new EntityNotFoundCustomException("Aucune entité trouvée pour le code : " + code);
        }
        CodeServiceMOMO entityToUpdate = entities.get(0);

        // Vérifier qu'il n'existe pas déjà une autre entité avec la nouvelle combinaison
        Optional<CodeServiceMOMO> duplicate = codeServiceMOMORepository.findByCodeMomoAndOperateurServiceMomo(
                codeServiceMOMO.getCodeMomo(), codeServiceMOMO.getOperateurServiceMomo());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(entityToUpdate.getId())) {
            throw new DuplicateEntryException("Ce code pour cet opérateur existe déjà.", HttpStatus.CONFLICT.value());
        }

        // Mise à jour du champ opérateurMOMO
        entityToUpdate.setOperateurServiceMomo(codeServiceMOMO.getOperateurServiceMomo());


        return codeServiceMOMORepository.save(entityToUpdate);
    }

    @Override
    public CodeServiceMOMO findById(Long id) {
        return codeServiceMOMORepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundCustomException("CodeServiceMOMO introuvable pour l'ID " + id));
    }

    @Override
    public List<CodeServiceMOMO> findAll() {
        return codeServiceMOMORepository.findAll();
    }

    @Override
    public Optional<CodeServiceMOMO> findByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo) {
        return codeServiceMOMORepository.findByCodeMomoAndOperateurServiceMomo(codeServiceMomo, operateurServiceMomo);
    }

    @Override
    public List<CodeServiceMOMO> findByCodeMomo(String codeServiceMomo) {
        return codeServiceMOMORepository.findByCodeMomo(codeServiceMomo);
    }

    @Override
    public List<CodeServiceMOMO> findByOperateurServiceMomo(String operateurServiceMomo) {
        return codeServiceMOMORepository.findByOperateurServiceMomo(operateurServiceMomo);
    }

    @Override
    public boolean existsByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo) {
        return codeServiceMOMORepository.existsByCodeMomoAndOperateurServiceMomo(codeServiceMomo, operateurServiceMomo);
    }
}
