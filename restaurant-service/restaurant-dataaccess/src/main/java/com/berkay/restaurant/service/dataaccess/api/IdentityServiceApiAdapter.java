package com.berkay.restaurant.service.dataaccess.api;

import com.berkay.restaurant.service.domain.dto.query.UserValidationResponse;
import com.berkay.restaurant.service.domain.ports.output.api.IdentityServiceApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class IdentityServiceApiAdapter implements IdentityServiceApiPort {

    private final RestTemplate restTemplate;
    private final String identityServiceUrl;

    public IdentityServiceApiAdapter(RestTemplate restTemplate,
                                     @Value("${restaurant-service.identity-service-url}") String identityServiceUrl) {
        this.restTemplate = restTemplate;
        this.identityServiceUrl = identityServiceUrl;
    }

    @Override
    public UserValidationResponse validateUserForPersonnel(String email) {
        String url = identityServiceUrl + "/internal/users/validate?email=" + email;
        try {
            log.info("Calling Identity Service to validate user: {}", email);
            
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpHeaders headers = new HttpHeaders();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<UserValidationResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserValidationResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call Identity Service for validation", e);
            throw new RuntimeException("Could not validate user from Identity Service", e);
        }
    }
}
