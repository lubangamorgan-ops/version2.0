package ke.co.machakosfa.mims.exception;

import ke.co.machakosfa.mims.model.PriceRecord;

public class DuplicatePriceException extends RuntimeException {
    private final PriceRecord existingRecord;

    public DuplicatePriceException(String message, PriceRecord existingRecord) {
        super(message);
        this.existingRecord = existingRecord;
    }

    public PriceRecord getExistingRecord() {
        return existingRecord;
    }
}
