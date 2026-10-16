package ke.co.machakosfa.mims.repository;

import ke.co.machakosfa.mims.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MarketRepository extends JpaRepository<Market, Long> {
    Optional<Market> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
