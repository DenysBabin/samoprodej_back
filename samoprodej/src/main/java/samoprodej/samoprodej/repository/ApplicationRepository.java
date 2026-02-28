package samoprodej.samoprodej.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import samoprodej.samoprodej.entity.Application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    Optional<Application> findByListingIdAndTenantId(UUID listingId, UUID tenantId);
    List<Application> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    List<Application> findAllByListingIdOrderByCreatedAtDesc(UUID listingId);
}