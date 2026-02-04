package samoprodej.samoprodej.entity;

import jakarta.persistence.*;

@Table(name = "payments_listing")
@PrimaryKeyJoinColumn(name = "payment_id")
public class VerificationPayment extends Payment{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private VerificationRequest verificationRequest;
}
