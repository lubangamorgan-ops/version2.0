package ke.co.machakosfa.mims.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "broadcasts")
public class Broadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(nullable = false)
    private String sentBy;

    @Column(nullable = false)
    private int recordCount;

    @Column(nullable = false)
    private int recipientCount;

    @Column(length = 1000)
    private String summary;

    @ManyToMany
    @JoinTable(
            name = "broadcast_price_records",
            joinColumns = @JoinColumn(name = "broadcast_id"),
            inverseJoinColumns = @JoinColumn(name = "price_record_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"broadcast_id", "price_record_id"})
    )
    private List<PriceRecord> priceRecords = new ArrayList<>();

    public Broadcast() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public String getSentBy() { return sentBy; }
    public void setSentBy(String sentBy) { this.sentBy = sentBy; }
    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }
    public int getRecipientCount() { return recipientCount; }
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<PriceRecord> getPriceRecords() { return priceRecords; }
    public void setPriceRecords(List<PriceRecord> priceRecords) {
        this.priceRecords = new ArrayList<>(priceRecords);
    }
}
