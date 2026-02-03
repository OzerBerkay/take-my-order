package com.berkay.identity.service.dataaccess.user.adapter;

import com.berkay.identity.service.dataaccess.user.entity.RoleEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import com.berkay.identity.service.dataaccess.user.exception.IdentityDataaccessException;
import com.berkay.identity.service.dataaccess.user.mapper.UserDataAccessMapper;
import com.berkay.identity.service.dataaccess.user.repository.RoleJpaRepository;
import com.berkay.identity.service.dataaccess.user.repository.UserJpaRepository;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserDataAccessMapper userDataAccessMapper;

    @Override
    public User save(User user) {
        // Domain -> Entity çevir
        UserEntity userEntity = userDataAccessMapper.userToUserEntity(user);

        // Adreslerin User ile olan ilişkisini set et (JPA gerekliliği)
        // Mapper'da userEntity.setAddresses(...) dedik ama
        // addressEntity.setUser(userEntity) demedik. Burada diyoruz.
        if (userEntity.getAddresses() != null) {
            userEntity.getAddresses().forEach(address -> address.setUser(userEntity));
        }

        // --- ROL İLİŞKİSİ (KRİTİK DÜZELTME) ---
        // Mapper 'detached' role nesneleri oluşturdu. Cascade kullanmadığımız için
        // bunları DB'den çekilen 'managed' nesnelerle değiştirmeliyiz.
        if (userEntity.getRoles() != null && !userEntity.getRoles().isEmpty()) {
            Set<UUID> roleIds = userEntity.getRoles().stream()
                    .map(RoleEntity::getId)
                    .collect(Collectors.toSet());

            // Veritabanındaki gerçek kayıtları getir
            List<RoleEntity> managedRoles = roleJpaRepository.findAllById(roleIds);

            if (managedRoles.size() != roleIds.size()) {
                throw new IdentityDataaccessException("One or more roles not found in database! Requested: " + roleIds);
            }

            // UserEntity içindeki seti bunlarla değiştir
            userEntity.setRoles(new HashSet<>(managedRoles));
        }

        // Kaydet
        UserEntity savedUserEntity = userJpaRepository.save(userEntity);

        // Entity -> Domain çevir ve dön
        return userDataAccessMapper.userEntityToUser(savedUserEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userDataAccessMapper::userEntityToUser);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userJpaRepository.findByPhoneNumber(phoneNumber)
                .map(userDataAccessMapper::userEntityToUser);
    }

    @Override
    public List<Role> findRolesByIds(List<RoleId> roleIds) {
        // VO Listesini UUID Listesine çevir
        List<UUID> roleUuids = roleIds.stream()
                .map(RoleId::getValue)
                .collect(Collectors.toList());

        return roleJpaRepository.findAllById(roleUuids).stream()
                .map(userDataAccessMapper::roleEntityToRole)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Role> findRoleByName(String roleName) {
        return roleJpaRepository.findByName(roleName)
                .map(userDataAccessMapper::roleEntityToRole);
    }
}