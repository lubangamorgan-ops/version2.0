package ke.co.machakosfa.mims.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_records", uniqueConstraints = @UniqueConstraint(columnNames = {"crop_id", "market_id", "price_date"}))
public class PriceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "market_id")
    private Market market;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(nullable = false)
    private String enteredBy;

    @Column(nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    private boolean revised = false;

    public PriceRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public Market getMarket() { return market; }
    public void setMarket(Market market) { this.market = market; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDate getPriceDate() { return priceDate; }
    public void setPriceDate(LocalDate priceDate) { this.priceDate = priceDate; }
    public String getEnteredBy() { return enteredBy; }
    public void setEnteredBy(String enteredBy) { this.enteredBy = enteredBy; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public boolean isRevised() { return revised; }
    public void setRevised(boolean revised) { this.revised = revised; }
}
