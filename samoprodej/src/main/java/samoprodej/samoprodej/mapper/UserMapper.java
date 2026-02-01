package samoprodej.samoprodej.mapper;

import org.springframework.stereotype.Component;
import samoprodej.samoprodej.dto.UserDTO;
import samoprodej.samoprodej.entity.User;

@Component
public class UserMapper {

    public UserDTO.Response toDTO(User user) {
        UserDTO.Response dto = new UserDTO.Response();

        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());

        return dto;
    }

    public void updateUserFromDto(UserDTO.Request dto, User user) {
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getPreferredLang() != null) user.setPreferredLang(dto.getPreferredLang());
    }

    public User toEntity(UserDTO.CreateRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setPreferredLang(request.getPreferredLang());
        return user;
    }
}