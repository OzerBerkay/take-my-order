package com.berkay.restaurant.service.dataaccess.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurants")
@Entity
public class RestaurantEntity {

    @Id
    private UUID restaurantId;

    private String restaurantName;
    private boolean isActive;
    private boolean available;

    private String street;
    private String city;
    private String postalCode;
    
    private String phoneNumber;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private Integer averageDeliveryTimeInMinutes;
    
    @ManyToMany
    @JoinTable(
        name = "restaurant_cuisines",
        schema = "restaurant",
        joinColumns = @JoinColumn(name = "restaurant_id"),
        inverseJoinColumns = @JoinColumn(name = "cuisine_id")
    )
    private Set<CuisineEntity> cuisines;
    
    private String description;
    private String logoUrl;
    
    private String bannerUrl;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductEntity> menu;

    @Column(name = "category_version")
    private Long categoryVersion;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductCategoryEntity> categories;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantEntity that = (RestaurantEntity) o;
        return restaurantId.equals(that.restaurantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId);
    }
}