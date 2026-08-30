package com.berkay.restaurant.service.domain.dto.create.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AddProductBatchResponse {
    private final List<UUID> productIds;
    private final String message;
}
