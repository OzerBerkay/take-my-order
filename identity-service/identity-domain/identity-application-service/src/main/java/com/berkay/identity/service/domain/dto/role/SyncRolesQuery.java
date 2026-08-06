package com.berkay.identity.service.domain.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SyncRolesQuery {
    private final ZonedDateTime cursor;
    private final int limit;
}
