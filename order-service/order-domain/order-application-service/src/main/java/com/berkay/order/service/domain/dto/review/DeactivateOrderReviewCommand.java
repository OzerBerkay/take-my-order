package com.berkay.order.service.domain.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeactivateOrderReviewCommand {

    @NotNull
    private UUID restaurantId;

    @NotNull
    private UUID reviewId;

    @NotBlank
    @Size(max = 500)
    private String reason;

    private UUID moderatorId;
}
