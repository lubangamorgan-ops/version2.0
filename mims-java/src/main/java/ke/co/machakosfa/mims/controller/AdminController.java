package ke.co.machakosfa.mims.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import ke.co.machakosfa.mims.exception.DuplicatePriceException;
import ke.co.machakosfa.mims.exception.InvalidPriceDateException;
import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.service.BroadcastService;
import ke.co.machakosfa.mims.service.CropService;
import ke.co.machakosfa.mims.service.FarmerService;
import ke.co.machakosfa.mims.service.MarketService;
import ke.co.machakosfa.mims.service.PriceService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CropService cropService;
    private final MarketService marketService;
    private final FarmerService farmerService;
    private final PriceService priceService;
    private final BroadcastService broadcastService;

    public AdminController(CropService cropService, MarketService marketService, FarmerService farmerService,
                            PriceService priceService, BroadcastService broadcastService) {
        this.cropService = cropService;
        this.marketService = marketService;
        this.farmerService = farmerService;
        this.priceService = priceService;
        this.broadcastService = broadcastService;
    }

    private String requireAdmin(HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login?role=admin";
        }
        return null;
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("staffRole", session.getAttribute("staffRole"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);

        List<PriceRecord> all = priceService.findAll();
        model.addAttribute("cropCount", cropService.findAll().size());
        model.addAttribute("marketCount", marketService.findAll().size());
        model.addAttribute("farmerCount", farmerService.findAll().size());
        model.addAttribute("todayCount", priceService.findForDate(LocalDate.now()).size());
        model.addAttribute("recent", all.size() > 6 ? all.subList(0, 6) : all);
        return "admin/dashboard";
    }

    @GetMapping("/crops")
    public String crops(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("crops", cropService.findAll());
        return "admin/crops";
    }

    @PostMapping("/crops")
    public String addCrop(@RequestParam String name, @RequestParam String unit,
                           HttpSession session, Model model) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            cropService.register(name, unit);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        addSessionAttrs(model, session);
        model.addAttribute("crops", cropService.findAll());
        return "admin/crops";
    }

    @PostMapping("/crops/{id}/delete")
    public String deleteCrop(@PathVariable Long id, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        cropService.delete(id);
        return "redirect:/admin/crops";
    }

    @GetMapping("/markets")
    public String markets(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("markets", marketService.findAll());
        return "admin/markets";
    }

    @PostMapping("/markets")
    public String addMarket(@RequestParam String name, @RequestParam String location,
                             HttpSession session, Model model) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            marketService.register(name, location);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        addSessionAttrs(model, session);
        model.addAttribute("markets", marketService.findAll());
        return "admin/markets";
    }

    @PostMapping("/markets/{id}/delete")
    public String deleteMarket(@PathVariable Long id, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        marketService.delete(id);
        return "redirect:/admin/markets";
    }

    @GetMapping("/prices")
    public String prices(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("crops", cropService.findAll());
        model.addAttribute("markets", marketService.findAll());
        List<PriceRecord> recent = priceService.findAll();
        model.addAttribute("recent", recent.size() > 10 ? recent.subList(0, 10) : recent);
        model.addAttribute("today", LocalDate.now());
        return "admin/prices";
    }

    @PostMapping("/prices")
    public String submitPrice(@RequestParam Long cropId, @RequestParam Long marketId,
                               @RequestParam BigDecimal price, @RequestParam String priceDate,
                               @RequestParam(defaultValue = "false") boolean replaceExisting,
                               HttpSession session, Model model) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;

        String enteredBy = (String) session.getAttribute("name");
        Crop crop = cropService.findById(cropId);
        Market market = marketService.findById(marketId);

        try {
            priceService.postPrice(crop, market, price, LocalDate.parse(priceDate), enteredBy, replaceExisting);
            model.addAttribute("success", "Price saved to the centralized board — visible to farmers immediately.");
        } catch (DuplicatePriceException e) {
            model.addAttribute("duplicateError", e.getMessage());
            model.addAttribute("dupCropId", cropId);
            model.addAttribute("dupMarketId", marketId);
            model.addAttribute("dupPrice", price);
            model.addAttribute("dupDate", priceDate);
        } catch (InvalidPriceDateException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        addSessionAttrs(model, session);
        model.addAttribute("crops", cropService.findAll());
        model.addAttribute("markets", marketService.findAll());
        List<PriceRecord> recent = priceService.findAll();
        model.addAttribute("recent", recent.size() > 10 ? recent.subList(0, 10) : recent);
        model.addAttribute("today", LocalDate.now());
        return "admin/prices";
    }

    @GetMapping("/farmers")
    public String farmers(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("farmers", farmerService.findAll());
        model.addAttribute("markets", marketService.findAll());
        return "admin/farmers";
    }

    @PostMapping("/farmers")
    public String addFarmer(@RequestParam String name, @RequestParam(required = false) String phone,
                             @RequestParam Long marketId, HttpSession session, Model model) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            farmerService.register(name, phone, marketService.findById(marketId));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        addSessionAttrs(model, session);
        model.addAttribute("farmers", farmerService.findAll());
        model.addAttribute("markets", marketService.findAll());
        return "admin/farmers";
    }

    @PostMapping("/farmers/{id}/delete")
    public String deleteFarmer(@PathVariable Long id, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        farmerService.delete(id);
        return "redirect:/admin/farmers";
    }

    @GetMapping("/broadcast")
    public String broadcast(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("todayPrices", priceService.findForDate(LocalDate.now()));
        model.addAttribute("farmerCount", farmerService.findAll().size());
        model.addAttribute("broadcasts", broadcastService.findAll());
        return "admin/broadcast";
    }

    @PostMapping("/broadcast")
    public String dispatch(HttpSession session, Model model) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        List<PriceRecord> todaysRecords = priceService.findForDate(LocalDate.now());
        int recipientCount = farmerService.findAll().size();
        broadcastService.dispatch(todaysRecords, recipientCount, (String) session.getAttribute("name"));

        addSessionAttrs(model, session);
        model.addAttribute("success", "Broadcast dispatched to " + recipientCount + " registered farmers.");
        model.addAttribute("todayPrices", todaysRecords);
        model.addAttribute("farmerCount", recipientCount);
        model.addAttribute("broadcasts", broadcastService.findAll());
        return "admin/broadcast";
    }
}
