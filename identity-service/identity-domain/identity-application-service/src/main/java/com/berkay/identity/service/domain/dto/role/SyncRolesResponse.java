package com.berkay.identity.service.domain.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SyncRolesResponse {
    private final List<SyncRoleDto> roles;
    private final ZonedDateTime nextCursor;
    private final boolean hasNextPage;
}
