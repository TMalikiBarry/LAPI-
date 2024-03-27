package sn.intouch.gu.lonaciapi.ejb.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.dto.RevenueDTO;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "revenue", uniqueConstraints = { @UniqueConstraint(columnNames = { "date", "operator" }) })
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
	private Double payin;
	private Double payout;

	public Revenue(Date date, Double grossGamingProduct, Double integratorRemuneration, Double revenue, Double royalties, Double payin, Double payout) {
		this.date = date;
		this.grossGamingProduct = grossGamingProduct;
		this.integratorRemuneration = integratorRemuneration;
		this.revenue = revenue;
		this.royalties = royalties;
		this.payin = payin;
		this.payout = payout;
	}

	public RevenueDTO toDTO() {
		return RevenueDTO.builder()
				.date(date != null ? DateUtil.SIMPLE_DATE_FORMAT.format(date) : null)
				.operator(operator)
				.grossGamingProduct(grossGamingProduct)
				.integratorRemuneration(integratorRemuneration)
				.revenue(revenue)
				.royalties(royalties)
				.payin(payin)
				.payout(payout)
				.build();
	}

	public static List<RevenueDTO> toDTOs(List<Revenue> revenues) {
		List<RevenueDTO> dtos = new ArrayList<>();
		for (Revenue revenue : revenues)
			dtos.add(revenue.toDTO());
		return dtos;
	}

}
