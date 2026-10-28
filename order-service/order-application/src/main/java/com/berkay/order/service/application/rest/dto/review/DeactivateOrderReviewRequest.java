package com.berkay.order.service.application.rest.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeactivateOrderReviewRequest {

    @NotBlank(message = "Reason cannot be blank")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}
