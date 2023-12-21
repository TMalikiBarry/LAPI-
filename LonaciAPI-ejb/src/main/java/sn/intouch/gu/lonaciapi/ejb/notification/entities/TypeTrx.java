package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import sn.intouch.gu.lonaciapi.ejb.dto.TypeTrxDTO;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;


@Entity
@Table(name="type_trx")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeTrx implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="type_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long typeId;
	
	@Column(name="code")
	private String code;
	
	@Column(name="libelle")
	private String label;
	
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
	public TypeTrxDTO toDTO() {
		return TypeTrxDTO.builder()
				.code(code)
				.label(label)
				.build();
	}
}
