package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.dto.RevenueDTO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "revenue")
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Revenue implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private Date date;
	private String operator;
	private Double grossGamingProduct;
	private Double integratorRemuneration;
	private Double revenue;
	private Double royalties;
	
	@PrePersist
	private void createdDate() {
		this.date = new Date();
	}

	public RevenueDTO toDTO() {
		return RevenueDTO.builder()
				.date(date != null ? date.getTime() : 0)
				.operator(operator)
				.grossGamingProduct(grossGamingProduct)
				.integratorRemuneration(integratorRemuneration)
				.revenue(revenue)
				.royalties(royalties)
				.build();
	}
}
