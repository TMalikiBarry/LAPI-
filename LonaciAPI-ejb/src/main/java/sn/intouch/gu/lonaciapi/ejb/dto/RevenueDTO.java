package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String date;
	private String operator;
	private Double grossGamingProduct;
	private Double integratorRemuneration;
	private Double revenue;
	private Double royalties;

	public RevenueDTO(String date, Double grossGamingProduct, Double integratorRemuneration, Double revenue, Double royalties) {
		this.date = DateUtil.SIMPLE_DATE_FORMAT.format(date);
		this.grossGamingProduct = grossGamingProduct;
		this.integratorRemuneration = integratorRemuneration;
		this.revenue = revenue;
		this.royalties = royalties;
	}
}
