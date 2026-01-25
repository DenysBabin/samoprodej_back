package samoprodej.samoprodej.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import samoprodej.samoprodej.entity.enums.PropertyStatus;
import samoprodej.samoprodej.entity.enums.PropertyType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties")
@SQLDelete(sql = "UPDATE properties SET deleted_at = CURRENT_TIMESTAMP, status = 'ARCHIVED' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Property {

    @Id
    @UuidGenerator
    private UUID id;

    @NotNull(message = "Owner ID is required")
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PropertyType type;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PropertyStatus status;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(nullable = false, length = 2)
    private String country;

    @NotBlank(message = "City is required")
    @Size(max = 128)
    @Column(name = "city_raw", nullable = false, length = 128)
    private String cityRaw;

    @Column(name = "city_norm", nullable = false, length = 128)
    private String cityNorm;

    @NotBlank(message = "Street is required")
    @Size(max = 255)
    @Column(name = "street_raw", nullable = false, length = 255)
    private String streetRaw;

    @Column(name = "street_norm", nullable = false, length = 255)
    private String streetNorm;

    @NotNull(message = "Size is required")
    @Min(value = 1)
    @Column(name = "size_m2", nullable = false)
    private Integer sizeM2;

    @NotNull(message = "Rooms count is required")
    @Min(value = 1)
    @Column(nullable = false)
    private Integer rooms;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Property() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PropertyStatus.DRAFT;
        }
        if (this.country == null) {
            this.country = "CZ";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public PropertyType getType() { return type; }
    public void setType(PropertyType type) { this.type = type; }

    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCityRaw() { return cityRaw; }
    public void setCityRaw(String cityRaw) { this.cityRaw = cityRaw; }

    public String getCityNorm() { return cityNorm; }
    public void setCityNorm(String cityNorm) { this.cityNorm = cityNorm; }

    public String getStreetRaw() { return streetRaw; }
    public void setStreetRaw(String streetRaw) { this.streetRaw = streetRaw; }

    public String getStreetNorm() { return streetNorm; }
    public void setStreetNorm(String streetNorm) { this.streetNorm = streetNorm; }

    public Integer getSizeM2() { return sizeM2; }
    public void setSizeM2(Integer sizeM2) { this.sizeM2 = sizeM2; }

    public Integer getRooms() { return rooms; }
    public void setRooms(Integer rooms) { this.rooms = rooms; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}