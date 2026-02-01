package samoprodej.samoprodej.service;

import org.springframework.stereotype.Service;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.repository.PropertyRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository repository;

    public PropertyService(PropertyRepository repository) {
        this.repository = repository;
    }

    public Property createProperty(Property property) {
        if (property.getAddressText() == null || property.getAddressText().isEmpty()) {
            generateAddressText(property);
        }
        return repository.save(property);
    }

    public List<Property> getAllProperties() {
        return repository.findAll();
    }

    public Property getPropertyById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
    }

    public List<Property> searchByCity(String city) {
        return repository.findByCityContainingIgnoreCase(city.trim());
    }

    public List<Property> searchByAddress(String fragment) {
        return repository.findByAddressTextContainingIgnoreCase(fragment.trim());
    }

    public Property updateProperty(UUID id, Property updatedDetails) {
        Property existing = getPropertyById(id);

        existing.setCity(updatedDetails.getCity());
        existing.setDistrict(updatedDetails.getDistrict());
        existing.setStreet(updatedDetails.getStreet());
        existing.setHouseNumber(updatedDetails.getHouseNumber());

        if (updatedDetails.getAddressText() != null && !updatedDetails.getAddressText().isEmpty()) {
            existing.setAddressText(updatedDetails.getAddressText());
        } else {
            generateAddressText(existing);
        }

        existing.setLat(updatedDetails.getLat());
        existing.setLng(updatedDetails.getLng());

        existing.setDispozice(updatedDetails.getDispozice());
        existing.setRoomsCount(updatedDetails.getRoomsCount());
        existing.setFloor(updatedDetails.getFloor());
        existing.setTotalFloors(updatedDetails.getTotalFloors());
        existing.setAreaM2(updatedDetails.getAreaM2());
        existing.setBalconyAreaM2(updatedDetails.getBalconyAreaM2());
        existing.setCellarAreaM2(updatedDetails.getCellarAreaM2());

        existing.setHasBalcony(updatedDetails.getHasBalcony());
        existing.setHasTerrace(updatedDetails.getHasTerrace());
        existing.setHasLoggia(updatedDetails.getHasLoggia());
        existing.setHasGarden(updatedDetails.getHasGarden());
        existing.setHasCellar(updatedDetails.getHasCellar());
        existing.setHasElevator(updatedDetails.getHasElevator());
        existing.setParkingType(updatedDetails.getParkingType());


        return repository.save(existing);
    }

    public void deleteProperty(UUID id) {
        repository.deleteById(id);
    }

    private void generateAddressText(Property p) {
        StringBuilder sb = new StringBuilder();
        if (p.getStreet() != null && !p.getStreet().isEmpty()) {
            sb.append(p.getStreet());
            if (p.getHouseNumber() != null) sb.append(" ").append(p.getHouseNumber());
        }
        if (p.getCity() != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(p.getCity());
        }
        p.setAddressText(sb.toString());
    }
}