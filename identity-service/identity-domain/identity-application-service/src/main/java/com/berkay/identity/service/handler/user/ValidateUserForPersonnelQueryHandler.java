package com.berkay.identity.service.handler.user;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.query.ValidateUserQuery;
import com.berkay.identity.service.dto.query.ValidateUserResponse;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateUserForPersonnelQueryHandler {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ValidateUserResponse validateUserForPersonnel(ValidateUserQuery query) {
        log.info("Validating user for personnel addition with email: {}", query.getEmail());
        
        Optional<User> userOpt = userRepository.findByEmail(query.getEmail());
        
        if (userOpt.isEmpty()) {
            return ValidateUserResponse.builder()
                    .valid(false)
                    .errorMessage("User not found with email: " + query.getEmail())
                    .build();
        }
        
        User user = userOpt.get();
        
        if (user.getStatus() != AccountStatus.ACTIVE) {
            return ValidateUserResponse.builder()
                    .userId(user.getId().getValue())
                    .valid(false)
                    .errorMessage("User account is not active")
                    .build();
        }
        
        if (user.getUserType() != UserType.MERCHANT) {
            return ValidateUserResponse.builder()
                    .userId(user.getId().getValue())
                    .valid(false)
                    .errorMessage("User must be a merchant to be added as personnel")
                    .build();
        }
        
        log.info("User validated successfully for personnel addition. UserId: {}", user.getId().getValue());
        return ValidateUserResponse.builder()
                .userId(user.getId().getValue())
                .valid(true)
                .build();
    }
}
