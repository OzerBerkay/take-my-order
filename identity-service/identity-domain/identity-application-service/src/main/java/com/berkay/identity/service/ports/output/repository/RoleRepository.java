package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.Role;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface RoleRepository {
    Role save(Role role);

    // Döküman Madde 3.b: context_id + name ikilisi Unique olmalıdır
    boolean existsByNameAndOrganizationalUnitId(String name, UUID organizationalUnitId);

    java.util.Optional<Role> findByNameAndOrganizationalUnitId(String name, UUID organizationalUnitId);

    java.util.Optional<Role> findByName(String name);

    java.util.Optional<Role> findById(com.berkay.identity.service.domain.valueobject.RoleId roleId);

    List<Role> findAllById(List<UUID> roleIds);

    void delete(Role role);

    // Update sırasında, eğer isim değişiyorsa aynı context'te başka bir rol bu ismi kullanıyor mu kontrolü
    boolean existsByNameAndOrganizationalUnitIdAndIdNot(String name, java.util.UUID organizationalUnitId, java.util.UUID roleId);

    List<Role> findRolesUpdatedAfter(ZonedDateTime cursor, int limit);
}