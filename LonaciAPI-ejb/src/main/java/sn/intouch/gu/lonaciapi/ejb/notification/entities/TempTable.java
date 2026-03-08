package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "temp_table", uniqueConstraints = { @UniqueConstraint(columnNames = { "value" }) })
@Getter
@Setter
@NoArgsConstructor
public class TempTable implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private Date date;
    private String value;

    public TempTable (String value) {
        super();
        this.value = value;
        this.date = new Date();
    }
}
