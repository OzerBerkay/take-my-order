package com.berkay.identity.service.ports.input.service;

import com.berkay.identity.service.dto.command.*;
import com.berkay.identity.service.dto.query.*;

import java.util.List;
import java.util.UUID;

public interface UserApplicationService {

    // Müşteri Kaydı
    CreateUserResponse registerCustomer(RegisterCustomerCommand command);

    // Merchant Kaydı
    CreateUserResponse registerMerchant(RegisterMerchantCommand command);

    // Admin Tarafından Kullanıcı Oluşturma
    CreateUserResponse registerInternalUser(RegisterInternalUserCommand command);

    // Doğrulama İşlemleri
    void verifyEmail(VerifyEmailCommand command);

    void verifyPhone(VerifyPhoneCommand command);

    UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command);

    UpdateUserStatusResponse updateUserStatus(UpdateUserStatusCommand command);


    void forceResetPassword(ForceResetPasswordCommand command);

    void assignRoleToUser(AssignUserRoleCommand command);

    void unassignRoleFromUser(UnassignUserRoleCommand command);

    // GET Endpoints
    PageResult<UserResponse> getAdminUsers(GetAdminUsersQuery query);

    PageResult<MerchantUserResponse> getMerchantUsers(GetMerchantUsersQuery query);

    com.berkay.identity.service.dto.query.UserResponse getAdminUserById(java.util.UUID userId);

    com.berkay.identity.service.dto.query.ValidateUserResponse validateUserForPersonnel(com.berkay.identity.service.dto.query.ValidateUserQuery query);

    MerchantUserResponse getMerchantUserById(UUID userId, UUID orgUnitId, List<UUID> authorizedOrgUnitIds);

    com.berkay.identity.service.dto.query.UserResponse getUserProfile(java.util.UUID userId);
}