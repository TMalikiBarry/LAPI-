package sn.intouch.gu.lonaciapi.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReformattedResponse implements Serializable {
    private String operator;
    private TimedResponse<Double> grossRevenue;
    private TimedResponse<Double> gamblingTax;
    private TimedResponse<Double> withholding;
    private TimedResponse<Double> grossGamingProduct;
    private TimedResponse<Double> integratorRemuneration;
    private TimedResponse<Double> revenue;
    private TimedResponse<Double> royalties;
    private TimedResponse<Double> payin;
    private TimedResponse<Double> payout;
    private TimedResponse<Double> mises;
    private TimedResponse<Double> gain;
    private TimedResponse<Double> bonus;
}
