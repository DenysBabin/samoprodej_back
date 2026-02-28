package samoprodej.samoprodej.dto.application;

import samoprodej.samoprodej.enums.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID listingId,
        UUID tenantId,
        UUID ownerId,
        ApplicationStatus status,
        String message,
        Instant createdAt,
        Instant updatedAt,
        UUID chatThreadId
) {
}