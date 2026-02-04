package samoprodej.samoprodej.entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Setter
    private ListingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 16)
    @Setter
    private PaymentStatus paymentStatus;

    @Column(name = "published_at")
    @Setter(AccessLevel.NONE)
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;

    @Column(name = "rent_monthly", nullable = false)
    @Setter
    private Integer rentMonthly;

    @Column(name = "deposit_kauce")
    @Setter
    private Integer depositKauce;

    @Column(name = "utilities_monthly")
    @Setter
    private Integer utilitiesMonthly;

    @Column(name = "pets_allowed")
    @Setter
    private Boolean petsAllowed;

    @Column(name = "smoking_allowed")
    @Setter
    private Boolean smokingAllowed;

    @Column(name = "children_allowed")
    @Setter
    private Boolean childrenAllowed;

    @Column(name = "max_tenants")
    @Setter
    private Short maxTenants;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = ListingStatus.DRAFT;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void publish() {
        status = ListingStatus.PUBLISHED;
        publishedAt = Instant.now();
    }
}