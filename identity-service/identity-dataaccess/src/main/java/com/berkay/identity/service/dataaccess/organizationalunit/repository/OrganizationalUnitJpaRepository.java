package com.berkay.identity.service.dataaccess.organizationalunit.repository;

import com.berkay.identity.service.dataaccess.organizationalunit.entity.OrganizationalUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganizationalUnitJpaRepository extends JpaRepository<OrganizationalUnitEntity, UUID> {
}
