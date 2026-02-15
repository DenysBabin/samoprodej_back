package samoprodej.samoprodej.mapper;

import org.springframework.stereotype.Component;
import samoprodej.samoprodej.dto.property_media.CreatePropertyMediaRequest;
import samoprodej.samoprodej.dto.property_media.PropertyMediaDTO;
import samoprodej.samoprodej.dto.property_media.PropertyMediaResponse;
import samoprodej.samoprodej.dto.property_media.UpdatePropertyMediaRequest;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.PropertyMedia;

@Component
public class PropertyMediaMapper {

    public PropertyMediaResponse toResponse(PropertyMedia entity) {
        if (entity == null) return null;

        return new PropertyMediaResponse(
                entity.getId(),
                entity.getProperty().getId(),
                entity.getType(),
                entity.getUrl(),
                entity.getPreviewUrl(),
                entity.getSortOrder(),
                entity.getCreatedAt()
        );
    }

    public PropertyMedia toEntity(CreatePropertyMediaRequest request, Property property) {
        if (request == null) return null;

        PropertyMedia media = new PropertyMedia(property, request.type(), request.url());
        media.setPreviewUrl(request.previewUrl());

        if (request.sortOrder() != null) {
            media.setSortOrder(request.sortOrder());
        }

        return media;
    }

    public void updateEntityFromDto(UpdatePropertyMediaRequest request, PropertyMedia entity) {
        if (request == null || entity == null) return;

        if (request.previewUrl() != null) {
            entity.setPreviewUrl(request.previewUrl());
        }

        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
    }

    // Legacy methods for backward compatibility
    @Deprecated
    public PropertyMediaDTO toDto(PropertyMedia entity) {
        if (entity == null) return null;

        PropertyMediaDTO dto = new PropertyMediaDTO();
        dto.setId(entity.getId());
        dto.setPropertyId(entity.getProperty().getId());
        dto.setType(entity.getType());
        dto.setUrl(entity.getUrl());
        dto.setPreviewUrl(entity.getPreviewUrl());
        dto.setSortOrder(entity.getSortOrder());

        return dto;
    }

    @Deprecated
    public PropertyMedia toEntity(PropertyMediaDTO dto, Property property) {
        if (dto == null) return null;

        PropertyMedia media = new PropertyMedia(property, dto.getType(), dto.getUrl());
        media.setPreviewUrl(dto.getPreviewUrl());

        if (dto.getSortOrder() != null) {
            media.setSortOrder(dto.getSortOrder());
        }

        return media;
    }
}