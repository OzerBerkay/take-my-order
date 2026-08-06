package com.berkay.restaurant.service.domain.ports.output.api;

import com.berkay.restaurant.service.domain.dto.query.UserValidationResponse;

public interface IdentityServiceApiPort {
    UserValidationResponse validateUserForPersonnel(String email);
}
