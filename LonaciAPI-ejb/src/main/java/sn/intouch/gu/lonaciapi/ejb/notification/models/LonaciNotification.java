package sn.intouch.gu.lonaciapi.ejb.notification.models;

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
import javax.persistence.Transient;

import java.util.Date;

@Entity
@Table(name="lonaci_notification")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LonaciNotification implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="transaction_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long transactionId;

	// operateur_id
	@Column(name="operateur_id")
    private String operateurID;
	
	@Column(name = "operator_transaction_id", unique = true)
	private String operatorTransactionID;
	
	// operateur_libelle
	@Column(name="operateur_libelle")
    private String operateurLibelle;

	// id_from_partner
    @Column(name="id_from_partner")
    private String idFromPartner;

    // code_service
    @Column(name="code_service")
    private String codeService;

    // type_transaction
    @Column(name="type_transaction")
    private String typeTransaction;
    
    private Double montant;
    private String destinataire;
    private Date date;
    @Transient
    private Date dateTransaction;
    @Column(unique = true)
    private String lonaciTransactionID;
    @Column(name="user_id")
    private String userID;

}
