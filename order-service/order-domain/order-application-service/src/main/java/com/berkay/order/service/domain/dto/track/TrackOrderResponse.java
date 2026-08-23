package com.berkay.order.service.domain.dto.track;

import com.berkay.domain.valueobject.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TrackOrderResponse {
    @NotNull
    private final UUID orderTrackingId;
    @NotNull
    private final UUID customerId;
    @NotNull
    private final OrderStatus orderStatus;
    private final List<String> failureMessages;
}
