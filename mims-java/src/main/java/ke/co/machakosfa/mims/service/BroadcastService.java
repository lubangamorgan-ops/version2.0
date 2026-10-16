package ke.co.machakosfa.mims.service;

import ke.co.machakosfa.mims.model.Broadcast;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.repository.BroadcastRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BroadcastService {

    private final BroadcastRepository broadcastRepository;

    public BroadcastService(BroadcastRepository broadcastRepository) {
        this.broadcastRepository = broadcastRepository;
    }

    public List<Broadcast> findAll() {
        return broadcastRepository.findAllByOrderBySentAtDesc();
    }

    public Broadcast dispatch(List<PriceRecord> todaysRecords, int recipientCount, String sentBy) {
        Broadcast broadcast = new Broadcast();
        broadcast.setSentBy(sentBy);
        broadcast.setRecordCount(todaysRecords.size());
        broadcast.setRecipientCount(recipientCount);
        broadcast.setSummary(todaysRecords.stream()
                .map(r -> r.getCrop().getName() + " at " + r.getMarket().getName() + ": Ksh " + r.getPrice())
                .collect(Collectors.joining(" | ")));
        return broadcastRepository.save(broadcast);
    }
}
