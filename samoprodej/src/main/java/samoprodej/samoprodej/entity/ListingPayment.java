package samoprodej.samoprodej.entity;

import jakarta.persistence.*;
import samoprodej.samoprodej.enums.PromotionType;

import java.time.Instant;

@Table(name = "payments_listing")
@PrimaryKeyJoinColumn(name = "payment_id")
public class ListingPayment extends Payment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    private PromotionType promotionType;

    @Column(name = "promotion_start_at")
    private Instant promotionStartAt;

    @Column(name = "promotion_start_end")
    private Instant promotionStartEnd;
}
