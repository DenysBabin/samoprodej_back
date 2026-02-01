package samoprodej.samoprodej.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Table(name = "payments_deal")
@PrimaryKeyJoinColumn(name = "payment_id")
public class DealPayment extends Payment{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ColumnDefault("0")
    @Column(name = "commission_rate_bts", nullable = true)
    private Integer commission_rate_bts;

    @Column(name = "commission_base_amount", nullable = true)
    private Integer commission_base_amount;

}
