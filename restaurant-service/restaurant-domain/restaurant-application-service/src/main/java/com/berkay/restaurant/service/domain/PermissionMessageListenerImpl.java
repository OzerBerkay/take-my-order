package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.message.PermissionEventPayload;
import com.berkay.restaurant.service.domain.ports.input.message.listener.permission.PermissionMessageListener;
import com.berkay.restaurant.service.domain.ports.output.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PermissionMessageListenerImpl implements PermissionMessageListener {

    private final RoleRepository roleRepository;

    public PermissionMessageListenerImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void permissionUpdated(PermissionEventPayload payload) {
        log.info("Permission updated event received for permission id: {}", payload.getPermission().getId());
        roleRepository.updatePermission(payload);
    }
}
