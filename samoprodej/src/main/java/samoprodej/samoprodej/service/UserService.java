package samoprodej.samoprodej.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import samoprodej.samoprodej.dto.user.ChangePasswordRequest;
import samoprodej.samoprodej.dto.user.CreateUserRequest;
import samoprodej.samoprodej.dto.user.UpdateUserRequest;
import samoprodej.samoprodej.dto.user.UserResponse;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.UserStatus;
import samoprodej.samoprodej.mapper.UserMapper;
import samoprodej.samoprodej.repository.UserRepository;
import samoprodej.samoprodej.enums.ErrorCode;
import samoprodej.samoprodej.exception.BusinessException;
import samoprodej.samoprodej.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserResponse create(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        // Check uniqueness
        if (repository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "User with email " + request.email() + " already exists");
        }
        if (request.phone() != null && !request.phone().isEmpty()) {
            repository.findByPhone(request.phone()).ifPresent(user -> {
                throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "User with phone " + request.phone() + " already exists");
            });
        }

        User user = mapper.toEntity(request);
        // Hash password (simple hash for now, can be improved with BCrypt later)
        user.setPasswordHash(hashPassword(request.password()));
        user.setPasswordUpdatedAt(LocalDateTime.now());

        User saved = repository.save(user);
        log.info("User created with ID: {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, "User not found with id: " + id));
        return mapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        log.info("Updating user ID: {}", id);
        User existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Check email uniqueness if email is being changed
        if (request.email() != null && !request.email().equals(existing.getEmail())) {
            if (repository.existsByEmail(request.email())) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "User with email " + request.email() + " already exists");
            }
        }

        // Check phone uniqueness if phone is being changed
        if (request.phone() != null && !request.phone().equals(existing.getPhone())) {
            repository.findByPhone(request.phone()).ifPresent(user -> {
                if (!user.getId().equals(id)) {
                    throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "User with phone " + request.phone() + " already exists");
                }
            });
        }

        mapper.updateEntityFromDto(request, existing);
        User updated = repository.save(existing);
        log.info("User updated with ID: {}", updated.getId());

        return mapper.toDto(updated);
    }

    public void delete(UUID id) {
        log.warn("Deleting user ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        user.setDeletedAt(LocalDateTime.now());
        repository.save(user);
    }

    public UserResponse activate(UUID id) {
        log.info("Activating user ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        user.setStatus(UserStatus.ACTIVE);
        User updated = repository.save(user);
        return mapper.toDto(updated);
    }

    public UserResponse block(UUID id) {
        log.info("Blocking user ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        user.setStatus(UserStatus.BLOCKED);
        User updated = repository.save(user);
        return mapper.toDto(updated);
    }

    public void changePassword(UUID id, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Verify old password
        if (!user.getPasswordHash().equals(hashPassword(request.oldPassword()))) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Invalid old password");
        }

        // Set new password
        user.setPasswordHash(hashPassword(request.newPassword()));
        user.setPasswordUpdatedAt(LocalDateTime.now());
        repository.save(user);
        log.info("Password changed for user ID: {}", id);
    }

    @Transactional(readOnly = true)
    public UserResponse searchByEmail(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return mapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponse searchByPhone(String phone) {
        User user = repository.findByPhone(phone)
                .orElseThrow(() -> new NotFoundException("User not found with phone: " + phone));
        return mapper.toDto(user);
    }

    /**
     * Simple password hashing (should be replaced with BCrypt in production)
     */
    public String hashPassword(String password) {
        // TODO: Replace with proper BCrypt hashing
        // For now, simple hash (not secure, but functional)
        return String.valueOf(password.hashCode());
    }
}
