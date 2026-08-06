package com.berkay.identity.service.dataaccess.organizationalunit.adapter;

import com.berkay.identity.service.dataaccess.organizationalunit.entity.OrganizationalUnitEntity;
import com.berkay.identity.service.dataaccess.organizationalunit.mapper.OrganizationalUnitDataAccessMapper;
import com.berkay.identity.service.dataaccess.organizationalunit.repository.OrganizationalUnitJpaRepository;
import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import com.berkay.identity.service.ports.output.repository.OrganizationalUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrganizationalUnitRepositoryImpl implements OrganizationalUnitRepository {

    private final OrganizationalUnitJpaRepository organizationalUnitJpaRepository;
    private final OrganizationalUnitDataAccessMapper organizationalUnitDataAccessMapper;

    @Override
    public OrganizationalUnit save(OrganizationalUnit organizationalUnit) {
        OrganizationalUnitEntity organizationalUnitEntity = organizationalUnitDataAccessMapper
                .organizationalUnitToOrganizationalUnitEntity(organizationalUnit);
        return organizationalUnitDataAccessMapper
                .organizationalUnitEntityToOrganizationalUnit(organizationalUnitJpaRepository.save(organizationalUnitEntity));
    }

    @Override
    public Optional<OrganizationalUnit> findById(OrganizationalUnitId organizationalUnitId) {
        return organizationalUnitJpaRepository.findById(organizationalUnitId.getValue())
                .map(organizationalUnitDataAccessMapper::organizationalUnitEntityToOrganizationalUnit);
    }
}
