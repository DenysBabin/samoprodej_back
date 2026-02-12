package samoprodej.samoprodej.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import samoprodej.samoprodej.dto.property.CreatePropertyRequest;
import samoprodej.samoprodej.dto.property.PropertyDTO;
import samoprodej.samoprodej.dto.property.PropertyResponse;
import samoprodej.samoprodej.dto.property.UpdatePropertyRequest;
import samoprodej.samoprodej.dto.propertymedia.PropertyMediaDTO;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.PropertyMedia;
import samoprodej.samoprodej.mapper.PropertyMapper;
import samoprodej.samoprodej.mapper.PropertyMediaMapper;
import samoprodej.samoprodej.repository.PropertyMediaRepository;
import samoprodej.samoprodej.repository.PropertyRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PropertyService {

    private final PropertyRepository repository;
    private final PropertyMediaRepository mediaRepository;
    private final PropertyMapper mapper;
    private final PropertyMediaMapper mediaMapper;
    private final UserRepository userRepository;

    public PropertyResponse create(CreatePropertyRequest request) {
        log.info("Creating property for owner: {}", request.ownerUserId());

        // Check if owner exists
        if (!userRepository.existsById(request.ownerUserId())) {
            throw new RuntimeException("User not found with id: " + request.ownerUserId());
        }

        Property property = mapper.toEntity(request);

        if (property.getAddressText() == null || property.getAddressText().isEmpty()) {
            generateAddressText(property);
        }

        Property saved = repository.save(property);
        log.info("Property created with ID: {}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyResponse getById(UUID id) {
        Property property = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        return mapper.toResponse(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> searchByCity(String city) {
        return repository.findByCityContainingIgnoreCase(city.trim()).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public PropertyResponse update(UUID id, UpdatePropertyRequest request) {
        log.info("Updating property ID: {}", id);
        Property existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

        mapper.updateEntityFromDto(request, existing);

        if (existing.getAddressText() == null || existing.getAddressText().isEmpty()) {
            generateAddressText(existing);
        }

        Property updated = repository.save(existing);
        log.info("Property updated with ID: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    public void delete(UUID id) {
        log.warn("Deleting property ID: {}", id);
        Property property = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        repository.delete(property);
    }

    // Legacy methods for backward compatibility
    @Deprecated
    @SuppressWarnings("deprecation")
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

    @Deprecated
    @Transactional(readOnly = true)
    @SuppressWarnings("deprecation")
    public List<PropertyDTO> getAllProperties() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Deprecated
    @Transactional(readOnly = true)
    @SuppressWarnings("deprecation")
    public PropertyDTO getPropertyById(UUID id) {
        Property property = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        return mapper.toDto(property);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public List<PropertyDTO> searchByCityOld(String city) {
        return repository.findByCityContainingIgnoreCase(city.trim()).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Deprecated
    @SuppressWarnings("deprecation")
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

    @Deprecated
    public void deleteProperty(UUID id) {
        log.warn("Deleting property ID: {}", id);
        repository.deleteById(id);
    }

    @SuppressWarnings("deprecation")
    public PropertyMediaDTO addMedia(UUID propertyId, PropertyMediaDTO mediaDto) {
        log.info("Adding media to property ID: {}", propertyId);

        Property property = repository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));

        PropertyMedia media = mediaMapper.toEntity(mediaDto, property);
        PropertyMedia saved = mediaRepository.save(media);
        log.info("Media added with ID: {}", saved.getId());

        return mediaMapper.toDto(saved);
    }

    @SuppressWarnings("deprecation")
    @Transactional(readOnly = true)
    public List<PropertyMediaDTO> getMediaForProperty(UUID propertyId) {
        if (!repository.existsById(propertyId)) {
            throw new RuntimeException("Property not found with id: " + propertyId);
        }
        return mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId).stream()
                .map(mediaMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteMedia(UUID mediaId) {
        log.warn("Deleting media ID: {}", mediaId);
        if (!mediaRepository.existsById(mediaId)) {
            throw new RuntimeException("Media not found with id: " + mediaId);
        }
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