package com.berkay.order.service.domain.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CreateOrderReviewCommand {

    @NotNull
    private UUID orderId;

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 1000)
    private String comment;
}
