package samoprodej.samoprodej.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import samoprodej.samoprodej.entity.PropertyMedia;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyMediaRepository extends JpaRepository<PropertyMedia, UUID> {
    List<PropertyMedia> findByPropertyIdOrderBySortOrderAsc(UUID propertyId);
    
    @Query("SELECT COALESCE(MAX(pm.sortOrder), -1) FROM PropertyMedia pm WHERE pm.property.id = :propertyId")
    Integer findMaxSortOrderByPropertyId(@Param("propertyId") UUID propertyId);
}