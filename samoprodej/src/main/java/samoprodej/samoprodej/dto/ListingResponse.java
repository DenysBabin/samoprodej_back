package samoprodej.samoprodej.dto;

import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record ListingResponse(
        UUID id,
        UUID propertyId,
        UUID ownerId,
        ListingStatus status,
        PaymentStatus paymentStatus,
        Instant publishedAt,
        Integer rentMonthly,
        Integer depositKauce,
        Integer utilitiesMonthly,
        Boolean petsAllowed,
        Boolean smokingAllowed,
        Boolean childrenAllowed,
        Short maxTenants
) {
}
