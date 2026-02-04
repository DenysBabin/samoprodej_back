package samoprodej.samoprodej.mapper;

import org.springframework.stereotype.Component;
import samoprodej.samoprodej.dto.PropertyMediaDTO;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.PropertyMedia;

@Component
public class PropertyMediaMapper {

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