package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.repository.MarketRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class MarketService {

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public List<Market> findAll() {
        return marketRepository.findAll();
    }

    public Market findById(Long id) {
        Long marketId = Objects.requireNonNull(id, "Market id must not be null");
        return marketRepository.findById(marketId)
                .orElseThrow(() -> new IllegalArgumentException("Market not found: " + marketId));
    }

    public Market register(String name, String location) {
        if (name == null || name.isBlank() || location == null || location.isBlank()) {
            throw new IllegalArgumentException("Market name and location are required.");
        }
        if (marketRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("\"" + name + "\" is already registered as a market.");
        }
        return marketRepository.save(new Market(name.trim(), location.trim()));
    }

    public void delete(Long id) {
        marketRepository.deleteById(Objects.requireNonNull(id, "Market id must not be null"));
    }
}
