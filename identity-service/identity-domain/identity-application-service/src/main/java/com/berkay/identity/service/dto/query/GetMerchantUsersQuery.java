package com.berkay.identity.service.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetMerchantUsersQuery {
    private final int page;
    private final int size;

    private final String email;
    private final String firstName;
    private final String lastName;
    
    // The filter orgUnitId provided in the request
    private final UUID filterOrgUnitId;

    // The orgUnitIds that the merchant is actually authorized to view (from JWT)
    private final List<UUID> authorizedOrgUnitIds;

    private final UUID roleId;
}
