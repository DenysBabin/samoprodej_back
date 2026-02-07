package samoprodej.samoprodej.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import samoprodej.samoprodej.dto.propertymedia.CreatePropertyMediaRequest;
import samoprodej.samoprodej.dto.propertymedia.PropertyMediaResponse;
import samoprodej.samoprodej.dto.propertymedia.ReorderMediaRequest;
import samoprodej.samoprodej.dto.propertymedia.UpdatePropertyMediaRequest;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.PropertyMedia;
import samoprodej.samoprodej.enums.MediaType;
import samoprodej.samoprodej.mapper.PropertyMediaMapper;
import samoprodej.samoprodej.repository.PropertyMediaRepository;
import samoprodej.samoprodej.repository.PropertyRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PropertyMediaService {

    private final PropertyMediaRepository mediaRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyMediaMapper mapper;
    private final FileStorageService fileStorageService;

    public PropertyMediaResponse uploadMedia(UUID propertyId, MultipartFile file, MediaType type) {
        log.info("Uploading media file for property ID: {}, type: {}", propertyId, type);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));

        // Check max media count
        long mediaCount = mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId).size();
        // This check will be done later with config, for now just log
        log.debug("Current media count for property {}: {}", propertyId, mediaCount);

        // Calculate next sortOrder
        Integer maxSortOrder = mediaRepository.findMaxSortOrderByPropertyId(propertyId);
        int nextSortOrder = (maxSortOrder == null || maxSortOrder == -1) ? 0 : maxSortOrder + 1;

        // Create PropertyMedia first to get ID
        PropertyMedia media = new PropertyMedia(property, type, "");
        PropertyMedia savedMedia = mediaRepository.save(media);

        try {
            // Save file
            String fileUrl = fileStorageService.saveFile(file, propertyId, savedMedia.getId(), type);
            savedMedia.setUrl(fileUrl);

            // Generate preview for photos using saved file
            String previewUrl = null;
            if (type == MediaType.PHOTO) {
                try {
                    previewUrl = fileStorageService.generatePreviewFromFile(fileUrl, propertyId, savedMedia.getId());
                } catch (Exception e) {
                    log.warn("Failed to generate preview for media {}: {}", savedMedia.getId(), e.getMessage());
                }
            }
            savedMedia.setPreviewUrl(previewUrl);
            savedMedia.setSortOrder(nextSortOrder);

            PropertyMedia finalMedia = mediaRepository.save(savedMedia);
            log.info("Media uploaded successfully with ID: {}", finalMedia.getId());

            return mapper.toResponse(finalMedia);
        } catch (IOException e) {
            // Cleanup: delete the media record if file save failed
            mediaRepository.delete(savedMedia);
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    public PropertyMediaResponse addMediaFromUrl(UUID propertyId, CreatePropertyMediaRequest request) {
        log.info("Adding media from URL for property ID: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));

        // Calculate next sortOrder if not provided
        Integer sortOrder;
        if (request.sortOrder() == null) {
            Integer maxSortOrder = mediaRepository.findMaxSortOrderByPropertyId(propertyId);
            sortOrder = (maxSortOrder == null || maxSortOrder == -1) ? 0 : maxSortOrder + 1;
        } else {
            // Check if sortOrder already exists
            Integer sortOrderToCheck = request.sortOrder();
            List<PropertyMedia> existing = mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId);
            boolean conflict = existing.stream()
                    .anyMatch(m -> m.getSortOrder().equals(sortOrderToCheck));
            if (conflict) {
                throw new RuntimeException("Sort order " + sortOrderToCheck + " already exists for this property");
            }
            sortOrder = sortOrderToCheck;
        }

        PropertyMedia media = mapper.toEntity(request, property);
        media.setSortOrder(sortOrder);

        PropertyMedia saved = mediaRepository.save(media);
        log.info("Media added from URL with ID: {}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PropertyMediaResponse> getMediaForProperty(UUID propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new RuntimeException("Property not found with id: " + propertyId);
        }
        return mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyMediaResponse> reorderMedia(UUID propertyId, ReorderMediaRequest request) {
        log.info("Reordering media for property ID: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));

        List<UUID> mediaIds = request.mediaIds();
        List<PropertyMedia> allMedia = mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId);

        // Verify all mediaIds belong to this property
        List<UUID> propertyMediaIds = allMedia.stream()
                .map(PropertyMedia::getId)
                .collect(Collectors.toList());

        if (!propertyMediaIds.containsAll(mediaIds) || mediaIds.size() != propertyMediaIds.size()) {
            throw new RuntimeException("Some media IDs do not belong to this property");
        }

        // Update sortOrder based on the order in the list
        for (int i = 0; i < mediaIds.size(); i++) {
            UUID mediaId = mediaIds.get(i);
            PropertyMedia media = allMedia.stream()
                    .filter(m -> m.getId().equals(mediaId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));
            media.setSortOrder(i);
        }

        mediaRepository.saveAll(allMedia);
        log.info("Media reordered successfully for property ID: {}", propertyId);

        return allMedia.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public PropertyMediaResponse updateMedia(UUID mediaId, UpdatePropertyMediaRequest request) {
        log.info("Updating media ID: {}", mediaId);

        PropertyMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + mediaId));

        // If sortOrder is being changed, check for conflicts
        if (request.sortOrder() != null && !request.sortOrder().equals(media.getSortOrder())) {
            UUID propertyId = media.getProperty().getId();
            List<PropertyMedia> existing = mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId);
            boolean conflict = existing.stream()
                    .anyMatch(m -> !m.getId().equals(mediaId) && m.getSortOrder().equals(request.sortOrder()));
            if (conflict) {
                throw new RuntimeException("Sort order " + request.sortOrder() + " already exists for this property");
            }
        }

        mapper.updateEntityFromDto(request, media);
        PropertyMedia updated = mediaRepository.save(media);
        log.info("Media updated successfully with ID: {}", updated.getId());

        return mapper.toResponse(updated);
    }

    public void deleteMedia(UUID mediaId) {
        log.warn("Deleting media ID: {}", mediaId);

        PropertyMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + mediaId));

        UUID propertyId = media.getProperty().getId();

        // Delete physical files
        if (media.getUrl() != null && !media.getUrl().isEmpty()) {
            fileStorageService.deleteFile(media.getUrl());
        }
        if (media.getPreviewUrl() != null && !media.getPreviewUrl().isEmpty()) {
            fileStorageService.deleteFile(media.getPreviewUrl());
        }

        // Delete media directory
        fileStorageService.deleteMediaDirectory(propertyId, mediaId);

        // Delete from database
        mediaRepository.delete(media);
        log.info("Media deleted successfully with ID: {}", mediaId);
    }
}
