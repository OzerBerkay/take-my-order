package com.berkay.identity.service.dto.query;

import com.berkay.identity.service.domain.valueobject.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class MerchantUserResponse {
    private final UUID id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final UserType userType;
    private final Boolean status;
    private final UUID organizationalUnitId; // Tekil context ID'si
    private final List<RoleResponse> roles; // Yalnızca bu organizationalUnitId'ye ait filtrelenmiş roller
}
