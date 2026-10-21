package ke.co.machakosfa.mims.repository;

import ke.co.machakosfa.mims.model.RememberMeToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, Long> {
    Optional<RememberMeToken> findByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime now);
    void deleteByTokenHash(String tokenHash);
    void deleteByAccountId(Long accountId);
}
