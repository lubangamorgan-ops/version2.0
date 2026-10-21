package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.model.Farmer;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.repository.AccountRepository;
import ke.co.machakosfa.mims.repository.FarmerRepository;
import ke.co.machakosfa.mims.repository.RememberMeTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final FarmerRepository farmerRepository = mock(FarmerRepository.class);
    private final RememberMeTokenRepository rememberMeTokenRepository =
            mock(RememberMeTokenRepository.class);
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AccountService accountService = new AccountService(
            accountRepository, farmerRepository, rememberMeTokenRepository, passwordEncoder, mailSender,
            "mims@example.test", "smtp.example.test", "https://mims.example.test");

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
    void updatesFarmerProfileWithNormalizedUsernameAndEmail() {
        when(farmerRepository.save(any(Farmer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Account account = new Account();
        account.setRole("FARMER");
        account.setUsername("old-name");
        account.setEmail("old@example.test");
        Farmer farmer = new Farmer("old-name", null, new Market("Old Market", "Machakos"));
        farmer.setAccount(account);
        Market market = new Market("New Market", "Machakos");

        Farmer updatedFarmer = accountService.updateFarmerProfile(
                farmer, "New_Name", "New@example.test", " +254712345678 ", market);

        assertEquals("new_name", account.getUsername());
        assertEquals("new@example.test", account.getEmail());
        assertEquals("new_name", updatedFarmer.getName());
        assertEquals("+254712345678", updatedFarmer.getPhone());
        assertSame(market, updatedFarmer.getMarket());
        verify(accountRepository).save(account);
        verify(farmerRepository).save(farmer);
    }

    @Test
    void savesValidatedProfilePhoto() throws IOException {
        when(farmerRepository.save(any(Farmer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Account account = new Account();
        account.setRole("FARMER");
        account.setUsername("farmer");
        account.setEmail("farmer@example.test");
        Farmer farmer = new Farmer("farmer", null, new Market("Market", "Machakos"));
        farmer.setAccount(account);
        ByteArrayOutputStream imageOutput = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", imageOutput);
        byte[] image = imageOutput.toByteArray();

        Farmer updatedFarmer = accountService.updateFarmerProfile(
                farmer, "farmer", "farmer@example.test", null, farmer.getMarket(), image);

        assertArrayEquals(image, updatedFarmer.getProfileImage());
        assertEquals("image/png", updatedFarmer.getProfileImageContentType());
        verify(farmerRepository).save(farmer);
    }

    @Test
    void rejectsInvalidProfilePhotoWithoutSavingProfileChanges() {
        Account account = new Account();
        account.setRole("FARMER");
        account.setUsername("farmer");
        account.setEmail("farmer@example.test");
        Farmer farmer = new Farmer("farmer", null, new Market("Market", "Machakos"));
        farmer.setAccount(account);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.updateFarmerProfile(
                        farmer, "new-name", "new@example.test", null, farmer.getMarket(),
                        new byte[]{1, 2, 3}));

        assertTrue(exception.getMessage().contains("valid JPEG, PNG, or GIF"));
        verify(accountRepository, never()).save(any(Account.class));
        verify(farmerRepository, never()).save(any(Farmer.class));
    }

    @Test
    void rejectsFarmerProfileUsernameAlreadyUsedByAnotherAccount() {
        Account account = new Account();
        account.setRole("FARMER");
        account.setUsername("old-name");
        account.setEmail("old@example.test");
        Farmer farmer = new Farmer("old-name", null, new Market("Old Market", "Machakos"));
        farmer.setAccount(account);
        when(accountRepository.existsByUsernameIgnoreCaseAndIdNot("taken", account.getId())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.updateFarmerProfile(
                        farmer, "taken", "new@example.test", null, new Market("Market", "Machakos")));

        assertEquals("That username is already in use.", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(farmerRepository, never()).save(any(Farmer.class));
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
                accountRepository, farmerRepository, rememberMeTokenRepository, passwordEncoder, mailSender, "", "", "http://localhost:8080");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> unconfiguredService.requestPasswordReset("morgan@example.test"));
        assertTrue(exception.getMessage().contains("not configured"));

        verifyNoInteractions(mailSender);
    }
}
