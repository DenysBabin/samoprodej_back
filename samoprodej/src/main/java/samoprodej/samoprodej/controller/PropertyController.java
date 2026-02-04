package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.dto.CreatePropertyRequest;
import samoprodej.samoprodej.dto.PropertyDTO;
import samoprodej.samoprodej.dto.PropertyMediaDTO;
import samoprodej.samoprodej.dto.PropertyResponse;
import samoprodej.samoprodej.dto.UpdatePropertyRequest;
import samoprodej.samoprodej.service.PropertyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService service;

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

    @PostMapping("/{id}/media")
    public ResponseEntity<PropertyMediaDTO> addMedia(@PathVariable UUID id,
                                                     @Valid @RequestBody PropertyMediaDTO mediaDto) {
        try {
            return ResponseEntity.ok(service.addMedia(id, mediaDto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<List<PropertyMediaDTO>> getMedia(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getMediaForProperty(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID mediaId) {
        try {
            service.deleteMedia(mediaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}