package samoprodej.samoprodej.mapper;

import org.springframework.stereotype.Component;
import samoprodej.samoprodej.dto.CreatePropertyRequest;
import samoprodej.samoprodej.dto.PropertyDTO;
import samoprodej.samoprodej.dto.PropertyResponse;
import samoprodej.samoprodej.dto.UpdatePropertyRequest;
import samoprodej.samoprodej.entity.Property;

@Component
public class PropertyMapper {

    // New methods for PropertyResponse
    public PropertyResponse toResponse(Property entity) {
        if (entity == null) return null;

        return new PropertyResponse(
                entity.getId(),
                entity.getOwnerUserId(),
                entity.getType(),
                entity.getCountry(),
                entity.getCity(),
                entity.getDistrict(),
                entity.getStreet(),
                entity.getHouseNumber(),
                entity.getAddressText(),
                entity.getLat(),
                entity.getLng(),
                entity.getDispozice(),
                entity.getRoomsCount(),
                entity.getFloor(),
                entity.getTotalFloors(),
                entity.getAreaM2(),
                entity.getBalconyAreaM2(),
                entity.getCellarAreaM2(),
                entity.getHasBalcony(),
                entity.getHasTerrace(),
                entity.getHasLoggia(),
                entity.getHasGarden(),
                entity.getHasCellar(),
                entity.getHasElevator(),
                entity.getParkingType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Property toEntity(CreatePropertyRequest dto) {
        if (dto == null) return null;

        Property property = new Property();
        property.setOwnerUserId(dto.ownerUserId());
        property.setType(dto.type());
        property.setCountry(dto.country() != null ? dto.country() : "CZ");

        property.setCity(dto.city());
        property.setDistrict(dto.district());
        property.setStreet(dto.street());
        property.setHouseNumber(dto.houseNumber());
        property.setAddressText(dto.addressText());
        property.setLat(dto.lat());
        property.setLng(dto.lng());
        property.setDispozice(dto.dispozice());
        property.setRoomsCount(dto.roomsCount());
        property.setFloor(dto.floor());
        property.setTotalFloors(dto.totalFloors());
        property.setAreaM2(dto.areaM2());
        property.setBalconyAreaM2(dto.balconyAreaM2());
        property.setCellarAreaM2(dto.cellarAreaM2());
        property.setHasBalcony(dto.hasBalcony());
        property.setHasTerrace(dto.hasTerrace());
        property.setHasLoggia(dto.hasLoggia());
        property.setHasGarden(dto.hasGarden());
        property.setHasCellar(dto.hasCellar());
        property.setHasElevator(dto.hasElevator());
        property.setParkingType(dto.parkingType());

        return property;
    }

    public void updateEntityFromDto(UpdatePropertyRequest dto, Property entity) {
        if (dto == null || entity == null) return;

        if (dto.city() != null && !dto.city().isEmpty()) {
            entity.setCity(dto.city());
        }
        if (dto.district() != null) {
            entity.setDistrict(dto.district());
        }
        if (dto.street() != null) {
            entity.setStreet(dto.street());
        }
        if (dto.houseNumber() != null) {
            entity.setHouseNumber(dto.houseNumber());
        }
        if (dto.addressText() != null && !dto.addressText().isEmpty()) {
            entity.setAddressText(dto.addressText());
        }
        if (dto.lat() != null) {
            entity.setLat(dto.lat());
        }
        if (dto.lng() != null) {
            entity.setLng(dto.lng());
        }
        if (dto.dispozice() != null) {
            entity.setDispozice(dto.dispozice());
        }
        if (dto.roomsCount() != null) {
            entity.setRoomsCount(dto.roomsCount());
        }
        if (dto.floor() != null) {
            entity.setFloor(dto.floor());
        }
        if (dto.totalFloors() != null) {
            entity.setTotalFloors(dto.totalFloors());
        }
        if (dto.areaM2() != null) {
            entity.setAreaM2(dto.areaM2());
        }
        if (dto.balconyAreaM2() != null) {
            entity.setBalconyAreaM2(dto.balconyAreaM2());
        }
        if (dto.cellarAreaM2() != null) {
            entity.setCellarAreaM2(dto.cellarAreaM2());
        }
        if (dto.hasBalcony() != null) {
            entity.setHasBalcony(dto.hasBalcony());
        }
        if (dto.hasTerrace() != null) {
            entity.setHasTerrace(dto.hasTerrace());
        }
        if (dto.hasLoggia() != null) {
            entity.setHasLoggia(dto.hasLoggia());
        }
        if (dto.hasGarden() != null) {
            entity.setHasGarden(dto.hasGarden());
        }
        if (dto.hasCellar() != null) {
            entity.setHasCellar(dto.hasCellar());
        }
        if (dto.hasElevator() != null) {
            entity.setHasElevator(dto.hasElevator());
        }
        if (dto.parkingType() != null) {
            entity.setParkingType(dto.parkingType());
        }
    }

    // Legacy methods for backward compatibility with PropertyDTO
    @Deprecated
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

    @Deprecated
    public Property toEntity(PropertyDTO dto) {
        if (dto == null) return null;

        Property property = new Property();
        property.setOwnerUserId(dto.getOwnerUserId());
        property.setType(dto.getType());
        property.setCountry(dto.getCountry());

        updateEntityFromDto(dto, property);

        return property;
    }

    @Deprecated
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