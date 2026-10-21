package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.model.RememberMeToken;
import ke.co.machakosfa.mims.repository.AccountRepository;
import ke.co.machakosfa.mims.repository.RememberMeTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RememberMeServiceTest {

    private final RememberMeTokenRepository tokenRepository = mock(RememberMeTokenRepository.class);
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final RememberMeService service =
            new RememberMeService(tokenRepository, accountRepository);

    @Test
    void storesOnlyHashedTokenAndRotatesOnUse() throws Exception {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(42L);

        String originalToken = service.createToken(account);

        ArgumentCaptor<RememberMeToken> savedToken = ArgumentCaptor.forClass(RememberMeToken.class);
        verify(tokenRepository).save(savedToken.capture());
        RememberMeToken stored = savedToken.getValue();
        assertNotEquals(originalToken, stored.getTokenHash());
        assertEquals(hash(originalToken), stored.getTokenHash());
        assertTrue(stored.getExpiresAt().isAfter(LocalDateTime.now().plusDays(29)));

        when(tokenRepository.findByTokenHashAndExpiresAtAfter(
                eq(hash(originalToken)), any(LocalDateTime.class))).thenReturn(Optional.of(stored));
        when(accountRepository.findById(42L)).thenReturn(Optional.of(account));

        Optional<RememberMeService.RememberedLogin> remembered =
                service.rotateToken(originalToken);

        assertTrue(remembered.isPresent());
        assertSame(account, remembered.get().account());
        assertNotEquals(originalToken, remembered.get().token());
        assertEquals(hash(remembered.get().token()), stored.getTokenHash());
    }

    @Test
    void doesNotRestoreExpiredOrUnknownTokens() {
        when(tokenRepository.findByTokenHashAndExpiresAtAfter(any(), any()))
                .thenReturn(Optional.empty());

        assertTrue(service.rotateToken("invalid-token").isEmpty());
        verifyNoInteractions(accountRepository);
    }

    private static String hash(String token) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
    }
}
