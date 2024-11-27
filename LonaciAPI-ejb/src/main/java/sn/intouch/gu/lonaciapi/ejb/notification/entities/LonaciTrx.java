package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.intouch.gu.lonaciapi.ejb.notification.models.LonaciNotification;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name="lonaci_trx")
@Data
@SuperBuilder
@AllArgsConstructor
public class LonaciTrx extends SuperLonaciTrx {

}
