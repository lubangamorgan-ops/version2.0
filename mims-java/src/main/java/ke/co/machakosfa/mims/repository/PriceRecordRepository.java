package ke.co.machakosfa.mims.repository;

import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceRecordRepository extends JpaRepository<PriceRecord, Long> {
    Optional<PriceRecord> findByCropAndMarketAndPriceDate(Crop crop, Market market, LocalDate priceDate);
    List<PriceRecord> findByPriceDateOrderByRecordedAtDesc(LocalDate priceDate);
    List<PriceRecord> findAllByOrderByRecordedAtDesc();
    List<PriceRecord> findByCropOrderByRecordedAtDesc(Crop crop);
    List<PriceRecord> findByMarketAndPriceDateOrderByRecordedAtDesc(Market market, LocalDate priceDate);
}
