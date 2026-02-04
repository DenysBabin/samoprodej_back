package samoprodej.samoprodej.dto;

import samoprodej.samoprodej.enums.ParkingType;
import samoprodej.samoprodej.enums.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PropertyResponse(
        UUID id,
        UUID ownerUserId,
        PropertyType type,
        String country,
        String city,
        String district,
        String street,
        String houseNumber,
        String addressText,
        BigDecimal lat,
        BigDecimal lng,
        String dispozice,
        Integer roomsCount,
        Integer floor,
        Integer totalFloors,
        BigDecimal areaM2,
        BigDecimal balconyAreaM2,
        BigDecimal cellarAreaM2,
        Boolean hasBalcony,
        Boolean hasTerrace,
        Boolean hasLoggia,
        Boolean hasGarden,
        Boolean hasCellar,
        Boolean hasElevator,
        ParkingType parkingType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
