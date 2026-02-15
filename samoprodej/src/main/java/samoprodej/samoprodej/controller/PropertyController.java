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
import samoprodej.samoprodej.dto.property_media.CreatePropertyMediaRequest;
import samoprodej.samoprodej.dto.property_media.PropertyMediaDTO;
import samoprodej.samoprodej.dto.property_media.PropertyMediaResponse;
import samoprodej.samoprodej.dto.property_media.ReorderMediaRequest;
import samoprodej.samoprodej.dto.property_media.UpdatePropertyMediaRequest;
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
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public List<PropertyResponse> search(@RequestParam String city) {
        return service.searchByCity(city);
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody CreatePropertyRequest request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdatePropertyRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PropertyResponse> patch(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdatePropertyRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
        try {
            return ResponseEntity.ok(service.getPropertyById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Deprecated
    @PostMapping("/legacy")
    public ResponseEntity<PropertyDTO> createLegacy(@Valid @RequestBody PropertyDTO dto) {
        try {
            return ResponseEntity.ok(service.createProperty(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Deprecated
    @PutMapping("/legacy/{id}")
    public ResponseEntity<PropertyDTO> updateLegacy(@PathVariable UUID id,
                                                     @Valid @RequestBody PropertyDTO dto) {
        try {
            return ResponseEntity.ok(service.updateProperty(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Deprecated
    @DeleteMapping("/legacy/{id}")
    public ResponseEntity<Void> deleteLegacy(@PathVariable UUID id) {
        try {
            service.deleteProperty(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Media endpoints - new implementation
    @PostMapping("/{id}/media/upload")
    public ResponseEntity<PropertyMediaResponse> uploadMedia(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") MediaType type
    ) {
        try {
            PropertyMediaResponse response = mediaService.uploadMedia(id, file, type);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/media")
    public ResponseEntity<PropertyMediaResponse> addMedia(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePropertyMediaRequest request
    ) {
        try {
            PropertyMediaResponse response = mediaService.addMediaFromUrl(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<List<PropertyMediaResponse>> getMedia(@PathVariable UUID id) {
        try {
            List<PropertyMediaResponse> media = mediaService.getMediaForProperty(id);
            return ResponseEntity.ok(media);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/media/reorder")
    public ResponseEntity<List<PropertyMediaResponse>> reorderMedia(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderMediaRequest request
    ) {
        try {
            List<PropertyMediaResponse> media = mediaService.reorderMedia(id, request);
            return ResponseEntity.ok(media);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/media/{mediaId}")
    public ResponseEntity<PropertyMediaResponse> updateMedia(
            @PathVariable UUID id,
            @PathVariable UUID mediaId,
            @Valid @RequestBody UpdatePropertyMediaRequest request
    ) {
        try {
            PropertyMediaResponse response = mediaService.updateMedia(mediaId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable UUID id,
            @PathVariable UUID mediaId
    ) {
        try {
            mediaService.deleteMedia(mediaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Legacy media endpoints for backward compatibility
    @Deprecated
    @PostMapping("/{id}/media/legacy")
    public ResponseEntity<PropertyMediaDTO> addMediaLegacy(
            @PathVariable UUID id,
            @Valid @RequestBody PropertyMediaDTO mediaDto
    ) {
        try {
            return ResponseEntity.ok(service.addMedia(id, mediaDto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Deprecated
    @GetMapping("/{id}/media/legacy")
    public ResponseEntity<List<PropertyMediaDTO>> getMediaLegacy(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getMediaForProperty(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Deprecated
    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMediaLegacy(@PathVariable UUID mediaId) {
        try {
            service.deleteMedia(mediaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}