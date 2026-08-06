package com.berkay.identity.service.dataaccess.user.repository;

import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import com.berkay.identity.service.domain.valueobject.AccountStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserSpecificationTest {

    @Mock
    private Root<UserEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(root.get("status")).thenReturn(path);
    }

    @Test
    void hasStatus_WithNull_ShouldReturnNullPredicate() {
        Specification<UserEntity> spec = UserSpecification.hasStatus(null);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNull(result);
    }

    @Test
    void hasStatus_WithEnum_ShouldReturnEqualPredicate() {
        when(cb.equal(path, AccountStatus.PENDING_APPROVAL)).thenReturn(predicate);

        Specification<UserEntity> spec = UserSpecification.hasStatus(AccountStatus.PENDING_APPROVAL);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, AccountStatus.PENDING_APPROVAL);
    }
}
