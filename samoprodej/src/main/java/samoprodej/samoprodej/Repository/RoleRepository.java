package samoprodej.samoprodej.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import samoprodej.samoprodej.Entity.Role;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // To find role by name, e.g., "USER"
    Optional<Role> findByCode(String code);
}