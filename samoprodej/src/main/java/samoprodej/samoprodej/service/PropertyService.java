package samoprodej.samoprodej.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import samoprodej.samoprodej.dto.PropertyDTO;
import samoprodej.samoprodej.dto.PropertyMediaDTO;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.PropertyMedia;
import samoprodej.samoprodej.mapper.PropertyMapper;
import samoprodej.samoprodej.mapper.PropertyMediaMapper;
import samoprodej.samoprodej.repository.PropertyMediaRepository;
import samoprodej.samoprodej.repository.PropertyRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PropertyService {

    private final PropertyRepository repository;
    private final PropertyMediaRepository mediaRepository;
    private final PropertyMapper mapper;
    private final PropertyMediaMapper mediaMapper;

    public PropertyService(PropertyRepository repository,
                           PropertyMediaRepository mediaRepository,
                           PropertyMapper mapper,
                           PropertyMediaMapper mediaMapper) {
        this.repository = repository;
        this.mediaRepository = mediaRepository;
        this.mapper = mapper;
        this.mediaMapper = mediaMapper;
    }

    public PropertyDTO createProperty(PropertyDTO dto) {
        log.info("Creating property for owner: {}", dto.getOwnerUserId());
        Property property = mapper.toEntity(dto);

        if (property.getAddressText() == null || property.getAddressText().isEmpty()) {
            generateAddressText(property);
        }

        Property saved = repository.save(property);
        log.info("Property created with ID: {}", saved.getId());

        return mapper.toDto(saved);
    }

    public List<PropertyDTO> getAllProperties() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public PropertyDTO getPropertyById(UUID id) {
        Property property = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        return mapper.toDto(property);
    }

    public List<PropertyDTO> searchByCity(String city) {
        return repository.findByCityContainingIgnoreCase(city.trim()).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public PropertyDTO updateProperty(UUID id, PropertyDTO dto) {
        log.info("Updating property ID: {}", id);
        Property existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        mapper.updateEntityFromDto(dto, existing);

        if (existing.getAddressText() == null || existing.getAddressText().isEmpty()) {
            generateAddressText(existing);
        }

        Property updated = repository.save(existing);
        return mapper.toDto(updated);
    }

    public void deleteProperty(UUID id) {
        log.warn("Deleting property ID: {}", id);
        repository.deleteById(id);
    }

    public PropertyMediaDTO addMedia(UUID propertyId, PropertyMediaDTO mediaDto) {
        log.info("Adding media to property ID: {}", propertyId);

        Property property = repository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        PropertyMedia media = mediaMapper.toEntity(mediaDto, property);
        PropertyMedia saved = mediaRepository.save(media);

        return mediaMapper.toDto(saved);
    }

    public List<PropertyMediaDTO> getMediaForProperty(UUID propertyId) {
        if (!repository.existsById(propertyId)) {
            throw new RuntimeException("Property not found");
        }
        return mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId).stream()
                .map(mediaMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteMedia(UUID mediaId) {
        log.warn("Deleting media ID: {}", mediaId);
        mediaRepository.deleteById(mediaId);
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