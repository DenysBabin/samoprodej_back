package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import samoprodej.samoprodej.dto.property.CreatePropertyRequest;
import samoprodej.samoprodej.dto.property.PropertyDTO;
import samoprodej.samoprodej.dto.property.PropertyResponse;
import samoprodej.samoprodej.dto.property.UpdatePropertyRequest;
import samoprodej.samoprodej.dto.propertymedia.CreatePropertyMediaRequest;
import samoprodej.samoprodej.dto.propertymedia.PropertyMediaDTO;
import samoprodej.samoprodej.dto.propertymedia.PropertyMediaResponse;
import samoprodej.samoprodej.dto.propertymedia.ReorderMediaRequest;
import samoprodej.samoprodej.dto.propertymedia.UpdatePropertyMediaRequest;
import samoprodej.samoprodej.enums.MediaType;
import samoprodej.samoprodej.service.PropertyMediaService;
import samoprodej.samoprodej.service.PropertyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService service;
    private final PropertyMediaService mediaService;

    @GetMapping
    public List<PropertyResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/search")
    public List<PropertyResponse> search(@RequestParam String city) {
        return service.searchByCity(city);
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody CreatePropertyRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdatePropertyRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PropertyResponse> patch(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdatePropertyRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Legacy endpoints for backward compatibility
    @Deprecated
    @GetMapping("/legacy")
    public List<PropertyDTO> getAllLegacy() {
        return service.getAllProperties();
    }

    @Deprecated
    @GetMapping("/legacy/{id}")
    public ResponseEntity<PropertyDTO> getByIdLegacy(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getPropertyById(id));
    }

    @Deprecated
    @PostMapping("/legacy")
    public ResponseEntity<PropertyDTO> createLegacy(@Valid @RequestBody PropertyDTO dto) {
        return ResponseEntity.ok(service.createProperty(dto));
    }

    @Deprecated
    @PutMapping("/legacy/{id}")
    public ResponseEntity<PropertyDTO> updateLegacy(@PathVariable UUID id,
                                                     @Valid @RequestBody PropertyDTO dto) {
        return ResponseEntity.ok(service.updateProperty(id, dto));
    }

    @Deprecated
    @DeleteMapping("/legacy/{id}")
    public ResponseEntity<Void> deleteLegacy(@PathVariable UUID id) {
        service.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    // Media endpoints - new implementation
    @PostMapping("/{id}/media/upload")
    public ResponseEntity<PropertyMediaResponse> uploadMedia(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") MediaType type
    ) {
        PropertyMediaResponse response = mediaService.uploadMedia(id, file, type);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/media")
    public ResponseEntity<PropertyMediaResponse> addMedia(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePropertyMediaRequest request
    ) {
        PropertyMediaResponse response = mediaService.addMediaFromUrl(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<List<PropertyMediaResponse>> getMedia(@PathVariable UUID id) {
        List<PropertyMediaResponse> media = mediaService.getMediaForProperty(id);
        return ResponseEntity.ok(media);
    }

    @PutMapping("/{id}/media/reorder")
    public ResponseEntity<List<PropertyMediaResponse>> reorderMedia(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderMediaRequest request
    ) {
        List<PropertyMediaResponse> media = mediaService.reorderMedia(id, request);
        return ResponseEntity.ok(media);
    }

    @PatchMapping("/{id}/media/{mediaId}")
    public ResponseEntity<PropertyMediaResponse> updateMedia(
            @PathVariable UUID id,
            @PathVariable UUID mediaId,
            @Valid @RequestBody UpdatePropertyMediaRequest request
    ) {
        PropertyMediaResponse response = mediaService.updateMedia(mediaId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable UUID id,
            @PathVariable UUID mediaId
    ) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    // Legacy media endpoints for backward compatibility
    @Deprecated
    @PostMapping("/{id}/media/legacy")
    public ResponseEntity<PropertyMediaDTO> addMediaLegacy(
            @PathVariable UUID id,
            @Valid @RequestBody PropertyMediaDTO mediaDto
    ) {
        return ResponseEntity.ok(service.addMedia(id, mediaDto));
    }

    @Deprecated
    @GetMapping("/{id}/media/legacy")
    public ResponseEntity<List<PropertyMediaDTO>> getMediaLegacy(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getMediaForProperty(id));
    }

    @Deprecated
    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMediaLegacy(@PathVariable UUID mediaId) {
        service.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }
}