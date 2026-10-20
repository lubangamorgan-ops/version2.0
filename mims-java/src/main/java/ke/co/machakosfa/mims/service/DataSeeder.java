package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.repository.CropRepository;
import ke.co.machakosfa.mims.repository.MarketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Populates the database with the association's default crop categories
 * and market locations the first time the application runs. Existing
 * records (by name) are never duplicated on later restarts.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CropRepository cropRepository;
    private final MarketRepository marketRepository;
    private final AccountService accountService;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(
            CropRepository cropRepository,
            MarketRepository marketRepository,
            AccountService accountService,
            @Value("${mims.admin.username:}") String adminUsername,
            @Value("${mims.admin.email:}") String adminEmail,
            @Value("${mims.admin.password:}") String adminPassword) {
        this.cropRepository = cropRepository;
        this.marketRepository = marketRepository;
        this.accountService = accountService;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        seedCrops();
        seedMarkets();
        accountService.bootstrapAdministrator(adminUsername, adminEmail, adminPassword);
    }

    private void seedCrops() {
        Map<String, String> defaults = new LinkedHashMap<>();

        // Legumes / pulses
        defaults.put("Beans (Rosecoco)", "kg");
        defaults.put("Beans (Yellow)", "kg");
        defaults.put("Beans (Red)", "kg");
        defaults.put("Green Grams", "kg");
        defaults.put("Pigeon Peas", "kg");

        // Cereals (20 types)
        defaults.put("Maize", "90kg bag");
        defaults.put("Wheat", "90kg bag");
        defaults.put("Rice", "kg");
        defaults.put("Sorghum", "kg");
        defaults.put("Finger Millet", "kg");
        defaults.put("Pearl Millet", "kg");
        defaults.put("Barley", "90kg bag");
        defaults.put("Oats", "kg");
        defaults.put("Rye", "kg");
        defaults.put("Teff", "kg");
        defaults.put("Triticale", "kg");
        defaults.put("Foxtail Millet", "kg");
        defaults.put("Proso Millet", "kg");
        defaults.put("Fonio", "kg");
        defaults.put("Quinoa", "kg");
        defaults.put("Amaranth Grain", "kg");
        defaults.put("Buckwheat", "kg");
        defaults.put("Job's Tears (Adlay)", "kg");
        defaults.put("Spelt", "kg");
        defaults.put("Durum Wheat", "90kg bag");
        defaults.put("Popcorn Maize", "kg");

        // Vegetables
        defaults.put("Kale (Sukuma Wiki)", "kg");
        defaults.put("Tomatoes", "kg");
        defaults.put("Cabbage", "kg");
        defaults.put("Onions", "kg");
        defaults.put("Spinach", "kg");
        defaults.put("Carrots", "kg");
        defaults.put("Capsicum (Pilipili Hoho)", "kg");
        defaults.put("Irish Potatoes", "50kg bag");
        defaults.put("Sweet Potatoes", "kg");
        defaults.put("Butternut", "kg");
        defaults.put("Cucumber", "kg");
        defaults.put("Managu (African Nightshade)", "kg");

        // Fruits
        defaults.put("Mangoes", "kg");
        defaults.put("Bananas", "bunch");
        defaults.put("Avocado", "kg");
        defaults.put("Oranges", "kg");
        defaults.put("Watermelon", "kg");
        defaults.put("Pawpaw", "kg");

        // Cash crops
        defaults.put("Tea", "kg");
        defaults.put("Coffee", "kg");
        defaults.put("Tobacco", "kg");
        defaults.put("Pyrethrum", "kg");
        defaults.put("Sisal", "kg");
        defaults.put("Cotton", "kg");
        defaults.put("Sugarcane", "tonne");
        defaults.put("Macadamia", "kg");
        defaults.put("Cashew Nuts", "kg");
        defaults.put("Sunflower", "kg");
        defaults.put("Bixa (Annatto)", "kg");

        defaults.forEach((name, unit) -> {
            if (!cropRepository.existsByNameIgnoreCase(name)) {
                cropRepository.save(new Crop(name, unit));
            }
        });
    }

    private void seedMarkets() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("Machakos Town Market", "Machakos Central");
        defaults.put("Kangundo Market", "Kangundo");
        defaults.put("Tala Market", "Kathiani");

        defaults.forEach((name, location) -> {
            if (!marketRepository.existsByNameIgnoreCase(name)) {
                marketRepository.save(new Market(name, location));
            }
        });
    }
}
