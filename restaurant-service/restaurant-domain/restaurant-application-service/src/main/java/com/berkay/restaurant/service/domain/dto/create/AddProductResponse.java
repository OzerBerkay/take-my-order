package com.berkay.restaurant.service.domain.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AddProductResponse {
    private final UUID productId;
    private final String message;
}