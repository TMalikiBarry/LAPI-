package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Builder
public class RevenueDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String date;
	private String operator;
	private Double grossGamingProduct;
	private Double integratorRemuneration;
	private Double revenue;
	private Double royalties;
	private Double payin;
	private Double payout;

	public RevenueDTO(String date, String operator, Double grossGamingProduct, Double integratorRemuneration,
					  Double revenue, Double royalties, Double payin, Double payout) {
		this.date = DateUtil.SIMPLE_DATE_FORMAT.format(date);
		this.operator = operator;
		this.grossGamingProduct = grossGamingProduct;
		this.integratorRemuneration = integratorRemuneration;
		this.revenue = revenue;
		this.royalties = royalties;
		this.payin = payin;
		this.payout = payout;
	}
}
