package com.berkay.identity.service.dataaccess.query;

import com.berkay.identity.service.dataaccess.role.repository.RoleJpaRepository;
import com.berkay.identity.service.dataaccess.role.repository.RolePermissionJpaRepository;
import com.berkay.identity.service.dataaccess.user.repository.UserJpaRepository;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RoleQueryRepositoryImplTest {

    @Mock
    private RoleJpaRepository roleJpaRepository;

    @Mock
    private RolePermissionJpaRepository rolePermissionJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private RoleQueryRepositoryImpl roleQueryRepository;

    private UUID authorizedOrgUnitId;
    private UUID unauthorizedOrgUnitId;

    @BeforeEach
    void setUp() {
        authorizedOrgUnitId = UUID.randomUUID();
        unauthorizedOrgUnitId = UUID.randomUUID();
    }

    @Test
    void shouldReturnEmptyPageWhenMerchantRequestsUnauthorizedOrgUnitRoles() {
        // given
        List<UUID> authorizedOrgUnits = List.of(authorizedOrgUnitId);
        org.springframework.data.domain.Page<com.berkay.identity.service.dataaccess.role.entity.RoleEntity> emptyPage = new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList());
        org.mockito.Mockito.when(roleJpaRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class), org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        // when
        com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.RoleResponse> result = roleQueryRepository.getMerchantRoles(0, 10, null, unauthorizedOrgUnitId, authorizedOrgUnits);

        // then
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
