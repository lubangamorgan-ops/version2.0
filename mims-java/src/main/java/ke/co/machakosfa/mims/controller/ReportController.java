package ke.co.machakosfa.mims.controller;

import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.service.CropService;
import ke.co.machakosfa.mims.service.MarketService;
import ke.co.machakosfa.mims.service.PriceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final CropService cropService;
    private final MarketService marketService;
    private final PriceService priceService;

    public ReportController(CropService cropService, MarketService marketService, PriceService priceService) {
        this.cropService = cropService;
        this.marketService = marketService;
        this.priceService = priceService;
    }

    @GetMapping
    public String reports(@RequestParam(required = false) Long cropId,
                           @RequestParam(required = false) Long marketId,
                           Model model, HttpSession session) {
        if (session.getAttribute("role") == null) {
            return "redirect:/login";
        }
        List<PriceRecord> rows = filtered(cropId, marketId);
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("rows", rows);
        model.addAttribute("crops", cropService.findAll());
        model.addAttribute("markets", marketService.findAll());
        model.addAttribute("selectedCropId", cropId);
        model.addAttribute("selectedMarketId", marketId);
        return "reports";
    }

    @GetMapping("/export.csv")
    public void exportCsv(@RequestParam(required = false) Long cropId,
                           @RequestParam(required = false) Long marketId,
                           HttpServletResponse response, HttpSession session) throws Exception {
        if (session.getAttribute("role") == null) {
            response.sendRedirect("/login");
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=mims-price-report.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Date,Crop,Market,Price (Ksh),Unit,Entered By");
            for (PriceRecord r : filtered(cropId, marketId)) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        r.getPriceDate(), r.getCrop().getName(), r.getMarket().getName(),
                        r.getPrice(), r.getCrop().getUnit(), r.getEnteredBy());
            }
        }
    }

    private List<PriceRecord> filtered(Long cropId, Long marketId) {
        List<PriceRecord> rows = priceService.findAll();
        if (cropId != null) {
            Crop crop = cropService.findById(cropId);
            rows = rows.stream().filter(r -> r.getCrop().getId().equals(crop.getId())).toList();
        }
        if (marketId != null) {
            Market market = marketService.findById(marketId);
            rows = rows.stream().filter(r -> r.getMarket().getId().equals(market.getId())).toList();
        }
        return rows;
    }
}
