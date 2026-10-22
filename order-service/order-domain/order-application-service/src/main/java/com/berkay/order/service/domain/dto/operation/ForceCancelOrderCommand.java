package com.berkay.order.service.domain.dto.operation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForceCancelOrderCommand {
    private UUID orderId;

    @NotBlank(message = "adminReason cannot be blank")
    @Size(min = 10, message = "adminReason must be at least 10 characters")
    private String adminReason;

    private Boolean forceRefund;

    public boolean isForceRefund() {
        return Boolean.TRUE.equals(this.forceRefund);
    }
}
