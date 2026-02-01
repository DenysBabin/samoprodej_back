package samoprodej.samoprodej.dto;

import lombok.Data;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.Role;
import java.util.UUID;

public class UserDTO {
    @Data
    public static class Request {
        private String firstName;
        private String lastName;
        private String phone;
        private String avatarUrl;
        private Language preferredLang;
    }

    @Data
    public static class Response {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String avatarUrl;
        private Role role;
    }

    @Data
    public static class CreateRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phone;
        private Language preferredLang;
        private Role role;
    }
}