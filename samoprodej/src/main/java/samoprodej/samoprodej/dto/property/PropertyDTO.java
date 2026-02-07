package samoprodej.samoprodej.dto.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import samoprodej.samoprodej.enums.ParkingType;
import samoprodej.samoprodej.enums.PropertyType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PropertyDTO {

    private UUID id;

    @NotNull
    private UUID ownerUserId;

    @NotNull
    private PropertyType type;

    private String country = "CZ";

    @NotBlank
    private String city;
    private String district;
    private String street;
    private String houseNumber;
    private String addressText;

    private BigDecimal lat;
    private BigDecimal lng;

    private String dispozice;
    private Integer roomsCount;
    private Integer floor;
    private Integer totalFloors;

    @NotNull
    private BigDecimal areaM2;
    private BigDecimal balconyAreaM2;
    private BigDecimal cellarAreaM2;

    private Boolean hasBalcony;
    private Boolean hasTerrace;
    private Boolean hasLoggia;
    private Boolean hasGarden;
    private Boolean hasCellar;
    private Boolean hasElevator;

    private ParkingType parkingType;
}
