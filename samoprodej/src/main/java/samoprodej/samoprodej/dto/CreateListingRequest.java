package samoprodej.samoprodej.dto;

import java.util.UUID;

public record CreateListingRequest(
        UUID propertyId,
        Integer rentMonthly,
        Integer depositKauce,
        Integer utilitiesMonthly,
        Boolean petsAllowed,
        Boolean smokingAllowed,
        Boolean childrenAllowed,
        Short maxTenants

) {
}
