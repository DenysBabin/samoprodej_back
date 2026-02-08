package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public ResponseEntity<List<ListingResponse>> getAll() {
        try {
            List<ListingResponse> listings = listingService.getAll();
            return ResponseEntity.ok(listings);
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

    @GetMapping("/search/status")
    public ResponseEntity<List<ListingResponse>> searchByStatus(
            @RequestParam ListingStatus status
    ) {
        try {
            List<ListingResponse> listings = listingService.searchByStatus(status);
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/owner")
    public ResponseEntity<List<ListingResponse>> searchByOwnerId(
            @RequestParam UUID ownerId
    ) {
        try {
            List<ListingResponse> listings = listingService.searchByOwnerId(ownerId);
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/property")
    public ResponseEntity<List<ListingResponse>> searchByPropertyId(
            @RequestParam UUID propertyId
    ) {
        try {
            List<ListingResponse> listings = listingService.searchByPropertyId(propertyId);
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
