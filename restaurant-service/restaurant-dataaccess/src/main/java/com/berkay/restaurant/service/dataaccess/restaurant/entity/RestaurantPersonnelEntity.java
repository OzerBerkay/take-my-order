package com.berkay.restaurant.service.dataaccess.restaurant.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurant_personnel")
@Entity
public class RestaurantPersonnelEntity {

    @Id
    private UUID id;
    private UUID restaurantId;
    private UUID userId;
    private ZonedDateTime createdAt;

}
