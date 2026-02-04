package samoprodej.samoprodej.mapper;

import org.springframework.stereotype.Component;
import samoprodej.samoprodej.dto.PropertyDTO;
import samoprodej.samoprodej.entity.Property;

@Component
public class PropertyMapper {

    public PropertyDTO toDto(Property entity) {
        if (entity == null) return null;

        PropertyDTO dto = new PropertyDTO();
        dto.setId(entity.getId());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setType(entity.getType());
        dto.setCountry(entity.getCountry());

        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setStreet(entity.getStreet());
        dto.setHouseNumber(entity.getHouseNumber());
        dto.setAddressText(entity.getAddressText());

        dto.setLat(entity.getLat());
        dto.setLng(entity.getLng());

        dto.setDispozice(entity.getDispozice());
        dto.setRoomsCount(entity.getRoomsCount());
        dto.setFloor(entity.getFloor());
        dto.setTotalFloors(entity.getTotalFloors());
        dto.setAreaM2(entity.getAreaM2());
        dto.setBalconyAreaM2(entity.getBalconyAreaM2());
        dto.setCellarAreaM2(entity.getCellarAreaM2());

        dto.setHasBalcony(entity.getHasBalcony());
        dto.setHasTerrace(entity.getHasTerrace());
        dto.setHasLoggia(entity.getHasLoggia());
        dto.setHasGarden(entity.getHasGarden());
        dto.setHasCellar(entity.getHasCellar());
        dto.setHasElevator(entity.getHasElevator());
        dto.setParkingType(entity.getParkingType());

        return dto;
    }

    public Property toEntity(PropertyDTO dto) {
        if (dto == null) return null;

        Property property = new Property();
        property.setOwnerUserId(dto.getOwnerUserId());
        property.setType(dto.getType());
        property.setCountry(dto.getCountry());

        updateEntityFromDto(dto, property);

        return property;
    }

    public void updateEntityFromDto(PropertyDTO dto, Property entity) {

        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setStreet(dto.getStreet());
        entity.setHouseNumber(dto.getHouseNumber());

        if (dto.getAddressText() != null && !dto.getAddressText().isEmpty()) {
            entity.setAddressText(dto.getAddressText());
        }

        entity.setLat(dto.getLat());
        entity.setLng(dto.getLng());

        entity.setDispozice(dto.getDispozice());
        entity.setRoomsCount(dto.getRoomsCount());
        entity.setFloor(dto.getFloor());
        entity.setTotalFloors(dto.getTotalFloors());
        entity.setAreaM2(dto.getAreaM2());
        entity.setBalconyAreaM2(dto.getBalconyAreaM2());
        entity.setCellarAreaM2(dto.getCellarAreaM2());

        entity.setHasBalcony(dto.getHasBalcony());
        entity.setHasTerrace(dto.getHasTerrace());
        entity.setHasLoggia(dto.getHasLoggia());
        entity.setHasGarden(dto.getHasGarden());
        entity.setHasCellar(dto.getHasCellar());
        entity.setHasElevator(dto.getHasElevator());

        entity.setParkingType(dto.getParkingType());
    }
}