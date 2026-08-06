package com.berkay.identity.service.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "identity-service.sample")
public class SampleUserProperties {

    private SampleUser customer = new SampleUser();
    private SampleUser merchant = new SampleUser();

    @Data
    public static class SampleUser {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;
    }
}
