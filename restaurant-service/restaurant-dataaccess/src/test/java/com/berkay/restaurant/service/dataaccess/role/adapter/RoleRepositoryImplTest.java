package com.berkay.restaurant.service.dataaccess.role.adapter;

import com.berkay.restaurant.service.dataaccess.role.entity.PermissionReplicaEntity;
import com.berkay.restaurant.service.dataaccess.role.entity.RolePermissionReplicaEntity;
import com.berkay.restaurant.service.dataaccess.role.entity.RoleReplicaEntity;
import com.berkay.restaurant.service.dataaccess.role.repository.PermissionReplicaRepository;
import com.berkay.restaurant.service.dataaccess.role.repository.RolePermissionReplicaRepository;
import com.berkay.restaurant.service.dataaccess.role.repository.RoleReplicaRepository;
import com.berkay.restaurant.service.domain.dto.message.PermissionPayload;
import com.berkay.restaurant.service.domain.dto.message.RoleEventPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.CacheEvict;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryImplTest {

    @Mock
    private RoleReplicaRepository roleReplicaRepository;
    @Mock
    private PermissionReplicaRepository permissionReplicaRepository;
    @Mock
    private RolePermissionReplicaRepository rolePermissionReplicaRepository;

    @InjectMocks
    private RoleRepositoryImpl roleRepository;

    @Test
    void shouldSaveRoleAndPermissions() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        UUID permId = UUID.randomUUID();
        RoleEventPayload payload = RoleEventPayload.builder()
                .roleId(roleId)
                .name("TEST")
                .permissions(List.of(
                        PermissionPayload.builder()
                                .id(UUID.fromString(permId.toString()))
                                .code("PERM")
                                .domain("RESTAURANT")
                                .build()
                ))
                .build();

        when(permissionReplicaRepository.findAllById(anySet())).thenReturn(List.of());

        // Act
        roleRepository.save(payload);

        // Assert
        verify(roleReplicaRepository, times(1)).save(any(RoleReplicaEntity.class));
        verify(permissionReplicaRepository, times(1)).findAllById(anySet());
        
        ArgumentCaptor<List<PermissionReplicaEntity>> permCaptor = ArgumentCaptor.forClass(List.class);
        verify(permissionReplicaRepository, times(1)).saveAll(permCaptor.capture());
        assertEquals(1, permCaptor.getValue().size());
        assertEquals(permId, permCaptor.getValue().get(0).getId());

        verify(rolePermissionReplicaRepository, times(1)).deleteByRoleId(roleId);
        
        ArgumentCaptor<List<RolePermissionReplicaEntity>> rolePermCaptor = ArgumentCaptor.forClass(List.class);
        verify(rolePermissionReplicaRepository, times(1)).saveAll(rolePermCaptor.capture());
        assertEquals(1, rolePermCaptor.getValue().size());
        assertEquals(roleId, rolePermCaptor.getValue().get(0).getRoleId());
        assertEquals(permId, rolePermCaptor.getValue().get(0).getPermissionId());
    }

    @Test
    void shouldDeleteRole() {
        UUID roleId = UUID.randomUUID();
        
        roleRepository.delete(roleId);
        
        verify(rolePermissionReplicaRepository, times(1)).deleteByRoleId(roleId);
        verify(roleReplicaRepository, times(1)).deleteById(roleId);
    }
    @Test
    void shouldNotSavePermissionsFromOtherDomains() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        UUID restaurantPermId = UUID.randomUUID();
        UUID identityPermId = UUID.randomUUID();
        
        RoleEventPayload payload = RoleEventPayload.builder()
                .roleId(roleId)
                .name("TEST_ROLE")
                .permissions(List.of(
                        PermissionPayload.builder()
                                .id(restaurantPermId)
                                .code("REST_PERM")
                                .domain("RESTAURANT")
                                .build(),
                        PermissionPayload.builder()
                                .id(identityPermId)
                                .code("ID_PERM")
                                .domain("IDENTITY")
                                .build()
                ))
                .build();

        when(permissionReplicaRepository.findAllById(anySet())).thenReturn(List.of());

        // Act
        roleRepository.save(payload);

        // Assert
        verify(roleReplicaRepository, times(1)).save(any(RoleReplicaEntity.class));
        
        // Only restaurantPermId should be fetched from DB
        ArgumentCaptor<Set<UUID>> idsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(permissionReplicaRepository, times(1)).findAllById(idsCaptor.capture());
        assertEquals(1, idsCaptor.getValue().size());
        assertEquals(true, idsCaptor.getValue().contains(restaurantPermId));

        // Only restaurantPermId should be inserted
        ArgumentCaptor<List<PermissionReplicaEntity>> permCaptor = ArgumentCaptor.forClass(List.class);
        verify(permissionReplicaRepository, times(1)).saveAll(permCaptor.capture());
        assertEquals(1, permCaptor.getValue().size());
        assertEquals(restaurantPermId, permCaptor.getValue().get(0).getId());

        // Junction table should only have restaurantPermId
        ArgumentCaptor<List<RolePermissionReplicaEntity>> rolePermCaptor = ArgumentCaptor.forClass(List.class);
        verify(rolePermissionReplicaRepository, times(1)).saveAll(rolePermCaptor.capture());
        assertEquals(1, rolePermCaptor.getValue().size());
        assertEquals(restaurantPermId, rolePermCaptor.getValue().get(0).getPermissionId());
    }

    @Test
    void shouldClearJunctionTableIfNoRestaurantPermissions() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        UUID identityPermId = UUID.randomUUID();
        
        RoleEventPayload payload = RoleEventPayload.builder()
                .roleId(roleId)
                .name("TEST_ROLE")
                .permissions(List.of(
                        PermissionPayload.builder()
                                .id(identityPermId)
                                .code("ID_PERM")
                                .domain("IDENTITY")
                                .build()
                ))
                .build();

        // Act
        roleRepository.save(payload);

        // Assert
        verify(roleReplicaRepository, times(1)).save(any(RoleReplicaEntity.class));
        verify(rolePermissionReplicaRepository, times(1)).deleteByRoleId(roleId);
        
        // Should not interact with permissions DB
        verify(permissionReplicaRepository, never()).findAllById(anySet());
        verify(permissionReplicaRepository, never()).saveAll(anyList());
        verify(rolePermissionReplicaRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldHaveCacheEvictOnSaveMethod() throws NoSuchMethodException {
        Method method = RoleRepositoryImpl.class.getMethod("save", RoleEventPayload.class);
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        
        org.junit.jupiter.api.Assertions.assertNotNull(cacheEvict, "save method must be annotated with @CacheEvict");
        List<String> cacheNames = Arrays.asList(cacheEvict.value());
        org.junit.jupiter.api.Assertions.assertTrue(cacheNames.contains("roles"), "CacheEvict must clear 'roles' cache");
        org.junit.jupiter.api.Assertions.assertTrue(cacheNames.contains("roleOrgUnits"), "CacheEvict must clear 'roleOrgUnits' cache");
        org.junit.jupiter.api.Assertions.assertEquals("#payload.roleId", cacheEvict.key(), "CacheEvict key must be #payload.roleId");
    }

    @Test
    void shouldHaveCacheEvictOnDeleteMethod() throws NoSuchMethodException {
        Method method = RoleRepositoryImpl.class.getMethod("delete", UUID.class);
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        
        org.junit.jupiter.api.Assertions.assertNotNull(cacheEvict, "delete method must be annotated with @CacheEvict");
        List<String> cacheNames = Arrays.asList(cacheEvict.value());
        org.junit.jupiter.api.Assertions.assertTrue(cacheNames.contains("roles"), "CacheEvict must clear 'roles' cache");
        org.junit.jupiter.api.Assertions.assertTrue(cacheNames.contains("roleOrgUnits"), "CacheEvict must clear 'roleOrgUnits' cache");
        org.junit.jupiter.api.Assertions.assertEquals("#roleId", cacheEvict.key(), "CacheEvict key must be #roleId");
    }

    @Test
    void shouldHaveCacheEvictOnUpdatePermissionMethod() throws NoSuchMethodException {
        Method method = RoleRepositoryImpl.class.getMethod("updatePermission", com.berkay.restaurant.service.domain.dto.message.PermissionEventPayload.class);
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        
        org.junit.jupiter.api.Assertions.assertNotNull(cacheEvict, "updatePermission method must be annotated with @CacheEvict");
        List<String> cacheNames = Arrays.asList(cacheEvict.value());
        org.junit.jupiter.api.Assertions.assertTrue(cacheNames.contains("roles"), "CacheEvict must clear 'roles' cache");
        org.junit.jupiter.api.Assertions.assertTrue(cacheNames.contains("roleOrgUnits"), "CacheEvict must clear 'roleOrgUnits' cache");
        org.junit.jupiter.api.Assertions.assertTrue(cacheEvict.allEntries(), "CacheEvict must clear all entries for updatePermission");
    }
}
