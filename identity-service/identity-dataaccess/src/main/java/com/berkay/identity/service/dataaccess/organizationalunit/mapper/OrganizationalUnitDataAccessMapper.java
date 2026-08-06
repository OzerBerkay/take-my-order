package com.berkay.identity.service.dataaccess.organizationalunit.mapper;

import com.berkay.identity.service.dataaccess.organizationalunit.entity.OrganizationalUnitEntity;
import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import org.springframework.stereotype.Component;

@Component
public class OrganizationalUnitDataAccessMapper {

    public OrganizationalUnitEntity organizationalUnitToOrganizationalUnitEntity(OrganizationalUnit organizationalUnit) {
        return OrganizationalUnitEntity.builder()
                .id(organizationalUnit.getId().getValue())
                .name(organizationalUnit.getName())
                .type(organizationalUnit.getType())
                .build();
    }

    public OrganizationalUnit organizationalUnitEntityToOrganizationalUnit(OrganizationalUnitEntity organizationalUnitEntity) {
        return OrganizationalUnit.builder()
                .id(new OrganizationalUnitId(organizationalUnitEntity.getId()))
                .name(organizationalUnitEntity.getName())
                .type(organizationalUnitEntity.getType())
                .build();
    }
}
