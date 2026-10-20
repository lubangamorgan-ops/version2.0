package ke.co.machakosfa.mims.repository;

import ke.co.machakosfa.mims.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsernameIgnoreCase(String username);
    Optional<Account> findByEmailIgnoreCase(String email);
    Optional<Account> findByResetTokenHashAndResetTokenExpiresAtAfter(
            String resetTokenHash, LocalDateTime now);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
}
