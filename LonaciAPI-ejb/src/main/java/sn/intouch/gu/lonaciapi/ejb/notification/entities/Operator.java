package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import sn.intouch.gu.lonaciapi.ejb.dto.OperatorDTO;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "operateur")
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Operator implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long Id;
	@Column(name = "operateur_id", unique = true)
	private String operatorId;
	@Column(name = "operateur_libelle")
	private String operatorLabel;
	@Column(name = "operateur_token")
	private String operatorToken;
	@Column(name = "code_marchand")
	private String merchantCode;
	@Column(name = "code_reseau")
	private String networkCode;
	@Builder.Default
	private Boolean supprime = false;
	@Column(name = "date_creation")
	@CreatedDate
	private Date creationDate;
	@Column(name = "date_modification")
	private Date modificationDate;
	private String statut;

	private String country;

	@PreUpdate
	private void updatedDate() {
		this.modificationDate = new Date();
	}

	@PrePersist
	private void createdDate() {
		this.creationDate = new Date();
		this.modificationDate = new Date();
	}

	public OperatorDTO toDTO() {
		return OperatorDTO.builder()
				.identifier(operatorId)
				.label(operatorLabel)
				.token(operatorToken)
				.merchantCode(merchantCode)
				.networkCode(networkCode)
				.status(statut)
				.creationDate(creationDate != null ? creationDate.getTime() : 0)
				.modificationDate(modificationDate != null ? modificationDate.getTime() : 0)
				.build();
	}
}
