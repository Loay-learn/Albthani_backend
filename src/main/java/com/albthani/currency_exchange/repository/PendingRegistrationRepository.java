package com.albthani.currency_exchange.repository;

import com.albthani.currency_exchange.model.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingRegistrationRepository
        extends JpaRepository<PendingRegistration, UUID> {

    Optional<PendingRegistration> findByEmail(String email);

    Optional<PendingRegistration> findByEmailAndOtpCodeAndIsUsedFalse(
            String email, String otpCode);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);
}
