package samoprodej.samoprodej.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.enums.ListingStatus;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    Page<Listing> findAll(Pageable pageable);

    Page<Listing> findByStatus(ListingStatus status, Pageable pageable);

    Page<Listing> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<Listing> findByPropertyId(UUID propertyId, Pageable pageable);

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByOwnerId(UUID ownerId);

    List<Listing> findByPropertyId(UUID propertyId);
}
