package samoprodej.samoprodej.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import samoprodej.samoprodej.enums.ParkingType;
import samoprodej.samoprodej.enums.PropertyType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePropertyRequest(
        @NotNull
        UUID ownerUserId,

        @NotNull
        PropertyType type,

        @Size(max = 2)
        String country,

        @NotBlank
        @Size(max = 128)
        String city,

        @Size(max = 128)
        String district,

        @Size(max = 255)
        String street,

        @Size(max = 32)
        String houseNumber,

        @Size(max = 512)
        String addressText,

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal lat,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal lng,

        @Size(max = 16)
        String dispozice,

        @Positive
        Integer roomsCount,

        Integer floor,

        @Positive
        Integer totalFloors,

        @NotNull
        @Positive
        BigDecimal areaM2,

        @Positive
        BigDecimal balconyAreaM2,

        @Positive
        BigDecimal cellarAreaM2,

        Boolean hasBalcony,

        Boolean hasTerrace,

        Boolean hasLoggia,

        Boolean hasGarden,

        Boolean hasCellar,

        Boolean hasElevator,

        ParkingType parkingType
) {
}
