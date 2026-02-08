package samoprodej.samoprodej.dto.listing;

import jakarta.validation.constraints.Positive;
import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.enums.PaymentStatus;

public record PatchListingRequest(
        ListingStatus status,
        PaymentStatus paymentStatus,
        @Positive Integer rentMonthly,
        @Positive Integer depositKauce,
        @Positive Integer utilitiesMonthly,
        Boolean petsAllowed,
        Boolean smokingAllowed,
        Boolean childrenAllowed,
        @Positive Short maxTenants
) {
}
