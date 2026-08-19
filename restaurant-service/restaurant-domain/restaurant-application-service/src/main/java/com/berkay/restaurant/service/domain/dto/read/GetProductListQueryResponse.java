package com.berkay.restaurant.service.domain.dto.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetProductListQueryResponse {
    private final UUID restaurantId;
    private final List<GetProductQueryResponse> products;
}
