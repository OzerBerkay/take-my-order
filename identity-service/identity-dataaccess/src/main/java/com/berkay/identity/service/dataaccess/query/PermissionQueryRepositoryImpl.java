package com.berkay.identity.service.dataaccess.query;

import com.berkay.identity.service.dataaccess.permission.entity.PermissionEntity;
import com.berkay.identity.service.dataaccess.permission.repository.PermissionJpaRepository;
import com.berkay.identity.service.dto.query.PageResult;
import com.berkay.identity.service.dto.query.PermissionResponse;
import com.berkay.identity.service.ports.output.repository.PermissionQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionQueryRepositoryImpl implements PermissionQueryRepository {

    private final PermissionJpaRepository permissionJpaRepository;

    @Override
    public PageResult<PermissionResponse> getAdminPermissions(int page, int size) {
        Page<PermissionEntity> pageResult = permissionJpaRepository.findAll(PageRequest.of(page, size));
        return mapToPageResult(pageResult);
    }

    @Override
    public PageResult<PermissionResponse> getMerchantPermissions(int page, int size) {
        Specification<PermissionEntity> spec = (root, query, cb) -> cb.and(
                cb.isTrue(root.get("active")),
                cb.isFalse(root.get("isRestricted"))
        );

        Page<PermissionEntity> pageResult = permissionJpaRepository.findAll(spec, PageRequest.of(page, size));
        return mapToPageResult(pageResult);
    }

    @Override
    public Map<String, List<PermissionResponse>> getGroupedPermissions(boolean isAdmin) {
        List<PermissionEntity> permissions;
        if (isAdmin) {
            permissions = permissionJpaRepository.findAll();
        } else {
            Specification<PermissionEntity> spec = (root, query, cb) -> cb.and(
                    cb.isTrue(root.get("active")),
                    cb.isFalse(root.get("isRestricted"))
            );
            permissions = permissionJpaRepository.findAll(spec);
        }

        return permissions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.groupingBy(
                        resp -> extractDomainFromCode(resp.getName())
                ));
    }

    private PermissionResponse mapToResponse(PermissionEntity p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getCode())
                .description(p.getDescription())
                .active(p.isActive())
                .isRestricted(p.isRestricted())
                .build();
    }

    private PageResult<PermissionResponse> mapToPageResult(Page<PermissionEntity> pageResult) {
        return PageResult.<PermissionResponse>builder()
                .data(pageResult.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    private String extractDomainFromCode(String permissionName) {
        String[] parts = permissionName.split("_");
        if (parts.length >= 3) {
            return parts[parts.length - 1].toUpperCase();
        }
        return "GENERAL";
    }
}
