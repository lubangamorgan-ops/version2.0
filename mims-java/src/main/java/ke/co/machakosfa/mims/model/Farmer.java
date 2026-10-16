package ke.co.machakosfa.mims.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmers")
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String phone;

    @ManyToOne
    @JoinColumn(name = "market_id")
    private Market market;

    @Column(nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    public Farmer() {}

    public Farmer(String name, String phone, Market market) {
        this.name = name;
        this.phone = phone;
        this.market = market;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Market getMarket() { return market; }
    public void setMarket(Market market) { this.market = market; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
