package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.dto.role.SyncPermissionDto;
import com.berkay.identity.service.domain.dto.role.SyncRoleDto;
import com.berkay.identity.service.domain.dto.role.SyncRolesQuery;
import com.berkay.identity.service.domain.dto.role.SyncRolesResponse;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
public class SyncRolesQueryHandler {

    private final RoleRepository roleRepository;

    public SyncRolesQueryHandler(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public SyncRolesResponse syncRoles(SyncRolesQuery query) {
        ZonedDateTime cursor = query.getCursor() != null ? query.getCursor() : ZonedDateTime.parse("1970-01-01T00:00:00Z");
        int limit = query.getLimit() > 0 ? query.getLimit() : 100;
        
        // Fetch Limit + 1 to determine hasNextPage
        List<Role> roles = roleRepository.findRolesUpdatedAfter(cursor, limit + 1);

        boolean hasNextPage = roles.size() > limit;
        List<Role> rolesToReturn = hasNextPage ? roles.subList(0, limit) : roles;

        List<SyncRoleDto> roleDtos = rolesToReturn.stream()
                .map(role -> SyncRoleDto.builder()
                        .id(role.getId().getValue())
                        .name(role.getName())
                        .userType(role.getUserType().name())
                        .organizationalUnitId(role.getOrganizationalUnitId())
                        .permissions(role.getPermissions().stream()
                                .filter(p -> p.isActive())
                                .map(p -> SyncPermissionDto.builder()
                                        .id(p.getId().getValue())
                                        .code(p.getCode())
                                        .domain(p.getDomain().name())
                                        .build())
                                .toList())
                        .updatedAt(role.getUpdatedAt())
                        .build())
                .toList();

        ZonedDateTime nextCursor = roleDtos.isEmpty() ? null : roleDtos.get(roleDtos.size() - 1).getUpdatedAt();

        return SyncRolesResponse.builder()
                .roles(roleDtos)
                .nextCursor(nextCursor)
                .hasNextPage(hasNextPage)
                .build();
    }
}
