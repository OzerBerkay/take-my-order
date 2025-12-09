package com.berkay.customer.service.outbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CustomerEventPayload { //Veritabanındaki payload sütununa basacağımız JSON'un karşılığı olan DTO.

    @JsonProperty
    private String customerId;

    @JsonProperty
    private String username;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonProperty
    private String email;

    @JsonProperty
    private ZonedDateTime createdAt;
}
