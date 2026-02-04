package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.dto.PropertyDTO;
import samoprodej.samoprodej.dto.PropertyMediaDTO; // NEW
import samoprodej.samoprodej.service.PropertyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService service;

    public PropertyController(PropertyService service) {
        this.service = service;
    }

    @GetMapping
    public List<PropertyDTO> getAll() {
        return service.getAllProperties();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getPropertyById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public List<PropertyDTO> search(@RequestParam String city) {
        return service.searchByCity(city);
    }

    @PostMapping
    public ResponseEntity<PropertyDTO> create(@Valid @RequestBody PropertyDTO dto) {
        return ResponseEntity.ok(service.createProperty(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyDTO> update(@PathVariable UUID id,
                                              @Valid @RequestBody PropertyDTO dto) {
        try {
            return ResponseEntity.ok(service.updateProperty(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteProperty(id);
        return ResponseEntity.noContent().build();
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
        service.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }
}