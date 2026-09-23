package vn.iotstar.entity;

import vn.iotstar.util.ImageValueUtil;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, length=255)
    private String name;

    @Column(name="description", length=2000)
    private String description;

    @Column(name="price", nullable=false, precision=18, scale=2)
    private BigDecimal price;

    @Column(name="quantity", nullable=false)
    private int quantity;

    // url|publicId
    @Column(name="image", length=1000)
    private String image;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    public Product() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Transient
    public String getImageUrl() {
        return ImageValueUtil.extractUrl(this.image);
    }

    @Transient
    public String getImagePublicId() {
        return ImageValueUtil.extractPublicId(this.image);
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}