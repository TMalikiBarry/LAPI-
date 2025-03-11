package sn.intouch.gu.lonaciapi.ejb.notification.entities;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(
//        uniqueConstraints = @UniqueConstraint(columnNames = {"code_service-momo", "operateur_service-momo"}),
        name = "code_service_momo"
)
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class CodeServiceMOMO {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le code de service MOMO ne doit pas être nul.")
    @Column(name = "code_momo")  // Nouveau nom en base
    private String codeMomo;

    @NotNull(message = "L'Opérateur de service MOMO ne doit pas être nul.")
    @Column(name = "operateur_service_momo")
    private String operateurServiceMomo;

    @Column(name = "service_nom")
    private String serviceNom;

    private String type;

    @Column(name="supprime")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "creation_date")
    private Date creationDate;
    @Column(name = "modification_date")
    private Date modificationDate;

    @PreUpdate
    private void updatedDate() {
        this.modificationDate = new Date();
    }
    @PrePersist
    private void createdDate() {
        this.creationDate = new Date();
        this.modificationDate = new Date();
    }
}


