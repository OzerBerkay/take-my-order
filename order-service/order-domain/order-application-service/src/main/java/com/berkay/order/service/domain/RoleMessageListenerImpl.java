package com.berkay.order.service.domain;

import com.berkay.order.service.domain.dto.message.RoleEventPayload;
import com.berkay.order.service.domain.ports.input.message.listener.role.RoleMessageListener;
import com.berkay.order.service.domain.ports.output.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoleMessageListenerImpl implements RoleMessageListener {

    private final RoleRepository roleRepository;

    public RoleMessageListenerImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void roleCreated(RoleEventPayload payload) {
        log.info("Role created event received for role id: {}", payload.getRoleId());
        roleRepository.save(payload);
    }

    @Override
    public void roleUpdated(RoleEventPayload payload) {
        log.info("Role updated event received for role id: {}", payload.getRoleId());
        roleRepository.save(payload);
    }

    @Override
    public void roleDeleted(RoleEventPayload payload) {
        log.info("Role deleted event received for role id: {}", payload.getRoleId());
        roleRepository.delete(payload.getRoleId());
    }
}
