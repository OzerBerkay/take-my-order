package com.berkay.order.service.domain.outbox.model.delivery;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveredEventPayload {
    @JsonProperty
    private UUID orderId;
    @JsonProperty
    private UUID restaurantId;
    @JsonProperty
    private UUID customerId;
    @JsonProperty
    private BigDecimal price;
    @JsonProperty
    private ZonedDateTime createdAt;
}
