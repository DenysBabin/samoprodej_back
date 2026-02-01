package samoprodej.samoprodej.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.enums.PropertyType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    List<Property> findByOwnerUserId(UUID ownerUserId);

    List<Property> findByCityContainingIgnoreCase(String city);

    List<Property> findByAddressTextContainingIgnoreCase(String addressFragment);

    List<Property> findByTypeAndAreaM2GreaterThanEqual(PropertyType type, BigDecimal minArea);
}