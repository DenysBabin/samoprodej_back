package samoprodej.samoprodej.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import samoprodej.samoprodej.entity.PropertyMedia;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyMediaRepository extends JpaRepository<PropertyMedia, UUID> {
    List<PropertyMedia> findByPropertyIdOrderBySortOrderAsc(UUID propertyId);
}