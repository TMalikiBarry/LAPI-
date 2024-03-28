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
	private Date endDate;
	private String operator;
	private Double grossGamingProduct;
	private Double integratorRemuneration;
	private Double revenue;
	private Double royalties;
	private Double payin;
	private Double payout;
	private Double mises;
	private Double gain;
	private Double bonus;

	public Revenue(Date date, Double grossGamingProduct, Double integratorRemuneration, Double revenue, Double royalties, Double payin, Double payout, Double mises, Double gain, Double bonus) {
		this.date = date;
		this.grossGamingProduct = getValueOr0(grossGamingProduct);
		this.integratorRemuneration = getValueOr0(integratorRemuneration);
		this.revenue = getValueOr0(revenue);
		this.royalties = getValueOr0(royalties);
		this.payin = getValueOr0(payin);
		this.payout = getValueOr0(payout);
		this.mises = getValueOr0(mises);
		this.gain = getValueOr0(gain);
		this.bonus = getValueOr0(bonus);
	}

	private Double getValueOr0(Double value) {
		if (value != null) return value;
		return 0D;
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
				.mises(mises)
				.gain(gain)
				.bonus(bonus)
				.build();
	}

	public static List<RevenueDTO> toDTOs(List<Revenue> revenues) {
		List<RevenueDTO> dtos = new ArrayList<>();
		for (Revenue revenue : revenues)
			dtos.add(revenue.toDTO());
		return dtos;
	}

}
