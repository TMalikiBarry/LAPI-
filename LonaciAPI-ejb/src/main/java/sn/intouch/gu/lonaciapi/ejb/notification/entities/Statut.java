package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "statuts")
public class Statut implements Serializable{
	
	private int idStatut;
	private String codeStatut;
	private String nomStatut;
	private String colorStatut;
	
	Boolean supprime=false;
	
	
	
	@Id
	@Column(name = "statut_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getIdStatut() {
		return idStatut;
	}
	public void setIdStatut(int idStatut) {
		this.idStatut = idStatut;
	}
	
	@Column(name = "statut_code")
	public String getCodeStatut() {
		return codeStatut;
	}
	public void setCodeStatut(String codeStatut) {
		this.codeStatut = codeStatut;
	}
	
	@Column(name = "statut_nom")
	public String getNomStatut() {
		return nomStatut;
	}
	public void setNomStatut(String nomStatut) {
		this.nomStatut = nomStatut;
	}
	
	@Column(name = "statut_couleur")
	public String getColorStatut() {
		return colorStatut;
	}
	public void setColorStatut(String colorStatut) {
		this.colorStatut = colorStatut;
	}
	
	@Column(name = "statut_sup")
	public Boolean getSupprime() {
		return supprime;
	}
	public void setSupprime(Boolean supprime) {
		this.supprime = supprime;
	}
	
	

}
