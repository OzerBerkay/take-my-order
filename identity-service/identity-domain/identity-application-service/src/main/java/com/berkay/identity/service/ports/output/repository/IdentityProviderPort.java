package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.User;

public interface IdentityProviderPort {
    /**
     * Kullanıcıyı IAM (Keycloak) sistemine kaydeder.
     * 
     * @param user     Domain User nesnesi (Email, Ad, Soyad vb. için)
     * @param password Kullanıcının ham şifresi (Sadece buraya kadar gelir, DB'ye
     *                 girmez)
     *                 User.id = Keycloak.id = Token.sub olacak
     */
    String registerUser(User user, String password);

    void deleteUser(String externalId);

    com.berkay.identity.service.dto.command.TokenResponse login(String username, String password);

    com.berkay.identity.service.dto.command.TokenResponse refreshToken(String refreshToken);

    void updatePassword(String externalId, String newPassword);

    void resetPassword(String externalUserId, String newPassword);
    void updateUserProfile(String externalUserId, String firstName, String lastName);
    void updateUserStatus(String externalUserId, boolean enabled);
    void updateUserRolesAndBranches(String externalUserId, java.util.List<String> roleIds, java.util.List<String> organizationalUnitIds);
}