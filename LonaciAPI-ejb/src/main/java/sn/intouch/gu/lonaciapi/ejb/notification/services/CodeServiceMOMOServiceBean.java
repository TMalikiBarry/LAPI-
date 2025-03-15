package sn.intouch.gu.lonaciapi.ejb.notification.services;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sn.intouch.gu.lonaciapi.config.BadRequestException;
import sn.intouch.gu.lonaciapi.config.DuplicateEntryException;
import sn.intouch.gu.lonaciapi.config.EntityNotFoundCustomException;
import sn.intouch.gu.lonaciapi.config.ResourceAlreadyExistsException;
import sn.intouch.gu.lonaciapi.ejb.dto.SaveAllResponse;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;
import sn.intouch.gu.lonaciapi.ejb.notification.repositories.CodeServiceMOMORepository;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


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
        } else if (codeServiceMOMO.getId() != null && codeServiceMOMORepository.existsById(codeServiceMOMO.getId())) {
            throw new ResourceAlreadyExistsException("Code service MOMO avec l'id " + codeServiceMOMO.getId() + " existe déjà!");
        }
        return codeServiceMOMORepository.save(codeServiceMOMO);
    }

    @Override
    public SaveAllResponse saveAll(List<CodeServiceMOMO> codeServiceMOMOList) {
        List<CodeServiceMOMO> toSave = new ArrayList<>();
        List<String> duplicateMessages = new ArrayList<>();
        List<String> alreadySavedMessages = new ArrayList<>();

        // Parcours de la liste pour séparer les doublons des nouvelles entrées
        for (CodeServiceMOMO c : codeServiceMOMOList) {
            if (codeServiceMOMORepository.existsByCodeMomoAndOperateurServiceMomo(
                    c.getCodeMomo(), c.getOperateurServiceMomo())) {
                duplicateMessages.add("Le code " + c.getCodeMomo()
                        + " pour l'opérateur " + c.getOperateurServiceMomo() + " existe déjà.");
            } else if (codeServiceMOMORepository.existsById(c.getId())) {
                alreadySavedMessages.add("Le service MOMO ayant pour identifiant " + c.getId() + " existe déjà");
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
                .alreadySavedMessages(alreadySavedMessages)
                .build();
    }

    @Override
    public CodeServiceMOMO update(CodeServiceMOMO codeServiceMOMO) {
        return updateEntity(codeServiceMOMO, codeServiceMOMO.getId());
    }

    @Override
    public CodeServiceMOMO updateByCodeService(CodeServiceMOMO codeServiceMOMO, String code) {
        CodeServiceMOMO existingEntity = codeServiceMOMORepository.findByCodeMomo(code)
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundCustomException("Aucune entité trouvée pour le code : " + code));

        return updateEntity(codeServiceMOMO, existingEntity.getId());
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
    public Page<CodeServiceMOMO> findByOptionalParamsPaged(String codeMomo, String operateurServiceMomo, String serviceNom, String type, Date startDate, Date endDate, Pageable pageable) {
        return codeServiceMOMORepository.findByOptionalParamsPaged(codeMomo, operateurServiceMomo, serviceNom, type, startDate, endDate, pageable);
    }

    @Override
    public List<CodeServiceMOMO> findByOptionalParams(String codeMomo, String operateurServiceMomo, String serviceNom, String type, Date startDate, Date endDate) {
        return codeServiceMOMORepository.findByOptionalParams(codeMomo, operateurServiceMomo, serviceNom, type, startDate, endDate);
    }

    @Override
    public boolean existsByCodeMomoAndOperateurServiceMomo(String codeServiceMomo, String operateurServiceMomo) {
        return codeServiceMOMORepository.existsByCodeMomoAndOperateurServiceMomo(codeServiceMomo, operateurServiceMomo);
    }

    @Override
    public List<String> findDistinctOperateurs() {
        return codeServiceMOMORepository.findDistinctOperateurs();
    }

    // Méthode générique qui gère la mise à jour
    private CodeServiceMOMO updateEntity(CodeServiceMOMO codeServiceMOMO, Long id) {
        if (id == null) throw new BadRequestException("L'ID est requis pour la mise à jour.");

        CodeServiceMOMO existingEntity = codeServiceMOMORepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundCustomException("CodeServiceMOMO introuvable pour l'ID " + id));

        // Vérification unicité si codeMomo et operateurServiceMomo sont fournis
        Optional.ofNullable(codeServiceMOMO.getCodeMomo()).ifPresent(codeMomo ->
                Optional.ofNullable(codeServiceMOMO.getOperateurServiceMomo()).ifPresent(operateur -> {
                    Optional<CodeServiceMOMO> duplicate = codeServiceMOMORepository
                            .findByCodeMomoAndOperateurServiceMomo(codeMomo, operateur);
                    if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                        throw new DuplicateEntryException("Ce code pour cet opérateur existe déjà.", HttpStatus.CONFLICT.value());
                    }
                })
        );

        // Mise à jour des champs modifiés
        applyUpdates(existingEntity, codeServiceMOMO);

        return codeServiceMOMORepository.save(existingEntity);
    }

    // Méthode utilitaire pour éviter les if répétitifs
    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        if (value != null) setter.accept(value);
    }

    private void applyUpdates(CodeServiceMOMO existing, CodeServiceMOMO updates) {
        updateIfNotNull(existing::setCodeMomo, updates.getCodeMomo());
        updateIfNotNull(existing::setOperateurServiceMomo, updates.getOperateurServiceMomo());
        updateIfNotNull(existing::setServiceNom, updates.getServiceNom());
        updateIfNotNull(existing::setType, updates.getType());
        updateIfNotNull(existing::setCreationDate, updates.getCreationDate());
    }
}
