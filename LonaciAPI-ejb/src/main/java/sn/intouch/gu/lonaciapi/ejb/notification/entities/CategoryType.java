package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.dto.CategoryTypeDTO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name="category_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryType implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
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
	public CategoryTypeDTO toDTO() {
		return CategoryTypeDTO.builder()
				.code(code)
				.label(label)
				.build();
	}
}
