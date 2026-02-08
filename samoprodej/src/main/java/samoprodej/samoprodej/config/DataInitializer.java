package samoprodej.samoprodej.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.enums.ParkingType;
import samoprodej.samoprodej.enums.PaymentStatus;
import samoprodej.samoprodej.enums.PropertyType;
import samoprodej.samoprodej.enums.Role;
import samoprodej.samoprodej.enums.UserStatus;
import samoprodej.samoprodej.repository.ListingRepository;
import samoprodej.samoprodej.repository.PropertyRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner dbInitializer(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            ListingRepository listingRepository
    ) {
        return args -> {
            System.out.println("Initializing mock data...");

            // === 1. Create Users ===
            User admin = createUserIfNotExists(userRepository, "admin@samoprodej.com", "Super", "Admin",
                    "+420111111111", Role.ADMIN, Language.EN);
            
            User owner = createUserIfNotExists(userRepository, "owner@samoprodej.com", "Petr", "Vlasnik",
                    "+420222222222", Role.OWNER, Language.CS);
            
            User tenant = createUserIfNotExists(userRepository, "tenant@samoprodej.com", "Jan", "Najemnik",
                    "+420333333333", Role.TENANT, Language.UA);

            // === 2. Create Properties ===
            if (owner != null) {
                List<Property> properties = createTestProperties(propertyRepository, owner.getId());
                
                // === 3. Create Listings ===
                if (!properties.isEmpty()) {
                    createTestListings(listingRepository, properties, owner);
                }
            }

            System.out.println(">>> Mock data initialization completed!");
        };
    }

    private User createUserIfNotExists(UserRepository repository, String email, String firstName,
                                      String lastName, String phone, Role role, Language lang) {
        if (!repository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setPasswordHash(email.split("@")[0] + "123");
            user.setStatus(UserStatus.ACTIVE);
            user.setAuthProvider(AuthProvider.LOCAL);
            user.setPreferredLang(lang);
            user.setRole(role);

            repository.save(user);
            System.out.println(">>> SUCCESS: Created user '" + email + "' (Role: " + role + ")");
            return user;
        } else {
            Optional<User> existing = repository.findByEmail(email);
            return existing.orElse(null);
        }
    }

    private List<Property> createTestProperties(PropertyRepository repository, java.util.UUID ownerId) {
        java.util.List<Property> createdProperties = new java.util.ArrayList<>();

        // Property 1: Квартира в Праге (APARTMENT)
        String address1 = "Вацлавская площадь 1, Прага 1";
        if (repository.findByAddressTextContainingIgnoreCase("Вацлавская площадь 1").isEmpty()) {
            Property property1 = new Property();
            property1.setOwnerUserId(ownerId);
            property1.setType(PropertyType.APARTMENT);
            property1.setCountry("CZ");
            property1.setCity("Прага");
            property1.setDistrict("Прага 1");
            property1.setStreet("Вацлавская площадь");
            property1.setHouseNumber("1");
            property1.setAddressText(address1);
            property1.setLat(new BigDecimal("50.0833"));
            property1.setLng(new BigDecimal("14.4167"));
            property1.setDispozice("2+kk");
            property1.setRoomsCount(2);
            property1.setFloor(3);
            property1.setTotalFloors(5);
            property1.setAreaM2(new BigDecimal("45.5"));
            property1.setBalconyAreaM2(new BigDecimal("5.0"));
            property1.setHasBalcony(true);
            property1.setHasElevator(true);
            property1.setParkingType(ParkingType.STREET);

            repository.save(property1);
            createdProperties.add(property1);
            System.out.println(">>> SUCCESS: Created Property - " + address1);
        }

        // Property 2: Дом в Брно (HOUSE)
        String address2 = "Свободы 15, Брно";
        if (repository.findByAddressTextContainingIgnoreCase("Свободы 15").isEmpty()) {
            Property property2 = new Property();
            property2.setOwnerUserId(ownerId);
            property2.setType(PropertyType.HOUSE);
            property2.setCountry("CZ");
            property2.setCity("Брно");
            property2.setStreet("Свободы");
            property2.setHouseNumber("15");
            property2.setAddressText(address2);
            property2.setLat(new BigDecimal("49.1951"));
            property2.setLng(new BigDecimal("16.6068"));
            property2.setDispozice("4+kk");
            property2.setRoomsCount(4);
            property2.setFloor(1);
            property2.setTotalFloors(2);
            property2.setAreaM2(new BigDecimal("120.0"));
            property2.setBalconyAreaM2(new BigDecimal("10.0"));
            property2.setCellarAreaM2(new BigDecimal("15.0"));
            property2.setHasBalcony(true);
            property2.setHasGarden(true);
            property2.setHasCellar(true);
            property2.setParkingType(ParkingType.GARAGE);

            repository.save(property2);
            createdProperties.add(property2);
            System.out.println(">>> SUCCESS: Created Property - " + address2);
        }

        // Property 3: Комната в Праге (ROOM)
        String address3 = "Карлова 10, Прага 2";
        if (repository.findByAddressTextContainingIgnoreCase("Карлова 10").isEmpty()) {
            Property property3 = new Property();
            property3.setOwnerUserId(ownerId);
            property3.setType(PropertyType.ROOM);
            property3.setCountry("CZ");
            property3.setCity("Прага");
            property3.setDistrict("Прага 2");
            property3.setStreet("Карлова");
            property3.setHouseNumber("10");
            property3.setAddressText(address3);
            property3.setLat(new BigDecimal("50.0755"));
            property3.setLng(new BigDecimal("14.4378"));
            property3.setDispozice("1+0");
            property3.setRoomsCount(1);
            property3.setFloor(2);
            property3.setTotalFloors(4);
            property3.setAreaM2(new BigDecimal("15.0"));
            property3.setHasBalcony(false);
            property3.setParkingType(ParkingType.NONE);

            repository.save(property3);
            createdProperties.add(property3);
            System.out.println(">>> SUCCESS: Created Property - " + address3);
        }

        // Property 4: Квартира в Праге 2 (APARTMENT)
        String address4 = "Виноградская 25, Прага 2";
        if (repository.findByAddressTextContainingIgnoreCase("Виноградская 25").isEmpty()) {
            Property property4 = new Property();
            property4.setOwnerUserId(ownerId);
            property4.setType(PropertyType.APARTMENT);
            property4.setCountry("CZ");
            property4.setCity("Прага");
            property4.setDistrict("Прага 2");
            property4.setStreet("Виноградская");
            property4.setHouseNumber("25");
            property4.setAddressText(address4);
            property4.setLat(new BigDecimal("50.0700"));
            property4.setLng(new BigDecimal("14.4300"));
            property4.setDispozice("3+1");
            property4.setRoomsCount(3);
            property4.setFloor(1);
            property4.setTotalFloors(3);
            property4.setAreaM2(new BigDecimal("65.0"));
            property4.setBalconyAreaM2(new BigDecimal("8.0"));
            property4.setHasBalcony(true);
            property4.setHasLoggia(true);
            property4.setHasElevator(false);
            property4.setParkingType(ParkingType.GARAGE_SPACE);

            repository.save(property4);
            createdProperties.add(property4);
            System.out.println(">>> SUCCESS: Created Property - " + address4);
        }

        // Property 5: Квартира в Праге 1 (APARTMENT)
        String address5 = "Староместская площадь 5, Прага 1";
        if (repository.findByAddressTextContainingIgnoreCase("Староместская площадь 5").isEmpty()) {
            Property property5 = new Property();
            property5.setOwnerUserId(ownerId);
            property5.setType(PropertyType.APARTMENT);
            property5.setCountry("CZ");
            property5.setCity("Прага");
            property5.setDistrict("Прага 1");
            property5.setStreet("Староместская площадь");
            property5.setHouseNumber("5");
            property5.setAddressText(address5);
            property5.setLat(new BigDecimal("50.0875"));
            property5.setLng(new BigDecimal("14.4214"));
            property5.setDispozice("1+kk");
            property5.setRoomsCount(1);
            property5.setFloor(4);
            property5.setTotalFloors(6);
            property5.setAreaM2(new BigDecimal("35.0"));
            property5.setHasBalcony(false);
            property5.setHasElevator(true);
            property5.setParkingType(ParkingType.NONE);

            repository.save(property5);
            createdProperties.add(property5);
            System.out.println(">>> SUCCESS: Created Property - " + address5);
        }

        return createdProperties;
    }

    private void createTestListings(ListingRepository repository, List<Property> properties, User owner) {
        if (properties.isEmpty()) {
            return;
        }

        // Listing 1: Опубликованное объявление (PUBLISHED)
        Property property1 = properties.get(0);
        if (repository.findByPropertyId(property1.getId()).isEmpty()) {
            Listing listing1 = new Listing(property1, owner, 15000);
            listing1.setStatus(ListingStatus.PUBLISHED);
            listing1.setPaymentStatus(PaymentStatus.SUCCEDED);
            listing1.setPublishedAt(Instant.now());
            listing1.setDepositKauce(30000);
            listing1.setUtilitiesMonthly(2000);
            listing1.setPetsAllowed(true);
            listing1.setSmokingAllowed(false);
            listing1.setChildrenAllowed(true);
            listing1.setMaxTenants((short) 2);

            repository.save(listing1);
            System.out.println(">>> SUCCESS: Created Listing - PUBLISHED (Rent: 15000 CZK)");
        }

        // Listing 2: Черновик (DRAFT)
        if (properties.size() > 1) {
            Property property2 = properties.get(1);
            if (repository.findByPropertyId(property2.getId()).isEmpty()) {
                Listing listing2 = new Listing(property2, owner, 12000);
                listing2.setStatus(ListingStatus.DRAFT);
                listing2.setPaymentStatus(PaymentStatus.UNPAID);
                listing2.setUtilitiesMonthly(1500);
                listing2.setPetsAllowed(false);
                listing2.setSmokingAllowed(false);
                listing2.setChildrenAllowed(true);
                listing2.setMaxTenants((short) 3);

                repository.save(listing2);
                System.out.println(">>> SUCCESS: Created Listing - DRAFT (Rent: 12000 CZK)");
            }
        }

        // Listing 3: Сданное в аренду (RENTED)
        if (properties.size() > 2) {
            Property property3 = properties.get(2);
            if (repository.findByPropertyId(property3.getId()).isEmpty()) {
                Listing listing3 = new Listing(property3, owner, 18000);
                listing3.setStatus(ListingStatus.RENTED);
                listing3.setPaymentStatus(PaymentStatus.SUCCEDED);
                listing3.setPublishedAt(Instant.now().minusSeconds(86400 * 30)); // 30 days ago
                listing3.setDepositKauce(36000);
                listing3.setUtilitiesMonthly(2500);
                listing3.setPetsAllowed(true);
                listing3.setSmokingAllowed(false);
                listing3.setChildrenAllowed(true);
                listing3.setMaxTenants((short) 4);

                repository.save(listing3);
                System.out.println(">>> SUCCESS: Created Listing - RENTED (Rent: 18000 CZK)");
            }
        }
    }
}