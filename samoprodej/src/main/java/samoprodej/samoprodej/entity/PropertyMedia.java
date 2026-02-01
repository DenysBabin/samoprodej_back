package samoprodej.samoprodej.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import samoprodej.samoprodej.enums.MediaType;


import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "property_media",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_property_media_property_sort",
                        columnNames = {"property_id", "sort_order"})
        },
        indexes = {
                @Index(name = "idx_property_media_property_id", columnList = "property_id"),
                @Index(name = "idx_property_media_type", columnList = "type")
        }
)
public class PropertyMedia {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Setter
    private MediaType type;

    @Column(columnDefinition = "text", nullable = false)
    @Setter
    private String url;

    @Column(name = "preview_url", columnDefinition = "text")
    @Setter
    private String previewUrl;

    @Column(name = "sort_order", nullable = false)
    @Setter
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    public PropertyMedia(Property property, MediaType type, String url) {
        this.property = property;
        this.type = type;
        this.url = url;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        if (this.sortOrder == null) this.sortOrder = 0;
    }
}
