package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.RoleId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findByEmail(String email);

    // Rolleri ID listesinden bulmak için
    List<Role> findRolesByIds(List<RoleId> roleIds);

    // İsimden Rol bulmak için (Customer ve Merchant vb. default rollerini çekmek için)
    Optional<Role> findRoleByName(String roleName);

    Optional<User> findByPhoneNumber(String phoneNumber);
}