package com.berkay.restaurant.service.domain.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserValidationResponse {
    private UUID userId;
    private boolean valid;
    private String errorMessage;
}
