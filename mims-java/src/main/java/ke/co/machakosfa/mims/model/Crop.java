package ke.co.machakosfa.mims.model;

import jakarta.persistence.*;

@Entity
@Table(name = "crops", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String unit;

    public Crop() {}

    public Crop(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
