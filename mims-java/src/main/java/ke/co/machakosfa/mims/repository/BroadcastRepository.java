package ke.co.machakosfa.mims.repository;

import ke.co.machakosfa.mims.model.Broadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    List<Broadcast> findAllByOrderBySentAtDesc();
}
