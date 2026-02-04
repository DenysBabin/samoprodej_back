package samoprodej.samoprodej.dto;

import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.enums.PaymentStatus;

public record PatchListingRequest(ListingStatus status,
                                  PaymentStatus paymentStatus,
                                  Integer rentMonthly,
                                  Integer depositKauce,
                                  Integer utilitiesMonthly,
                                  Boolean petsAllowed,
                                  Boolean smokingAllowed,
                                  Boolean childrenAllowed,
                                  Short maxTenants) {
}
