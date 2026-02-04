package samoprodej.samoprodej.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import samoprodej.samoprodej.dto.UserDTO;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.UserStatus;
import samoprodej.samoprodej.mapper.UserMapper;
import samoprodej.samoprodej.repository.UserRepository;
import samoprodej.samoprodej.enums.Role;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO.Response getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return userMapper.toDTO(user);
    }

    public UserDTO.Response updateUser(UUID id, UserDTO.Request request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        userMapper.updateUserFromDto(request, user);

        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    public UserDTO.Response createUser(UserDTO.CreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityNotFoundException("Email already exists");
        }

        User user = userMapper.toEntity(request);

        if (request.getRole() == Role.OWNER) {
            user.setRole(Role.OWNER);
        } else {
            user.setRole(Role.TENANT);
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);

        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }
}