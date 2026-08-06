package com.berkay.order.service.domain.ports.output.security;

import java.util.UUID;

public interface AuthenticationServicePort {
    UUID getCurrentUserId();
    String getCurrentUserName();
}