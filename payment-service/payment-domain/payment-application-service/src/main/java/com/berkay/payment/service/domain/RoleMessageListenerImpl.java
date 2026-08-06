package com.berkay.payment.service.domain;

import com.berkay.payment.service.domain.dto.message.RoleEventPayload;
import com.berkay.payment.service.domain.ports.input.message.listener.role.RoleMessageListener;
import com.berkay.payment.service.domain.ports.output.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class RoleMessageListenerImpl implements RoleMessageListener {

    private final RoleRepository roleRepository;

    public RoleMessageListenerImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void roleCreated(RoleEventPayload payload) {
        roleRepository.save(payload);
        log.info("Role created/upserted with id: {}", payload.getRoleId());
    }

    @Override
    public void roleUpdated(RoleEventPayload payload) {
        roleRepository.save(payload);
        log.info("Role updated with id: {}", payload.getRoleId());
    }

    @Override
    public void roleDeleted(RoleEventPayload payload) {
        log.info("Role deleted event received for role id: {}", payload.getRoleId());
        roleRepository.delete(payload.getRoleId());
    }
}
