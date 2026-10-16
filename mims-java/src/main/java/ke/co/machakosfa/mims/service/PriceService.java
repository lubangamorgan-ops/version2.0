package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.exception.DuplicatePriceException;
import ke.co.machakosfa.mims.exception.InvalidPriceDateException;
import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.repository.PriceRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PriceService {

    private final PriceRecordRepository priceRecordRepository;

    public PriceService(PriceRecordRepository priceRecordRepository) {
        this.priceRecordRepository = priceRecordRepository;
    }

    public List<PriceRecord> findAll() {
        return priceRecordRepository.findAllByOrderByRecordedAtDesc();
    }

    public List<PriceRecord> findForDate(LocalDate date) {
        return priceRecordRepository.findByPriceDateOrderByRecordedAtDesc(date);
    }

    public List<PriceRecord> findForCrop(Crop crop) {
        return priceRecordRepository.findByCropOrderByRecordedAtDesc(crop);
    }

    public List<PriceRecord> findForMarketAndDate(Market market, LocalDate date) {
        return priceRecordRepository.findByMarketAndPriceDateOrderByRecordedAtDesc(market, date);
    }

    public PriceRecord postPrice(Crop crop, Market market, BigDecimal price, LocalDate priceDate,
                                  String enteredBy, boolean replaceExisting) {

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Enter a price greater than zero.");
        }
        if (priceDate == null) {
            throw new IllegalArgumentException("Select a date for this price record.");
        }
        if (priceDate.isAfter(LocalDate.now())) {
            throw new InvalidPriceDateException(
                    "Timestamp validation failed: the date cannot be in the future.");
        }

        Optional<PriceRecord> existing =
                priceRecordRepository.findByCropAndMarketAndPriceDate(crop, market, priceDate);

        if (existing.isPresent() && !replaceExisting) {
            PriceRecord dup = existing.get();
            throw new DuplicatePriceException(
                    String.format("Duplicate record: %s at %s for %s was already posted at Ksh %s.",
                            crop.getName(), market.getName(), priceDate, dup.getPrice()),
                    dup);
        }

        PriceRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setPrice(price);
            record.setEnteredBy(enteredBy);
            record.setRevised(true);
        } else {
            record = new PriceRecord();
            record.setCrop(crop);
            record.setMarket(market);
            record.setPrice(price);
            record.setPriceDate(priceDate);
            record.setEnteredBy(enteredBy);
        }
        return priceRecordRepository.save(record);
    }
}
