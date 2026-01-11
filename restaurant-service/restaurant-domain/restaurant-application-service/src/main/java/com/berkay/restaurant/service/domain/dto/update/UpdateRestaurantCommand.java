package com.berkay.restaurant.service.domain.dto.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdateRestaurantCommand { //Restoranı aktif/pasif yapmak için
    private final UUID restaurantId;
    private final boolean active;
}
