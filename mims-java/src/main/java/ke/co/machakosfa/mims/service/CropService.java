package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.repository.CropRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class CropService {

    private final CropRepository cropRepository;

    public CropService(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    public List<Crop> findAll() {
        return cropRepository.findAll();
    }

    public Crop findById(Long id) {
        Long cropId = Objects.requireNonNull(id, "Crop id must not be null");
        return cropRepository.findById(cropId)
                .orElseThrow(() -> new IllegalArgumentException("Crop not found: " + cropId));
    }

    public Crop register(String name, String unit) {
        if (name == null || name.isBlank() || unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Crop name and unit are required.");
        }
        if (cropRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("\"" + name + "\" is already registered as a crop category.");
        }
        return cropRepository.save(new Crop(name.trim(), unit.trim()));
    }

    public void delete(Long id) {
        cropRepository.deleteById(Objects.requireNonNull(id, "Crop id must not be null"));
    }
}
