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
public class ReplyOrderReviewRequest {

    @NotBlank(message = "Reply cannot be blank")
    @Size(max = 1000, message = "Reply cannot exceed 1000 characters")
    private String reply;
}
