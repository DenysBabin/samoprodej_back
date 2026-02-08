package samoprodej.samoprodej.config.initializers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.Role;
import samoprodej.samoprodej.enums.UserStatus;
import samoprodej.samoprodej.repository.UserRepository;
import samoprodej.samoprodej.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class UserInitializer {
    private final UserRepository userRepository;
    private final UserService userService;
    private final Random random = new Random();

    @Value("${app.user.count:10}")
    private int userCount;

    public UserInitializer(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public void initialize() {
        if (userRepository.count() == 0) {
            System.out.println("Initializing mock users...");
            createAdmin();
            createTenantUsers();
            createOwnerUsers();
        }
    }

    private void createTenantUsers() {
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            User user = new User();
            int randomSuffix = random.nextInt(100000000);
            String phone = String.format("+420%08d", randomSuffix);

            user.setFirstName("NameTenant#" + i);
            user.setLastName("LastNameTenant#" + i);
            user.setEmail("tenant#" + i + "@mock.com");
            user.setPhone(phone);
            user.setPasswordHash(userService.hashPassword("mock_tenant_password#" + i));

            user.setRole(Role.TENANT);
            user.setPreferredLang(Language.CS);
            user.setStatus(UserStatus.ACTIVE);
            user.setAuthProvider(AuthProvider.LOCAL);
            users.add(user);
        }

        userRepository.saveAll(users);
        System.out.println("Created " + users.size() + " users.");

    }
    public void createOwnerUsers(){
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            User user = new User();

            int randomSuffix = random.nextInt(100000000);
            String phone = String.format("+420%08d", randomSuffix);

            user.setFirstName("NameOwner#" + i);
            user.setLastName("LastNameOwner#" + i);
            user.setEmail("owner#" + i + "@mock.com");

            user.setPhone(phone);
            user.setPasswordHash(userService.hashPassword("mock_owner_password#" + i));
            user.setRole(Role.OWNER);
            user.setPreferredLang(Language.CS);
            user.setStatus(UserStatus.ACTIVE);
            user.setAuthProvider(AuthProvider.LOCAL);

            users.add(user);
        }

        userRepository.saveAll(users);
        System.out.println("Created " + users.size() + " users.");
    }
    private void createAdmin(){
        String phoneNumber = "+42012312312";
        String firstName = "admin";
        String lastName = "AdminLastName";
        String email = "admin@mock.com";
        String password = "adminpassword";
        Role role = Role.ADMIN;
        Language language = Language.EN;
        UserStatus userStatus = UserStatus.ACTIVE;
        User admin = new User();
        admin.setPhone(phoneNumber);
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setPasswordHash(userService.hashPassword("adminpassword"));
        admin.setRole(role);
        admin.setPreferredLang(language);
        admin.setStatus(userStatus);
        admin.setAuthProvider(AuthProvider.LOCAL);

        userRepository.save(admin);
        System.out.println("Created admin with name \'admin\'");
    }
}
