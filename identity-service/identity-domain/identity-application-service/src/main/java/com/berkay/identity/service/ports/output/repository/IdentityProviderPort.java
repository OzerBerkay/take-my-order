package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.User;

public interface IdentityProviderPort {
    /**
     * Kullanıcıyı IAM (Keycloak) sistemine kaydeder.
     * @param user Domain User nesnesi (Email, Ad, Soyad vb. için)
     * @param password Kullanıcının ham şifresi (Sadece buraya kadar gelir, DB'ye girmez)
     * User.id = Keycloak.id = Token.sub olacak
     */
    String registerUser(User user, String password);
}