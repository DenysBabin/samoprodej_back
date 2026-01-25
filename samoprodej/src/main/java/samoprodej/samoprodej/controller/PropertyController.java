package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.entity.Property;
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
    public List<Property> getAll() {
        return service.getAllProperties();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getPropertyById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public List<Property> search(@RequestParam String city) {
        return service.searchByCity(city);
    }

    @PostMapping
    public ResponseEntity<Property> create(@Valid @RequestBody Property property) {
        Property created = service.createProperty(property);
        return ResponseEntity.ok(created);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Property> update(@PathVariable UUID id,
                                           @Valid @RequestBody Property property) {
        try {
            return ResponseEntity.ok(service.updateProperty(id, property));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
}