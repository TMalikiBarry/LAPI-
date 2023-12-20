package sn.intouch.gu.lonaciapi.ejb.operator.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: Operateur
 *
 */
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
	@Column(unique = true)
	private String operateur_id;
	private String operateur_libelle;
	private String operateur_token;
	private String code_marchand;
	private String code_reseau;
	Boolean supprime = false;
	private Date date_creation, date_modification;
	private String statut;

}
