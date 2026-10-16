package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Farmer;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.repository.FarmerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    public List<Farmer> findAll() {
        return farmerRepository.findAll();
    }

    public Farmer findById(Long id) {
        Long farmerId = Objects.requireNonNull(id, "Farmer id must not be null");
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
    }

    public Optional<Farmer> findByName(String name) {
        return farmerRepository.findByNameIgnoreCase(name);
    }

    public Farmer register(String name, String phone, Market market) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Farmer name is required.");
        }
        if (farmerRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("A farmer named \"" + name + "\" is already registered.");
        }
        return farmerRepository.save(new Farmer(name.trim(), phone, market));
    }

    public Farmer findOrRegister(String name, String phone, Market market) {
        return farmerRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> farmerRepository.save(new Farmer(name.trim(), phone, market)));
    }

    public void delete(Long id) {
        farmerRepository.deleteById(Objects.requireNonNull(id, "Farmer id must not be null"));
    }
}
