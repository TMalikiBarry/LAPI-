package sn.intouch.gu.lonaciapi.ejb.parameter.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "compute_parameter")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComputeParameter implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String operator;
    private String country;

    @Column(name = "cashin_fees")
    private Double cashinFees = 0D;
    @Column(name = "payment_fees")
    private Double paymentFees = 0D;
    @Column(name = "royalty_rate")
    private Double royaltyRate = 0D;
    @Column(name = "payment_rate")
    private Double paymentRate = 0D;
    @Column(name = "cashin_rate")
    private Double cashinRate = 0D;
    @Column(name = "is_active")
    private Boolean isActive = true;
}
