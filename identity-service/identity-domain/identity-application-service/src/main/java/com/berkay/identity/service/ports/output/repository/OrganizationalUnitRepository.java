package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;

import java.util.Optional;

public interface OrganizationalUnitRepository {
    OrganizationalUnit save(OrganizationalUnit organizationalUnit);
    Optional<OrganizationalUnit> findById(OrganizationalUnitId organizationalUnitId);
}
