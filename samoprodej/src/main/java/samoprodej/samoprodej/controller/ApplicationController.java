package samoprodej.samoprodej.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.dto.application.ApplicationResponse;
import samoprodej.samoprodej.dto.application.CreateApplicationRequest;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.ErrorCode;
import samoprodej.samoprodej.enums.Role;
import samoprodej.samoprodej.exception.BusinessException;
import samoprodej.samoprodej.exception.NotFoundException;
import samoprodej.samoprodej.repository.UserRepository;
import samoprodej.samoprodej.service.ApplicationService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    @PostMapping("/api/listings/{listingId}/apply")
    public ResponseEntity<ApplicationResponse> apply(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) CreateApplicationRequest request) {

        User tenant = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (tenant.getRole() != Role.TENANT) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Only tenants can apply to listings");
        }

        ApplicationResponse response = applicationService.applyToListing(listingId, tenant.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}