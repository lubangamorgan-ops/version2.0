package ke.co.machakosfa.mims.controller;

import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.service.BroadcastService;
import ke.co.machakosfa.mims.service.CropService;
import ke.co.machakosfa.mims.service.MarketService;
import ke.co.machakosfa.mims.service.PriceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/farmer")
public class FarmerController {

    private final CropService cropService;
    private final MarketService marketService;
    private final PriceService priceService;
    private final BroadcastService broadcastService;

    public FarmerController(CropService cropService, MarketService marketService,
                             PriceService priceService, BroadcastService broadcastService) {
        this.cropService = cropService;
        this.marketService = marketService;
        this.priceService = priceService;
        this.broadcastService = broadcastService;
    }

    private String requireFarmer(HttpSession session) {
        if (!"farmer".equals(session.getAttribute("role"))) {
            return "redirect:/login?role=farmer";
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String guard = requireFarmer(session);
        if (guard != null) return guard;

        Long marketId = (Long) session.getAttribute("marketId");
        Market myMarket = marketService.findById(marketId);
        List<PriceRecord> myMarketToday = priceService.findForMarketAndDate(myMarket, LocalDate.now());
        List<PriceRecord> allToday = priceService.findForDate(LocalDate.now());

        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("myMarket", myMarket);
        model.addAttribute("myMarketToday", myMarketToday);
        model.addAttribute("allToday", allToday);
        model.addAttribute("crops", cropService.findAll());
        model.addAttribute("markets", marketService.findAll());
        return "farmer/dashboard";
    }

    @GetMapping("/lookup")
    public String lookup(@RequestParam(required = false) Long cropId,
                          @RequestParam(required = false) Long marketId,
                          Model model, HttpSession session) {
        String guard = requireFarmer(session);
        if (guard != null) return guard;

        List<PriceRecord> results = priceService.findAll();
        if (cropId != null) {
            Crop crop = cropService.findById(cropId);
            results = results.stream().filter(r -> r.getCrop().getId().equals(crop.getId())).toList();
        }
        if (marketId != null) {
            Market market = marketService.findById(marketId);
            results = results.stream().filter(r -> r.getMarket().getId().equals(market.getId())).toList();
        }

        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("results", results.size() > 25 ? results.subList(0, 25) : results);
        model.addAttribute("crops", cropService.findAll());
        model.addAttribute("markets", marketService.findAll());
        model.addAttribute("selectedCropId", cropId);
        model.addAttribute("selectedMarketId", marketId);
        return "farmer/lookup";
    }

    @GetMapping("/trends")
    public String trends(@RequestParam(required = false) Long cropId, Model model, HttpSession session) {
        String guard = requireFarmer(session);
        if (guard != null) return guard;

        List<Crop> crops = cropService.findAll();
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("crops", crops);

        Long selected = cropId != null ? cropId : (crops.isEmpty() ? null : crops.get(0).getId());
        if (selected != null) {
            Crop crop = cropService.findById(selected);
            List<PriceRecord> rows = priceService.findForCrop(crop);
            model.addAttribute("selectedCrop", crop);
            model.addAttribute("rows", rows.size() > 8 ? rows.subList(0, 8) : rows);
            int max = rows.stream().mapToInt(r -> r.getPrice().intValue()).max().orElse(1);
            model.addAttribute("maxPrice", Math.max(max, 1));
        }
        return "farmer/trends";
    }

    @GetMapping("/alerts")
    public String alerts(Model model, HttpSession session) {
        String guard = requireFarmer(session);
        if (guard != null) return guard;
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("broadcasts", broadcastService.findAll());
        return "farmer/alerts";
    }
}
