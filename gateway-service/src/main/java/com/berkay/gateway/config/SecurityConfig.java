package com.berkay.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // API'lerde CSRF korumasına gerek yoktur (Session kullanmıyoruz)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // CORS konfigürasyonunu Spring Security zincirine entegre et
                .cors(Customizer.withDefaults())

                .authorizeExchange(exchanges -> exchanges
                        // Aktüatör gibi sağlık kontrolü endpointlerine izin ver
                        .pathMatchers("/actuator/**").permitAll()
                        // Kullanıcı kayıt ve giriş (Auth) endpointlerine dışarıdan yetkisiz erişime izin ver
                        .pathMatchers(HttpMethod.POST, "/auth/login", "/auth/register/**", "/auth/refresh").permitAll()
                        // Public API'lere izin ver
                        .pathMatchers(HttpMethod.GET, "/public/**").permitAll()
                        // Diğer TÜM istekler için Token (Authentication) zorunlu kıl
                        .anyExchange().authenticated()
                )
                // JWT Token doğrulamasını aktif et
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}