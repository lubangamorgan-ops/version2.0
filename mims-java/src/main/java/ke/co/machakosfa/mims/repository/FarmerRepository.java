package ke.co.machakosfa.mims.repository;

import ke.co.machakosfa.mims.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByNameIgnoreCase(String name);
    Optional<Farmer> findByAccount_Id(Long accountId);
    boolean existsByNameIgnoreCase(String name);
}
