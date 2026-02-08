package samoprodej.samoprodej.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.enums.ListingStatus;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByStatus(ListingStatus status);
    
    @Query("SELECT l FROM Listing l WHERE l.owner.id = :ownerId")
    List<Listing> findByOwnerId(@Param("ownerId") UUID ownerId);
    
    @Query("SELECT l FROM Listing l WHERE l.property.id = :propertyId")
    List<Listing> findByPropertyId(@Param("propertyId") UUID propertyId);
}
