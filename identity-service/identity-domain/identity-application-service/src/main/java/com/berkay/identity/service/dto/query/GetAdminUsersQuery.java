package com.berkay.identity.service.dto.query;

import com.berkay.identity.service.domain.valueobject.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetAdminUsersQuery {
    private final int page;
    private final int size;

    private final String email;
    private final String firstName;
    private final String lastName;
    private final com.berkay.identity.service.domain.valueobject.AccountStatus status;
    private final UserType userType;
    private final UUID orgUnitId;
    private final UUID roleId;
}
