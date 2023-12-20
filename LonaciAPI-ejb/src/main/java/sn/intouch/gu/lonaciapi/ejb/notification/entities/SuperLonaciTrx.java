package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.intouch.gu.lonaciapi.ejb.notification.models.LonaciNotification;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@MappedSuperclass
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SuperLonaciTrx implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="transaction_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long transactionId;

	// operateur_id
	@Column(name="operateur_id")
    private String operateurID;
	
	// operateur_libelle
	@Column(name="operateur_libelle")
    private String operateurLibelle;

	// id_from_partner
    @Column(name="id_from_partner", unique = true)
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
    @Column(unique = true)
    private String lonaciTransactionID;

	public static SuperLonaciTrx buildTrxFromNotification(LonaciNotification notification) {
		SuperLonaciTrx lonaciTrx = new SuperLonaciTrx();
		lonaciTrx.setOperateurID(notification.getOperateurID());
		lonaciTrx.setOperateurLibelle(notification.getOperateurLibelle());
		lonaciTrx.setIdFromPartner(notification.getIdFromPartner());
		lonaciTrx.setCodeService(notification.getCodeService());
		lonaciTrx.setTypeTransaction(notification.getTypeTransaction());
		lonaciTrx.setMontant(notification.getMontant());
		lonaciTrx.setDestinataire(notification.getDestinataire());
		lonaciTrx.setDate(notification.getDateTransaction());
		lonaciTrx.setLonaciTransactionID(notification.getLonaciTransactionID());
		
		return lonaciTrx;
	}
}
