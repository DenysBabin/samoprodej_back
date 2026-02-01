package samoprodej.samoprodej.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.dto.CreateListingRequest;
import samoprodej.samoprodej.dto.ListingResponse;
import samoprodej.samoprodej.dto.PatchListingRequest;
import samoprodej.samoprodej.service.ListingService;


import java.util.UUID;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // ---- CREATE ----
    @PostMapping
    public ListingResponse create(
            @Valid @RequestBody CreateListingRequest request,
            @RequestHeader("X-User-Id") UUID ownerId   // позже будет из JWT
    ) {
        return listingService.create(request, ownerId);
    }

    // ---- GET BY ID ----
    @GetMapping("/{id}")
    public ListingResponse getById(@PathVariable UUID id) {
        return listingService.getById(id);
    }

    // ---- PARTIAL UPDATE (PATCH) ----
    @PatchMapping("/{id}")
    public ListingResponse patch(
            @PathVariable UUID id,
            @RequestBody PatchListingRequest request
    ) {
        return listingService.patch(id, request);
    }

    // ---- PUBLISH LISTING ----
    @PostMapping("/{id}/publish")
    public void publish(@PathVariable UUID id) {
        listingService.publish(id);
    }
}
