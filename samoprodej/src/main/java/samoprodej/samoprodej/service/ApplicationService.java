package samoprodej.samoprodej.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import samoprodej.samoprodej.dto.application.ApplicationResponse;
import samoprodej.samoprodej.dto.application.CreateApplicationRequest;
import samoprodej.samoprodej.entity.Application;
import samoprodej.samoprodej.entity.ChatThread;
import samoprodej.samoprodej.entity.Listing;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.ApplicationStatus;
import samoprodej.samoprodej.enums.ChatThreadStatus;
import samoprodej.samoprodej.enums.ErrorCode;
import samoprodej.samoprodej.enums.ListingStatus;
import samoprodej.samoprodej.exception.BusinessException;
import samoprodej.samoprodej.exception.NotFoundException;
import samoprodej.samoprodej.repository.ApplicationRepository;
import samoprodej.samoprodej.repository.ChatThreadRepository;
import samoprodej.samoprodej.repository.ListingRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApplicationResponse applyToListing(UUID listingId, UUID tenantId, CreateApplicationRequest request) {
        log.info("Tenant {} is applying to listing {}", tenantId, listingId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.CONFLICT, "LISTING_NOT_PUBLISHED");
        }

        if (listing.getOwner().getId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "CANNOT_APPLY_OWN_LISTING");
        }

        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        Optional<Application> existingApp = applicationRepository.findByListingIdAndTenantId(listingId, tenantId);
        if (existingApp.isPresent()) {
            log.info("Application already exists, returning existing one");
            Application app = existingApp.get();
            ChatThread existingThread = chatThreadRepository.findByListingIdAndTenantId(listingId, tenantId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Chat thread missing for existing application"));
            return mapToResponse(app, existingThread.getId());
        }

        Application application = new Application();
        application.setListing(listing);
        application.setTenant(tenant);
        application.setOwner(listing.getOwner());
        application.setStatus(ApplicationStatus.NEW);
        application.setMessage(request != null ? request.message() : null);

        application = applicationRepository.save(application);

        ChatThread chatThread = new ChatThread();
        chatThread.setListing(listing);
        chatThread.setTenant(tenant);
        chatThread.setOwner(listing.getOwner());
        chatThread.setStatus(ChatThreadStatus.OPEN);

        chatThread = chatThreadRepository.save(chatThread);

        log.info("Successfully created application {} and chat thread {}", application.getId(), chatThread.getId());

        return mapToResponse(application, chatThread.getId());
    }

    private ApplicationResponse mapToResponse(Application app, UUID chatThreadId) {
        return new ApplicationResponse(
                app.getId(),
                app.getListing().getId(),
                app.getTenant().getId(),
                app.getOwner().getId(),
                app.getStatus(),
                app.getMessage(),
                app.getCreatedAt(),
                app.getUpdatedAt(),
                chatThreadId
        );
    }
}