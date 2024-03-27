package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;

import java.io.Serializable;

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
	private Double payin;
	private Double payout;
	private Double mises;
	private Double gain;
	private Double bonus;

}
