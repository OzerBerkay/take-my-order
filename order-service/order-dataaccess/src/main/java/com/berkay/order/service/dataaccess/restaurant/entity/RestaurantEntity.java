package com.berkay.order.service.dataaccess.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurants", schema = "orders") // ARTIK KENDİ ŞEMAMIZDA
@Entity
public class RestaurantEntity {

    @Id
    private UUID restaurantId;

    private boolean restaurantActive;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<ProductEntity> products;

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
