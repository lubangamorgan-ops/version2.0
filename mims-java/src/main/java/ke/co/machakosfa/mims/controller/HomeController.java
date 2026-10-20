package ke.co.machakosfa.mims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.model.Farmer;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.service.AccountService;
import ke.co.machakosfa.mims.service.FarmerService;
import ke.co.machakosfa.mims.service.MarketService;
import ke.co.machakosfa.mims.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    private final MarketService marketService;
    private final FarmerService farmerService;
    private final PriceService priceService;
    private final AccountService accountService;

    public HomeController(
            MarketService marketService,
            FarmerService farmerService,
            PriceService priceService,
            AccountService accountService) {
        this.marketService = marketService;
        this.farmerService = farmerService;
        this.priceService = priceService;
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String landing(Model model) {
        List<PriceRecord> latest = priceService.findAll();
        model.addAttribute("latestPrices", latest.size() > 4 ? latest.subList(0, 4) : latest);
        model.addAttribute("markets", marketService.findAll());
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(defaultValue = "farmer") String role,
            @RequestParam(defaultValue = "false") boolean reset,
            Model model) {
        String selectedRole = "admin".equalsIgnoreCase(role) ? "admin" : "farmer";
        model.addAttribute("role", selectedRole);
        model.addAttribute("markets", marketService.findAll());
        if (reset) {
            model.addAttribute("success", "Your password was reset. Sign in with your new password.");
        }
        return "login";
    }

    @PostMapping("/login/admin")
    public String loginAdmin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            Model model) {
        Optional<Account> account = accountService.authenticate(username, password, "ADMIN");
        if (account.isEmpty()) {
            model.addAttribute("role", "admin");
            model.addAttribute("error", "Invalid username or password.");
            return "login";
        }

        HttpSession session = establishSession(request);
        session.setAttribute("role", "admin");
        session.setAttribute("name", account.get().getUsername());
        session.setAttribute("staffRole", "Association Administrator");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/login/farmer")
    public String loginFarmer(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            Model model) {
        Optional<Account> account = accountService.authenticate(username, password, "FARMER");
        if (account.isEmpty()) {
            model.addAttribute("role", "farmer");
            model.addAttribute("markets", marketService.findAll());
            model.addAttribute("error", "Invalid username or password.");
            return "login";
        }

        Farmer farmer = farmerService.findByAccountId(account.get().getId());
        HttpSession session = establishSession(request);
        session.setAttribute("role", "farmer");
        session.setAttribute("name", farmer.getName());
        session.setAttribute("farmerId", farmer.getId());
        session.setAttribute("marketId", farmer.getMarket().getId());
        return "redirect:/farmer/dashboard";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("markets", marketService.findAll());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone,
            @RequestParam Long marketId,
            HttpServletRequest request,
            Model model) {
        try {
            Market market = marketService.findById(marketId);
            Farmer farmer = accountService.registerFarmer(username, email, password, phone, market);
            HttpSession session = establishSession(request);
            session.setAttribute("role", "farmer");
            session.setAttribute("name", farmer.getName());
            session.setAttribute("farmerId", farmer.getId());
            session.setAttribute("marketId", market.getId());
            return "redirect:/farmer/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("markets", marketService.findAll());
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam String email, Model model) {
        try {
            accountService.requestPasswordReset(email);
            model.addAttribute("success",
                    "If an account uses that email, password reset instructions have been sent.");
        } catch (MailException | IllegalStateException e) {
            LOGGER.warn("Password reset email could not be sent.", e);
            model.addAttribute("error", e.getMessage());
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token == null ? "" : token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        model.addAttribute("token", token);
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "The passwords do not match.");
            return "reset-password";
        }
        try {
            if (accountService.resetPassword(token, password)) {
                return "redirect:/login?reset=true";
            }
            model.addAttribute("error", "This password reset link is invalid or has expired.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reset-password";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private HttpSession establishSession(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        return request.getSession(true);
    }
}
