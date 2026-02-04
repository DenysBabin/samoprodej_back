package samoprodej.samoprodej.mapper;

import org.springframework.stereotype.Component;
import samoprodej.samoprodej.dto.CreateUserRequest;
import samoprodej.samoprodej.dto.UpdateUserRequest;
import samoprodej.samoprodej.dto.UserResponse;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.UserStatus;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public UserResponse toDto(User entity) {
        if (entity == null) return null;

        return new UserResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getRole(),
                entity.getStatus(),
                entity.getAuthProvider(),
                entity.getPreferredLang(),
                entity.getAvatarUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt()
        );
    }

    public User toEntity(CreateUserRequest dto) {
        if (dto == null) return null;

        User user = new User();
        user.setEmail(dto.email());
        user.setPhone(dto.phone());
        user.setPasswordHash(dto.password()); // Will be hashed in service
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setRole(dto.role());
        user.setAuthProvider(dto.authProvider());
        user.setPreferredLang(dto.preferredLang() != null ? dto.preferredLang() : Language.CS);
        user.setStatus(UserStatus.ACTIVE);

        return user;
    }

    public void updateEntityFromDto(UpdateUserRequest dto, User entity) {
        if (dto == null || entity == null) return;

        if (dto.email() != null && !dto.email().isEmpty()) {
            entity.setEmail(dto.email());
        }
        if (dto.phone() != null) {
            entity.setPhone(dto.phone());
        }
        if (dto.firstName() != null) {
            entity.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            entity.setLastName(dto.lastName());
        }
        if (dto.preferredLang() != null) {
            entity.setPreferredLang(dto.preferredLang());
        }
        if (dto.avatarUrl() != null) {
            entity.setAvatarUrl(dto.avatarUrl());
        }
    }
}
