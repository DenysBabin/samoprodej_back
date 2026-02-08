package samoprodej.samoprodej.config.initializers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.enums.PaymentStatus;
import samoprodej.samoprodej.repository.ListingRepository;
import samoprodej.samoprodej.repository.PropertyRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ListingInitializer {
    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Value("${app.listing.count:50}")
    private int maxListingsToCreate;

    public ListingInitializer(ListingRepository listingRepository,
                              PropertyRepository propertyRepository,
                              UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public void initialize() {
        if (listingRepository.count() == 0) {
            createListings();
        }
    }

    private void createListings() {
        List<Property> properties = propertyRepository.findAll();

        if (properties.isEmpty()) {
            System.out.println("Skipping listing initialization: No properties found.");
            return;
        }

        Map<UUID, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<Listing> listings = new ArrayList<>();
        int counter = 0;

        for (Property property : properties) {
            if (counter >= maxListingsToCreate) break;

            User owner = userMap.get(property.getOwnerUserId());

            if (owner == null) continue;

            int basePricePerMeter = 250 + random.nextInt(150);
            int rentMonthly = (int) (property.getAreaM2().doubleValue() * basePricePerMeter);

            rentMonthly = (rentMonthly / 100) * 100;

            Listing listing = new Listing(property, owner, rentMonthly);

            int utilities = rentMonthly / 5;
            listing.setUtilitiesMonthly((utilities / 100) * 100);

            listing.setDepositKauce(rentMonthly * (random.nextInt(2) + 1));

            listing.setMaxTenants((short) (random.nextInt(4) + 1));

            listing.setPetsAllowed(random.nextBoolean());
            listing.setSmokingAllowed(false);
            listing.setChildrenAllowed(true);

            if (random.nextInt(10) < 7) {
                listing.publish();
                listing.setPaymentStatus(PaymentStatus.UNPAID);
            } else {
                listing.setStatus(ListingStatus.DRAFT);
                listing.setPaymentStatus(PaymentStatus.UNPAID);
            }

            listings.add(listing);
            counter++;
        }

        listingRepository.saveAll(listings);
        System.out.println("Created " + listings.size() + " listings.");
    }
}