package com.berkay.identity.service.outbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserEventPayload {

    @JsonProperty
    private String userId;

    @JsonProperty
    private String email;

    @JsonProperty
    private String phoneNumber;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonProperty
    private String userType; // CUSTOMER, MERCHANT

    @JsonProperty
    private String accountStatus; // ACTIVE, PENDING_VERIFICATION...

    @JsonProperty
    private ZonedDateTime createdAt;
}