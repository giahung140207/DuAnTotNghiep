package poly.edu.duantotnghiep_nhom2.entity;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pitches")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pitch { // Bỏ extends BaseEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PitchType type; // PITCH_5, PITCH_7

    @Column(name = "price_per_hour", nullable = false)
    private BigDecimal pricePerHour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PitchStatus status; // ACTIVE, MAINTENANCE

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "surface_image_url")
    private String surfaceImageUrl;

    @Column(name = "bench_image_url")
    private String benchImageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // --- GETTERS AND SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PitchType getType() {
        return type;
    }

    public void setType(PitchType type) {
        this.type = type;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public PitchStatus getStatus() {
        return status;
    }

    public void setStatus(PitchStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSurfaceImageUrl() {
        return surfaceImageUrl;
    }

    public void setSurfaceImageUrl(String surfaceImageUrl) {
        this.surfaceImageUrl = surfaceImageUrl;
    }

    public String getBenchImageUrl() {
        return benchImageUrl;
    }

    public void setBenchImageUrl(String benchImageUrl) {
        this.benchImageUrl = benchImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
