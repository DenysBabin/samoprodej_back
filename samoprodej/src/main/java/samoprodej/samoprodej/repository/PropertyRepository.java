package samoprodej.samoprodej.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.enums.PropertyStatus;
import samoprodej.samoprodej.enums.PropertyType;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    List<Property> findByCityNormContainingIgnoreCase(String cityNorm);

    List<Property> findByOwnerId(UUID ownerId);

    List<Property> findByStatus(PropertyStatus status);

    List<Property> findByTypeAndSizeM2GreaterThanEqual(PropertyType type, Integer minSize);
}