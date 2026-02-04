package samoprodej.samoprodej.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.Role;
import samoprodej.samoprodej.enums.UserStatus;
import samoprodej.samoprodej.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner dbInitializer(UserRepository userRepository) {
        return args -> {
            System.out.println("Initializing mock data...");

            // === 1. Create ADMIN ===
            if (!userRepository.existsByEmail("admin@samoprodej.com")) {
                User admin = new User();
                admin.setEmail("admin@samoprodej.com");
                admin.setFirstName("Super");
                admin.setLastName("Admin");
                admin.setPhone("+420111111111");
                admin.setPasswordHash("admin123");
                admin.setStatus(UserStatus.ACTIVE);
                admin.setAuthProvider(AuthProvider.LOCAL);
                admin.setPreferredLang(Language.EN);

                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
                System.out.println(">>> SUCCESS: Created user 'admin@samoprodej.com' (Role: ADMIN)");
            }

            // === 2. Create OWNER ===
            if (!userRepository.existsByEmail("owner@samoprodej.com")) {
                User owner = new User();
                owner.setEmail("owner@samoprodej.com");
                owner.setFirstName("Petr");
                owner.setLastName("Vlasnik");
                owner.setPhone("+420222222222");
                owner.setPasswordHash("owner123");
                owner.setStatus(UserStatus.ACTIVE);
                owner.setAuthProvider(AuthProvider.LOCAL);
                owner.setPreferredLang(Language.CS);

                owner.setRole(Role.OWNER);

                userRepository.save(owner);
                System.out.println(">>> SUCCESS: Created user 'owner@samoprodej.com' (Role: OWNER)");
            }

            // === 3. Create TENANT ===
            if (!userRepository.existsByEmail("tenant@samoprodej.com")) {
                User tenant = new User();
                tenant.setEmail("tenant@samoprodej.com");
                tenant.setFirstName("Jan");
                tenant.setLastName("Najemnik");
                tenant.setPhone("+420333333333");
                tenant.setPasswordHash("tenant123");
                tenant.setStatus(UserStatus.ACTIVE);
                tenant.setAuthProvider(AuthProvider.LOCAL);
                tenant.setPreferredLang(Language.UA);

                tenant.setRole(Role.TENANT);

                userRepository.save(tenant);
                System.out.println(">>> SUCCESS: Created user 'tenant@samoprodej.com' (Role: TENANT)");
            }
        };
    }
}