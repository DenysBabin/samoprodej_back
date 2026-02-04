package samoprodej.samoprodej.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import samoprodej.samoprodej.entity.Listing;

import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
}
