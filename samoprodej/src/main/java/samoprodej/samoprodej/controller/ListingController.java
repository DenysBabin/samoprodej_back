package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.dto.common.PageResponse;
import samoprodej.samoprodej.dto.listing.CreateListingRequest;
import samoprodej.samoprodej.dto.listing.ListingResponse;
import samoprodej.samoprodej.dto.listing.PatchListingRequest;
import samoprodej.samoprodej.dto.listing.UpdateListingRequest;
import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.service.ListingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // Default pagination values
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    @PostMapping
    public ResponseEntity<ListingResponse> create(
            @Valid @RequestBody CreateListingRequest request,
            @RequestHeader("X-User-Id") UUID ownerId   // позже будет из JWT
    ) {
        try {
            ListingResponse response = listingService.create(request, ownerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all listings with pagination support
     *
     * @param page Page number (0-based), default: 0
     * @param size Page size, default: 10, max: 100
     * @param sort Sort field and direction (e.g., "createdAt,desc")
     * @return Paginated listing response
     */
    @GetMapping
    public ResponseEntity<PageResponse<ListingResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        try {
            // Validate and adjust page size
            if (size > MAX_SIZE) {
                size = MAX_SIZE;
            }
            if (size < 1) {
                size = DEFAULT_SIZE;
            }
            if (page < 0) {
                page = DEFAULT_PAGE;
            }

            Pageable pageable = createPageable(page, size, sort);
            PageResponse<ListingResponse> response = listingService.getAllPaginated(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getById(@PathVariable UUID id) {
        try {
            ListingResponse response = listingService.getById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListingResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        try {
            ListingResponse response = listingService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ListingResponse> patch(
            @PathVariable UUID id,
            @Valid @RequestBody PatchListingRequest request
    ) {
        try {
            ListingResponse response = listingService.patch(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            listingService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable UUID id) {
        try {
            listingService.publish(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ListingResponse> unpublish(@PathVariable UUID id) {
        try {
            ListingResponse response = listingService.unpublish(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ListingResponse> archive(@PathVariable UUID id) {
        try {
            ListingResponse response = listingService.archive(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Search listings by status with pagination support
     */
    @GetMapping("/search/status")
    public ResponseEntity<PageResponse<ListingResponse>> searchByStatus(
            @RequestParam ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        try {
            if (size > MAX_SIZE) {
                size = MAX_SIZE;
            }
            if (size < 1) {
                size = DEFAULT_SIZE;
            }
            if (page < 0) {
                page = DEFAULT_PAGE;
            }

            Pageable pageable = createPageable(page, size, sort);
            PageResponse<ListingResponse> response = listingService.searchByStatusPaginated(status, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Search listings by owner ID with pagination support
     */
    @GetMapping("/search/owner")
    public ResponseEntity<PageResponse<ListingResponse>> searchByOwnerId(
            @RequestParam UUID ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        try {
            if (size > MAX_SIZE) {
                size = MAX_SIZE;
            }
            if (size < 1) {
                size = DEFAULT_SIZE;
            }
            if (page < 0) {
                page = DEFAULT_PAGE;
            }

            Pageable pageable = createPageable(page, size, sort);
            PageResponse<ListingResponse> response = listingService.searchByOwnerIdPaginated(ownerId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Search listings by property ID with pagination support
     */
    @GetMapping("/search/property")
    public ResponseEntity<PageResponse<ListingResponse>> searchByPropertyId(
            @RequestParam UUID propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        try {
            if (size > MAX_SIZE) {
                size = MAX_SIZE;
            }
            if (size < 1) {
                size = DEFAULT_SIZE;
            }
            if (page < 0) {
                page = DEFAULT_PAGE;
            }

            Pageable pageable = createPageable(page, size, sort);
            PageResponse<ListingResponse> response = listingService.searchByPropertyIdPaginated(propertyId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper method to create Pageable object with optional sorting
     *
     * @param page Page number
     * @param size Page size
     * @param sort Sort parameter in format "field,direction" (e.g., "createdAt,desc")
     * @return Pageable object
     */
    private Pageable createPageable(int page, int size, String sort) {
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                String field = sortParams[0];
                String direction = sortParams[1];
                Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                return PageRequest.of(page, size, Sort.by(sortDirection, field));
            } else if (sortParams.length == 1) {
                // Default to ascending if direction not specified
                return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortParams[0]));
            }
        }
        return PageRequest.of(page, size);
    }
}