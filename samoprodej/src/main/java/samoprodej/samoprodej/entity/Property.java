package samoprodej.samoprodej.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import samoprodej.samoprodej.enums.ParkingType;
import samoprodej.samoprodej.enums.PropertyType;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE properties SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Property {

    @Id
    @UuidGenerator
    private UUID id;


    @NotNull
    @Column(name = "owner_user_id", nullable = false, updatable = false)
    private UUID ownerUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, updatable = false)
    private PropertyType type;

    @NotBlank
    @Size(max = 2)
    @ColumnDefault("'CZ'")
    @Column(nullable = false, length = 2, updatable = false)
    private String country = "CZ";


    @NotBlank
    @Size(max = 128)
    @Column(nullable = false, length = 128)
    private String city;

    @Size(max = 128)
    @Column(length = 128)
    private String district;

    @Size(max = 255)
    @Column(length = 255)
    private String street;

    @Size(max = 32)
    @Column(name = "house_number", length = 32)
    private String houseNumber;

    @NotBlank
    @Size(max = 512)
    @Column(name = "address_text", nullable = false, length = 512)
    private String addressText;

    @Size(max = 512)
    @Column(name = "city_raw")
    String cityRaw;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "city_norm", nullable = false)
    private String cityNorm;

    @Size(max = 16)
    @Column(length = 16)
    private String dispozice;

    @Column(name = "rooms_count")
    private Integer roomsCount;

    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @NotNull
    @Column(name = "area_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaM2;

    @Column(name = "balcony_area_m2", precision = 10, scale = 2)
    private BigDecimal balconyAreaM2;

    @Column(name = "cellar_area_m2", precision = 10, scale = 2)
    private BigDecimal cellarAreaM2;

    @Column(name = "has_balcony", nullable = false)
    @ColumnDefault("false")
    private Boolean hasBalcony = false;

    @Column(name = "has_terrace", nullable = false)
    @ColumnDefault("false")
    private Boolean hasTerrace = false;

    @Column(name = "has_loggia", nullable = false)
    @ColumnDefault("false")
    private Boolean hasLoggia = false;

    @Column(name = "has_garden", nullable = false)
    @ColumnDefault("false")
    private Boolean hasGarden = false;

    @Column(name = "has_cellar", nullable = false)
    @ColumnDefault("false")
    private Boolean hasCellar = false;

    @Column(name = "has_elevator", nullable = false)
    @ColumnDefault("false")
    private Boolean hasElevator = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_type", length = 32)
    private ParkingType parkingType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.country == null) this.country = "CZ";
        if (this.hasBalcony == null) this.hasBalcony = false;
        if (this.hasElevator == null) this.hasElevator = false;

        normalizeCity();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        normalizeCity();
    }

    private void normalizeCity() {
        if (this.city != null) {
            this.cityNorm = unaccent(this.city.toLowerCase());
        }
    }

    private String unaccent(String src) {
        if (src == null) return null;
        String normalized = Normalizer.normalize(src, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
    }
}