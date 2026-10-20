package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.model.Farmer;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.repository.AccountRepository;
import ke.co.machakosfa.mims.repository.FarmerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
    private static final String FARMER_ROLE = "FARMER";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int RESET_TOKEN_MINUTES = 30;
    private static final int MAX_PROFILE_IMAGE_BYTES = 1_048_576;
    private static final long MAX_PROFILE_IMAGE_PIXELS = 12_000_000;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9._-]{3,32}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String mailHost;
    private final String publicBaseUrl;

    public AccountService(
            AccountRepository accountRepository,
            FarmerRepository farmerRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${mims.mail.from:}") String mailFrom,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${mims.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.accountRepository = accountRepository;
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailHost = mailHost;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Transactional
    public Farmer registerFarmer(String username, String email, String password, String phone, Market market) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        validatePassword(password);
        if (market == null) {
            throw new IllegalArgumentException("Please select a market.");
        }
        if (accountRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new IllegalArgumentException("That username is already in use.");
        }
        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }

        Account account = new Account();
        account.setUsername(normalizedUsername);
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(FARMER_ROLE);
        accountRepository.save(account);

        Farmer farmer = new Farmer(normalizedUsername, phone, market);
        farmer.setAccount(account);
        return farmerRepository.save(farmer);
    }

    @Transactional
    public Farmer updateFarmerProfile(Farmer farmer, String username, String email, String phone, Market market) {
        return updateFarmerProfile(farmer, username, email, phone, market, null);
    }

    @Transactional
    public Farmer updateFarmerProfile(Farmer farmer, String username, String email, String phone, Market market,
                                      byte[] profileImage) {
        if (farmer == null || farmer.getAccount() == null
                || !FARMER_ROLE.equals(farmer.getAccount().getRole())) {
            throw new IllegalArgumentException("Farmer account not found.");
        }
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        ValidatedProfileImage validatedImage = profileImage == null ? null : validateProfileImage(profileImage);
        if (market == null) {
            throw new IllegalArgumentException("Please select a market.");
        }

        Account account = farmer.getAccount();
        if (accountRepository.existsByUsernameIgnoreCaseAndIdNot(normalizedUsername, account.getId())) {
            throw new IllegalArgumentException("That username is already in use.");
        }
        if (accountRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, account.getId())) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }

        account.setUsername(normalizedUsername);
        account.setEmail(normalizedEmail);
        farmer.setName(normalizedUsername);
        farmer.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        farmer.setMarket(market);
        if (validatedImage != null) {
            farmer.setProfileImage(validatedImage.data());
            farmer.setProfileImageContentType(validatedImage.contentType());
        }
        accountRepository.save(account);
        return farmerRepository.save(farmer);
    }

    private ValidatedProfileImage validateProfileImage(byte[] imageData) {
        if (imageData.length == 0 || imageData.length > MAX_PROFILE_IMAGE_BYTES) {
            throw new IllegalArgumentException("Choose an image smaller than 1 MB.");
        }

        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData))) {
            if (imageInput == null) {
                throw new IllegalArgumentException("Choose a valid JPEG, PNG, or GIF image.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Choose a valid JPEG, PNG, or GIF image.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                String contentType = switch (format) {
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    default -> throw new IllegalArgumentException("Choose a JPEG, PNG, or GIF image.");
                };
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels <= 0 || pixels > MAX_PROFILE_IMAGE_PIXELS || reader.read(0) == null) {
                    throw new IllegalArgumentException("The image is invalid or too large.");
                }
                return new ValidatedProfileImage(imageData, contentType);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("The image could not be read. Choose a valid JPEG, PNG, or GIF.", e);
        }
    }

    private record ValidatedProfileImage(byte[] data, String contentType) {}

    @Transactional(readOnly = true)
    public Optional<Account> authenticate(String username, String password, String requiredRole) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return accountRepository.findByUsernameIgnoreCase(username.trim())
                .filter(account -> requiredRole.equals(account.getRole()))
                .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()));
    }

    @Transactional
    public void bootstrapAdministrator(String username, String email, String password) {
        boolean anyValueConfigured = hasText(username) || hasText(email) || hasText(password);
        if (!anyValueConfigured) {
            LOGGER.warn("No administrator account configured. Set MIMS_ADMIN_USERNAME, MIMS_ADMIN_EMAIL, and "
                    + "MIMS_ADMIN_PASSWORD before starting the application.");
            return;
        }
        if (!hasText(username) || !hasText(email) || !hasText(password)) {
            throw new IllegalStateException(
                    "Configure MIMS_ADMIN_USERNAME, MIMS_ADMIN_EMAIL, and MIMS_ADMIN_PASSWORD together.");
        }

        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        validatePassword(password);
        if (password.length() < 12) {
            throw new IllegalStateException("The initial administrator password must be at least 12 characters.");
        }
        if (accountRepository.existsByUsernameIgnoreCase(normalizedUsername)
                || accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }

        Account account = new Account();
        account.setUsername(normalizedUsername);
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(ADMIN_ROLE);
        accountRepository.save(account);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        if (!hasText(mailHost) || !hasText(mailFrom)) {
            throw new IllegalStateException(
                    "Password recovery email is not configured. Set SMTP_HOST and MIMS_MAIL_FROM.");
        }

        Optional<Account> accountResult = accountRepository.findByEmailIgnoreCase(email.trim());
        if (accountResult.isEmpty()) {
            return;
        }

        Account account = accountResult.get();
        byte[] randomToken = new byte[32];
        SECURE_RANDOM.nextBytes(randomToken);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken);
        account.setResetTokenHash(sha256(token));
        account.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
        accountRepository.save(account);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(account.getEmail());
        message.setSubject("Reset your MIMS password");
        message.setText("Use this link within " + RESET_TOKEN_MINUTES + " minutes to reset your password:\n\n"
                + publicBaseUrl.replaceAll("/+$", "") + "/reset-password?token=" + token
                + "\n\nIf you did not request this, you can ignore this email.");
        mailSender.send(message);
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        validatePassword(newPassword);
        if (!hasText(token)) {
            return false;
        }
        Optional<Account> accountResult = accountRepository.findByResetTokenHashAndResetTokenExpiresAtAfter(
                sha256(token), LocalDateTime.now());
        if (accountResult.isEmpty()) {
            return false;
        }

        Account account = accountResult.get();
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setResetTokenHash(null);
        account.setResetTokenExpiresAt(null);
        accountRepository.save(account);
        return true;
    }

    private String normalizeUsername(String username) {
        if (!hasText(username) || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException("Username must be 3–32 characters: letters, numbers, dots, _ or -.");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        if (!hasText(email) || !EMAIL_PATTERN.matcher(email.trim()).matches() || email.trim().length() > 254) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be at least 8 characters and no more than 72 UTF-8 bytes.");
        }
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
