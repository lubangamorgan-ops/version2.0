package ke.co.machakosfa.mims.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.model.Farmer;
import ke.co.machakosfa.mims.repository.FarmerRepository;
import ke.co.machakosfa.mims.service.RememberMeService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class RememberMeInterceptor implements HandlerInterceptor {

    private final RememberMeService rememberMeService;
    private final FarmerRepository farmerRepository;

    public RememberMeInterceptor(
            RememberMeService rememberMeService, FarmerRepository farmerRepository) {
        this.rememberMeService = rememberMeService;
        this.farmerRepository = farmerRepository;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null && existingSession.getAttribute("role") != null) {
            return true;
        }

        String token = RememberMeCookie.read(request);
        if (token == null || token.isBlank()) {
            return true;
        }

        Optional<RememberMeService.RememberedLogin> remembered =
                rememberMeService.rotateToken(token);
        if (remembered.isEmpty()) {
            RememberMeCookie.clear(response, request);
            return true;
        }

        Account account = remembered.get().account();
        if (!restoreSession(request, account)) {
            rememberMeService.revoke(remembered.get().token());
            RememberMeCookie.clear(response, request);
            return true;
        }

        RememberMeCookie.set(response, request, remembered.get().token());
        return true;
    }

    private boolean restoreSession(HttpServletRequest request, Account account) {
        if (account.getRole() == null) {
            return false;
        }

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
        HttpSession session = request.getSession(true);

        if ("ADMIN".equalsIgnoreCase(account.getRole())) {
            session.setAttribute("role", "admin");
            session.setAttribute("name", account.getUsername());
            session.setAttribute("staffRole", "Association Administrator");
            return true;
        }
        if ("FARMER".equalsIgnoreCase(account.getRole())) {
            Optional<Farmer> farmer = farmerRepository.findByAccount_Id(account.getId());
            if (farmer.isEmpty() || farmer.get().getMarket() == null) {
                session.invalidate();
                return false;
            }
            session.setAttribute("role", "farmer");
            session.setAttribute("name", farmer.get().getName());
            session.setAttribute("farmerId", farmer.get().getId());
            session.setAttribute("marketId", farmer.get().getMarket().getId());
            return true;
        }

        session.invalidate();
        return false;
    }
}
