package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.model.RememberMeToken;
import ke.co.machakosfa.mims.repository.AccountRepository;
import ke.co.machakosfa.mims.repository.RememberMeTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class RememberMeService {

    public static final int TOKEN_LIFETIME_DAYS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RememberMeTokenRepository tokenRepository;
    private final AccountRepository accountRepository;

    public RememberMeService(
            RememberMeTokenRepository tokenRepository,
            AccountRepository accountRepository) {
        this.tokenRepository = tokenRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public String createToken(Account account) {
        String token = newToken();
        tokenRepository.save(new RememberMeToken(
                account.getId(), hash(token), LocalDateTime.now().plusDays(TOKEN_LIFETIME_DAYS)));
        return token;
    }

    @Transactional
    public Optional<RememberedLogin> rotateToken(String token) {
        Optional<RememberMeToken> storedToken = tokenRepository.findByTokenHashAndExpiresAtAfter(
                hash(token), LocalDateTime.now());
        if (storedToken.isEmpty()) {
            return Optional.empty();
        }

        RememberMeToken record = storedToken.get();
        Optional<Account> account = accountRepository.findById(record.getAccountId());
        if (account.isEmpty()) {
            tokenRepository.delete(record);
            return Optional.empty();
        }

        String replacement = newToken();
        record.setTokenHash(hash(replacement));
        record.setExpiresAt(LocalDateTime.now().plusDays(TOKEN_LIFETIME_DAYS));
        tokenRepository.save(record);
        return Optional.of(new RememberedLogin(account.get(), replacement));
    }

    @Transactional
    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            tokenRepository.deleteByTokenHash(hash(token));
        }
    }

    @Transactional
    public void revokeAllForAccount(Long accountId) {
        tokenRepository.deleteByAccountId(accountId);
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }

    public record RememberedLogin(Account account, String token) {}
}
