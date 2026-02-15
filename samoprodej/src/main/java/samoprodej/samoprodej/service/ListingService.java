package samoprodej.samoprodej.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import samoprodej.samoprodej.dto.common.PageResponse;
import samoprodej.samoprodej.dto.listing.CreateListingRequest;
import samoprodej.samoprodej.dto.listing.ListingResponse;
import samoprodej.samoprodej.dto.listing.PatchListingRequest;
import samoprodej.samoprodej.dto.listing.UpdateListingRequest;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.mapper.ListingMapper;
import samoprodej.samoprodej.repository.ListingRepository;
import samoprodej.samoprodej.repository.PropertyRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ListingMapper mapper;

    public ListingResponse create(CreateListingRequest req, UUID ownerId) {
        log.info("Creating listing for property {} by owner {}", req.propertyId(), ownerId);

        Property property = propertyRepository.findById(req.propertyId())
                .orElseThrow(() -> {
                    log.error("Property not found: {}", req.propertyId());
                    return new RuntimeException("Property not found with id: " + req.propertyId());
                });

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", ownerId);
                    return new RuntimeException("User not found with id: " + ownerId);
                });

        Listing listing = mapper.toEntity(req, property, owner);
        Listing saved = listingRepository.save(listing);

        log.info("Listing created successfully with id: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ListingResponse getById(UUID id) {
        log.debug("Getting listing by id: {}", id);

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Listing not found: {}", id);
                    return new RuntimeException("Listing not found with id: " + id);
                });

        return mapper.toResponse(listing);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getAll() {
        log.debug("Getting all listings (non-paginated)");

        return listingRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all listings with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ListingResponse> getAllPaginated(Pageable pageable) {
        log.debug("Getting all listings with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Listing> listingPage = listingRepository.findAll(pageable);
        Page<ListingResponse> responsePage = listingPage.map(mapper::toResponse);

        return PageResponse.of(responsePage);
    }

    public ListingResponse update(UUID id, UpdateListingRequest req) {
        log.info("Updating listing {} with UpdateListingRequest", id);

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Listing not found: {}", id);
                    return new RuntimeException("Listing not found with id: " + id);
                });

        mapper.updateEntityFromDto(req, listing);
        Listing saved = listingRepository.save(listing);

        log.info("Listing {} updated successfully", id);
        return mapper.toResponse(saved);
    }

    public ListingResponse patch(UUID id, PatchListingRequest req) {
        log.info("Patching listing {} with PatchListingRequest", id);

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Listing not found: {}", id);
                    return new RuntimeException("Listing not found with id: " + id);
                });

        mapper.updateEntityFromDto(req, listing);
        Listing saved = listingRepository.save(listing);

        log.info("Listing {} patched successfully", id);
        return mapper.toResponse(saved);
    }

    public void delete(UUID id) {
        log.info("Deleting listing: {}", id);

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Listing not found: {}", id);
                    return new RuntimeException("Listing not found with id: " + id);
                });

        listingRepository.delete(listing);
        log.info("Listing {} deleted successfully", id);
    }

    public void publish(UUID id) {
        log.info("Publishing listing: {}", id);

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Listing not found: {}", id);
                    return new RuntimeException("Listing not found with id: " + id);
                });

        listing.publish();
        listingRepository.save(listing);

        log.info("Listing {} published successfully", id);
    }

    public ListingResponse unpublish(UUID id) {
        log.info("Unpublishing listing: {}", id);

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Listing not found: {}", id);
                    return new RuntimeException("Listing not found with id: " + id);
                });

        if (listing.getStatus() != ListingStatus.PUBLISHED) {
            log.warn("Listing {} is not published, cannot unpublish", id);
            throw new RuntimeException("Listing is not published, cannot unpublish");
        }

        listing.setStatus(ListingStatus.DRAFT);
        listing.setPublishedAt(null);
        Listing saved = listingRepository.save(listing);

        log.info("Listing {} unpublished successfully", id);
        return mapper.toResponse(saved);
    }

    public ListingResponse archive(UUID id) {
        log.info("Archiving listing: {}", id);

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Listing not found: {}", id);
                    return new RuntimeException("Listing not found with id: " + id);
                });

        listing.setStatus(ListingStatus.ARCHIVED);
        Listing saved = listingRepository.save(listing);

        log.info("Listing {} archived successfully", id);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> searchByStatus(ListingStatus status) {
        log.debug("Searching listings by status: {} (non-paginated)", status);

        return listingRepository.findByStatus(status).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search listings by status with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ListingResponse> searchByStatusPaginated(ListingStatus status, Pageable pageable) {
        log.debug("Searching listings by status: {} with pagination: page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<Listing> listingPage = listingRepository.findByStatus(status, pageable);
        Page<ListingResponse> responsePage = listingPage.map(mapper::toResponse);

        return PageResponse.of(responsePage);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> searchByOwnerId(UUID ownerId) {
        log.debug("Searching listings by ownerId: {} (non-paginated)", ownerId);

        return listingRepository.findByOwnerId(ownerId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search listings by owner ID with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ListingResponse> searchByOwnerIdPaginated(UUID ownerId, Pageable pageable) {
        log.debug("Searching listings by ownerId: {} with pagination: page={}, size={}",
                ownerId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Listing> listingPage = listingRepository.findByOwnerId(ownerId, pageable);
        Page<ListingResponse> responsePage = listingPage.map(mapper::toResponse);

        return PageResponse.of(responsePage);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> searchByPropertyId(UUID propertyId) {
        log.debug("Searching listings by propertyId: {} (non-paginated)", propertyId);

        return listingRepository.findByPropertyId(propertyId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search listings by property ID with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ListingResponse> searchByPropertyIdPaginated(UUID propertyId, Pageable pageable) {
        log.debug("Searching listings by propertyId: {} with pagination: page={}, size={}",
                propertyId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Listing> listingPage = listingRepository.findByPropertyId(propertyId, pageable);
        Page<ListingResponse> responsePage = listingPage.map(mapper::toResponse);

        return PageResponse.of(responsePage);
    }
}