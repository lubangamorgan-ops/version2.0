package ke.co.machakosfa.mims.controller;

import ke.co.machakosfa.mims.model.Farmer;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.service.MarketService;
import ke.co.machakosfa.mims.service.FarmerService;
import ke.co.machakosfa.mims.service.PriceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    private final MarketService marketService;
    private final FarmerService farmerService;
    private final PriceService priceService;

    public HomeController(MarketService marketService, FarmerService farmerService, PriceService priceService) {
        this.marketService = marketService;
        this.farmerService = farmerService;
        this.priceService = priceService;
    }

    @GetMapping("/")
    public String landing(Model model) {
        List<PriceRecord> latest = priceService.findAll();
        model.addAttribute("latestPrices", latest.size() > 4 ? latest.subList(0, 4) : latest);
        model.addAttribute("markets", marketService.findAll());
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(defaultValue = "farmer") String role, Model model) {
        model.addAttribute("role", role);
        model.addAttribute("markets", marketService.findAll());
        return "login";
    }

    @PostMapping("/login/admin")
    public String loginAdmin(@RequestParam String name, @RequestParam String staffRole, HttpSession session) {
        session.setAttribute("role", "admin");
        session.setAttribute("name", name);
        session.setAttribute("staffRole", staffRole);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/login/farmer")
    public String loginFarmer(@RequestParam String name, @RequestParam(required = false) String phone,
                               @RequestParam Long marketId, HttpSession session) {
        Market market = marketService.findById(marketId);
        Farmer farmer = farmerService.findOrRegister(name, phone, market);

        session.setAttribute("role", "farmer");
        session.setAttribute("name", farmer.getName());
        session.setAttribute("farmerId", farmer.getId());
        session.setAttribute("marketId", market.getId());
        return "redirect:/farmer/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
