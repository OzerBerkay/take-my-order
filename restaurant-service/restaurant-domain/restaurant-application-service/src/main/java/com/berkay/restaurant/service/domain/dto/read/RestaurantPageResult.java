package com.berkay.restaurant.service.domain.dto.read;

import com.berkay.restaurant.service.domain.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantPageResult {
    private final List<Restaurant> restaurants;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean isLast;
}
