package samoprodej.samoprodej.service;

import org.springframework.stereotype.Service;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.enums.PropertyStatus;
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
        normalizeAddress(property);

        if (property.getStatus() == null) {
            property.setStatus(PropertyStatus.DRAFT);
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
        String normCity = city.trim().toLowerCase();
        return repository.findByCityNormContainingIgnoreCase(normCity);
    }

    public Property updateProperty(UUID id, Property updatedDetails) {
        Property existing = getPropertyById(id);

        existing.setType(updatedDetails.getType());
        existing.setStatus(updatedDetails.getStatus());
        existing.setCountry(updatedDetails.getCountry());
        existing.setSizeM2(updatedDetails.getSizeM2());
        existing.setRooms(updatedDetails.getRooms());

        existing.setCityRaw(updatedDetails.getCityRaw());
        existing.setStreetRaw(updatedDetails.getStreetRaw());

        normalizeAddress(existing);

        return repository.save(existing);
    }

    public void deleteProperty(UUID id) {
        repository.deleteById(id);
    }

    private void normalizeAddress(Property property) {
        if (property.getCityRaw() != null) {
            property.setCityNorm(property.getCityRaw().trim().toLowerCase());
        }
        if (property.getStreetRaw() != null) {
            property.setStreetNorm(property.getStreetRaw().trim().toLowerCase());
        }
    }
}