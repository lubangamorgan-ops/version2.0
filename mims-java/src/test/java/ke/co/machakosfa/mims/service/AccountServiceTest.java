package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.model.Farmer;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.repository.AccountRepository;
import ke.co.machakosfa.mims.repository.FarmerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private JavaMailSender mailSender;

    private PasswordEncoder passwordEncoder;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        accountService = new AccountService(
                accountRepository, farmerRepository, passwordEncoder, mailSender,
                "mims@example.test", "smtp.example.test", "https://mims.example.test");
    }

    @Test
    void registersFarmerWithNormalizedIdentityAndHashedPassword() {
        when(farmerRepository.save(any(Farmer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Market market = new Market("Machakos Market", "Machakos");

        Farmer farmer = accountService.registerFarmer(
                "MORGAN_1", "Morgan@example.test", "secure-pass-123", "0712345678", market);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account account = accountCaptor.getValue();
        assertEquals("morgan_1", account.getUsername());
        assertEquals("morgan@example.test", account.getEmail());
        assertEquals("FARMER", account.getRole());
        assertNotEquals("secure-pass-123", account.getPasswordHash());
        assertTrue(passwordEncoder.matches("secure-pass-123", account.getPasswordHash()));
        assertSame(account, farmer.getAccount());
        assertEquals("morgan_1", farmer.getName());
    }

    @Test
    void authenticatesOnlyForMatchingRoleAndPassword() {
        Account account = new Account();
        account.setUsername("morgan");
        account.setRole("FARMER");
        account.setPasswordHash(passwordEncoder.encode("secure-pass-123"));
        when(accountRepository.findByUsernameIgnoreCase("morgan")).thenReturn(Optional.of(account));

        assertTrue(accountService.authenticate("morgan", "secure-pass-123", "FARMER").isPresent());
        assertTrue(accountService.authenticate("morgan", "wrong-password", "FARMER").isEmpty());
        assertTrue(accountService.authenticate("morgan", "secure-pass-123", "ADMIN").isEmpty());
    }

    @Test
    void resetPasswordUpdatesHashAndClearsResetToken() {
        Account account = new Account();
        account.setPasswordHash(passwordEncoder.encode("old-password"));
        account.setResetTokenHash("stored-token-hash");
        when(accountRepository.findByResetTokenHashAndResetTokenExpiresAtAfter(anyString(), any()))
                .thenReturn(Optional.of(account));

        assertTrue(accountService.resetPassword("reset-token", "new-password-123"));

        assertTrue(passwordEncoder.matches("new-password-123", account.getPasswordHash()));
        assertNull(account.getResetTokenHash());
        assertNull(account.getResetTokenExpiresAt());
        verify(accountRepository).save(account);
    }

    @Test
    void doesNotSendResetMailWhenSmtpIsNotConfigured() {
        AccountService unconfiguredService = new AccountService(
                accountRepository, farmerRepository, passwordEncoder, mailSender, "", "", "http://localhost:8080");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> unconfiguredService.requestPasswordReset("morgan@example.test"));
        assertTrue(exception.getMessage().contains("not configured"));

        verifyNoInteractions(mailSender);
    }
}
