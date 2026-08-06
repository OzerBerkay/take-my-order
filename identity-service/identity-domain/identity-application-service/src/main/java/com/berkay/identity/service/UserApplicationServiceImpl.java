package com.berkay.identity.service;

import com.berkay.identity.service.dto.command.*;
import com.berkay.identity.service.dto.query.*;

import java.util.List;
import java.util.UUID;
import com.berkay.identity.service.handler.*;

import com.berkay.identity.service.ports.input.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

    private final RegisterCustomerCommandHandler registerCustomerCommandHandler;
    private final RegisterMerchantCommandHandler registerMerchantCommandHandler;
    private final RegisterInternalUserCommandHandler registerInternalUserCommandHandler;
    private final UserVerificationCommandHandler userVerificationCommandHandler;
    private final UpdateUserProfileCommandHandler updateUserProfileCommandHandler;
    private final UpdateUserStatusCommandHandler updateUserStatusCommandHandler;
    private final UpdatePasswordCommandHandler updatePasswordCommandHandler;

    private final com.berkay.identity.service.handler.user.AssignUserRoleCommandHandler assignUserRoleCommandHandler;
    private final com.berkay.identity.service.handler.user.UnassignUserRoleCommandHandler unassignUserRoleCommandHandler;
    private final com.berkay.identity.service.handler.user.ValidateUserForPersonnelQueryHandler validateUserForPersonnelQueryHandler;
    
    private final com.berkay.identity.service.ports.output.repository.UserQueryRepository userQueryRepository;

    @Override
    public CreateUserResponse registerCustomer(RegisterCustomerCommand command) {
        return registerCustomerCommandHandler.registerCustomer(command);
    }

    @Override
    public CreateUserResponse registerMerchant(RegisterMerchantCommand command) {
        return registerMerchantCommandHandler.registerMerchant(command);
    }

    @Override
    public CreateUserResponse registerInternalUser(RegisterInternalUserCommand command) {
        return registerInternalUserCommandHandler.registerInternalUser(command);
    }

    @Override
    public void verifyEmail(VerifyEmailCommand command) {
        userVerificationCommandHandler.verifyEmail(command);
    }

    @Override
    public void verifyPhone(VerifyPhoneCommand command) {
        userVerificationCommandHandler.verifyPhone(command);
    }

    @Override
    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command) {
        return updateUserProfileCommandHandler.updateUserProfile(command);
    }

    @Override
    public UpdateUserStatusResponse updateUserStatus(UpdateUserStatusCommand command) {
        return updateUserStatusCommandHandler.updateUserStatus(command);
    }


    @Override
    public void forceResetPassword(ForceResetPasswordCommand command) {
        updatePasswordCommandHandler.forceResetPassword(command);
    }



    @Override
    public void assignRoleToUser(AssignUserRoleCommand command) {
        assignUserRoleCommandHandler.assign(command);
    }

    @Override
    public void unassignRoleFromUser(UnassignUserRoleCommand command) {
        unassignUserRoleCommandHandler.unassign(command);
    }

    @Override
    public com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.UserResponse> getAdminUsers(com.berkay.identity.service.dto.query.GetAdminUsersQuery query) {
        return userQueryRepository.getAdminUsers(query);
    }

    @Override
    public com.berkay.identity.service.dto.query.UserResponse getAdminUserById(java.util.UUID userId) {
        return userQueryRepository.getAdminUserById(userId);
    }

    @Override
    public PageResult<MerchantUserResponse> getMerchantUsers(GetMerchantUsersQuery query) {
        return userQueryRepository.getMerchantUsers(query);
    }

    @Override
    public MerchantUserResponse getMerchantUserById(UUID userId, UUID orgUnitId, List<UUID> authorizedOrgUnitIds) {
        return userQueryRepository.getMerchantUserById(userId, orgUnitId, authorizedOrgUnitIds);
    }

    @Override
    public com.berkay.identity.service.dto.query.UserResponse getUserProfile(java.util.UUID userId) {
        return userQueryRepository.getAdminUserById(userId);
    }

    @Override
    public com.berkay.identity.service.dto.query.ValidateUserResponse validateUserForPersonnel(com.berkay.identity.service.dto.query.ValidateUserQuery query) {
        return validateUserForPersonnelQueryHandler.validateUserForPersonnel(query);
    }
}