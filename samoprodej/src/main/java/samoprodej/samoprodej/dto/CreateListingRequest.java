package samoprodej.samoprodej.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateListingRequest(
        @NotNull UUID propertyId,
        @NotNull @Positive Integer rentMonthly,
        @Positive Integer depositKauce,
        @Positive Integer utilitiesMonthly,
        Boolean petsAllowed,
        Boolean smokingAllowed,
        Boolean childrenAllowed,
        @Positive Short maxTenants
) {
}
