package samoprodej.samoprodej.service;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import samoprodej.samoprodej.dto.CreateListingRequest;
import samoprodej.samoprodej.dto.ListingResponse;
import samoprodej.samoprodej.dto.PatchListingRequest;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.repository.ListingRepository;
import samoprodej.samoprodej.repository.PropertyRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ListingResponse create(CreateListingRequest req, UUID ownerId) {
        Property property = propertyRepository.findById(req.propertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Listing listing = new Listing(property, owner, req.rentMonthly());
        listing.setDepositKauce(req.depositKauce());
        listing.setUtilitiesMonthly(req.utilitiesMonthly());
        listing.setPetsAllowed(req.petsAllowed());
        listing.setSmokingAllowed(req.smokingAllowed());
        listing.setChildrenAllowed(req.childrenAllowed());
        listing.setMaxTenants(req.maxTenants());

        Listing saved = listingRepository.save(listing);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ListingResponse getById(UUID id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        return toResponse(listing);
    }

    public ListingResponse patch(UUID id, PatchListingRequest req) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (req.status() != null) listing.setStatus(req.status());
        if (req.paymentStatus() != null) listing.setPaymentStatus(req.paymentStatus());
        if (req.rentMonthly() != null) listing.setRentMonthly(req.rentMonthly());
        if (req.depositKauce() != null) listing.setDepositKauce(req.depositKauce());
        if (req.utilitiesMonthly() != null) listing.setUtilitiesMonthly(req.utilitiesMonthly());
        if (req.petsAllowed() != null) listing.setPetsAllowed(req.petsAllowed());
        if (req.smokingAllowed() != null) listing.setSmokingAllowed(req.smokingAllowed());
        if (req.childrenAllowed() != null) listing.setChildrenAllowed(req.childrenAllowed());
        if (req.maxTenants() != null) listing.setMaxTenants(req.maxTenants());

        return toResponse(listing);
    }

    public void publish(UUID id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        listing.publish();
    }

    private ListingResponse toResponse(Listing l) {
        return new ListingResponse(
                l.getId(),
                l.getProperty().getId(),
                l.getOwner().getId(),
                l.getStatus(),
                l.getPaymentStatus(),
                l.getPublishedAt(),
                l.getRentMonthly(),
                l.getDepositKauce(),
                l.getUtilitiesMonthly(),
                l.getPetsAllowed(),
                l.getSmokingAllowed(),
                l.getChildrenAllowed(),
                l.getMaxTenants()
        );
    }
}

