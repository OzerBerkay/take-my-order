package com.berkay.identity.service.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserResponse {

    private UUID userId;
    private boolean valid;
    private String errorMessage;

}
