package samoprodej.samoprodej.config.initializers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.ParkingType;
import samoprodej.samoprodej.enums.PropertyType;
import samoprodej.samoprodej.enums.Role;
import samoprodej.samoprodej.repository.PropertyRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.math.BigDecimal;
import java.util.*;

@Component
public class PropertyInitializer {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Value("${app.property.count:50}")
    private int propertyCount;

    private final String[] CITIES = {"Praha", "Brno", "Ostrava", "Plzeň"};
    private final String[] STREETS = {"Václavské náměstí", "Dlouhá", "Pařížská", "Masarykova", "Nádražní"};
    private final String[] DISPOSITIONS = {"1+kk", "2+kk", "2+1", "3+kk", "3+1", "4+kk"};

    public PropertyInitializer(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public void initialize() {
        if (propertyRepository.count() == 0) {
            System.out.println("Initializing mock properties...");
            createProperties();
        }
    }

    private void createProperties() {
        List<User> owners = userRepository.findByRole(Role.OWNER);

        if (owners.isEmpty()) {
            System.out.println("Cannot create properties: No users found in DB.");
            return;
        }

        List<Property> properties = new ArrayList<>();

        for (int i = 1; i <= propertyCount; i++) {
            Property property = new Property();


            User randomOwner = owners.get(random.nextInt(owners.size()));
            property.setOwnerUserId(randomOwner.getId());

            PropertyType[] types = PropertyType.values();
            property.setType(types[random.nextInt(types.length)]);

            property.setCountry("CZ");

            String city = CITIES[random.nextInt(CITIES.length)];
            String street = STREETS[random.nextInt(STREETS.length)];
            String houseNum = String.valueOf(random.nextInt(100) + 1);

            property.setCity(city);
            property.setCityRaw(city.toLowerCase(Locale.ROOT));
            property.setStreet(street);
            property.setHouseNumber(houseNum);
            property.setAddressText(street + " " + houseNum + ", " + city);

            double randomArea = 30 + (120 * random.nextDouble());
            property.setAreaM2(BigDecimal.valueOf(randomArea));


            property.setDispozice(DISPOSITIONS[random.nextInt(DISPOSITIONS.length)]);

            int totalFloors = random.nextInt(10) + 1;
            property.setTotalFloors(totalFloors);
            property.setFloor(random.nextInt(totalFloors) + 1);
            property.setRoomsCount(random.nextInt(5) + 1);

            property.setLat(BigDecimal.valueOf(50.0 + random.nextDouble()));
            property.setLng(BigDecimal.valueOf(14.4 + random.nextDouble()));

            property.setHasBalcony(random.nextBoolean());
            property.setHasTerrace(random.nextBoolean());
            property.setHasLoggia(random.nextBoolean());
            property.setHasGarden(random.nextBoolean());
            property.setHasCellar(random.nextBoolean());
            property.setHasElevator(totalFloors > 3);

            if (property.getHasBalcony()) property.setBalconyAreaM2(BigDecimal.valueOf(5.5));
            if (property.getHasCellar()) property.setCellarAreaM2(BigDecimal.valueOf(3.0));

            ParkingType[] parkingTypes = ParkingType.values();
            property.setParkingType(parkingTypes[random.nextInt(parkingTypes.length)]);

            properties.add(property);
        }

        propertyRepository.saveAll(properties);
        System.out.println("Created " + properties.size() + " properties.");
    }
}