package com.berkay.identity.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "identity-service")
public class IdentityServiceConfigData {
    private String roleEventsTopicName;
    private String permissionEventsTopicName;
    private String userEventsTopicName;
    private String customerTopicName;
}