package samoprodej.samoprodej.mapper;

import org.springframework.stereotype.Component;
import samoprodej.samoprodej.dto.CreateListingRequest;
import samoprodej.samoprodej.dto.ListingResponse;
import samoprodej.samoprodej.dto.PatchListingRequest;
import samoprodej.samoprodej.dto.UpdateListingRequest;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.User;

@Component
public class ListingMapper {

    public ListingResponse toResponse(Listing entity) {
        if (entity == null) return null;

        return new ListingResponse(
                entity.getId(),
                entity.getProperty().getId(),
                entity.getOwner().getId(),
                entity.getStatus(),
                entity.getPaymentStatus(),
                entity.getPublishedAt(),
                entity.getRentMonthly(),
                entity.getDepositKauce(),
                entity.getUtilitiesMonthly(),
                entity.getPetsAllowed(),
                entity.getSmokingAllowed(),
                entity.getChildrenAllowed(),
                entity.getMaxTenants(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Listing toEntity(CreateListingRequest dto, Property property, User owner) {
        if (dto == null || property == null || owner == null) {
            throw new IllegalArgumentException("CreateListingRequest, Property, and User must not be null");
        }

        Listing listing = new Listing(property, owner, dto.rentMonthly());
        
        if (dto.depositKauce() != null) {
            listing.setDepositKauce(dto.depositKauce());
        }
        if (dto.utilitiesMonthly() != null) {
            listing.setUtilitiesMonthly(dto.utilitiesMonthly());
        }
        if (dto.petsAllowed() != null) {
            listing.setPetsAllowed(dto.petsAllowed());
        }
        if (dto.smokingAllowed() != null) {
            listing.setSmokingAllowed(dto.smokingAllowed());
        }
        if (dto.childrenAllowed() != null) {
            listing.setChildrenAllowed(dto.childrenAllowed());
        }
        if (dto.maxTenants() != null) {
            listing.setMaxTenants(dto.maxTenants());
        }

        return listing;
    }

    public void updateEntityFromDto(UpdateListingRequest dto, Listing entity) {
        if (dto == null || entity == null) return;

        if (dto.status() != null) {
            entity.setStatus(dto.status());
        }
        if (dto.paymentStatus() != null) {
            entity.setPaymentStatus(dto.paymentStatus());
        }
        if (dto.rentMonthly() != null) {
            entity.setRentMonthly(dto.rentMonthly());
        }
        if (dto.depositKauce() != null) {
            entity.setDepositKauce(dto.depositKauce());
        }
        if (dto.utilitiesMonthly() != null) {
            entity.setUtilitiesMonthly(dto.utilitiesMonthly());
        }
        if (dto.petsAllowed() != null) {
            entity.setPetsAllowed(dto.petsAllowed());
        }
        if (dto.smokingAllowed() != null) {
            entity.setSmokingAllowed(dto.smokingAllowed());
        }
        if (dto.childrenAllowed() != null) {
            entity.setChildrenAllowed(dto.childrenAllowed());
        }
        if (dto.maxTenants() != null) {
            entity.setMaxTenants(dto.maxTenants());
        }
    }

    public void updateEntityFromDto(PatchListingRequest dto, Listing entity) {
        if (dto == null || entity == null) return;

        if (dto.status() != null) {
            entity.setStatus(dto.status());
        }
        if (dto.paymentStatus() != null) {
            entity.setPaymentStatus(dto.paymentStatus());
        }
        if (dto.rentMonthly() != null) {
            entity.setRentMonthly(dto.rentMonthly());
        }
        if (dto.depositKauce() != null) {
            entity.setDepositKauce(dto.depositKauce());
        }
        if (dto.utilitiesMonthly() != null) {
            entity.setUtilitiesMonthly(dto.utilitiesMonthly());
        }
        if (dto.petsAllowed() != null) {
            entity.setPetsAllowed(dto.petsAllowed());
        }
        if (dto.smokingAllowed() != null) {
            entity.setSmokingAllowed(dto.smokingAllowed());
        }
        if (dto.childrenAllowed() != null) {
            entity.setChildrenAllowed(dto.childrenAllowed());
        }
        if (dto.maxTenants() != null) {
            entity.setMaxTenants(dto.maxTenants());
        }
    }
}
