package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.dto.user.ChangePasswordRequest;
import samoprodej.samoprodej.dto.user.CreateUserRequest;
import samoprodej.samoprodej.dto.user.UpdateUserRequest;
import samoprodej.samoprodej.dto.user.UserResponse;
import samoprodej.samoprodej.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patch(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<UserResponse> block(@PathVariable UUID id) {
        return ResponseEntity.ok(service.block(id));
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search/email")
    public ResponseEntity<UserResponse> searchByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.searchByEmail(email));
    }

    @GetMapping("/search/phone")
    public ResponseEntity<UserResponse> searchByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(service.searchByPhone(phone));
    }
}
