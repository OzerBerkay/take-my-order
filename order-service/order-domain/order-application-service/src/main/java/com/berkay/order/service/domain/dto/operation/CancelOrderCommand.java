package com.berkay.order.service.domain.dto.operation;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.MaterialAction;
import com.berkay.domain.valueobject.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderCommand {
    private UUID orderId;

    @NotNull(message = "currentStatus cannot be null")
    private OrderStatus currentStatus;

    @NotNull(message = "cancelReason cannot be null")
    private CancelReason cancelReason;

    private MaterialAction materialAction;

    private String internalNote;
}
