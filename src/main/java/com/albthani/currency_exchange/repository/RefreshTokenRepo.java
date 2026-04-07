package com.albthani.currency_exchange.repository;

import com.albthani.currency_exchange.model.entity.RefreshToken;
import com.albthani.currency_exchange.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    // احذف كل توكنات المستخدم عند تسجيل الخروج
    void deleteByUser(User user);

    // عدد التوكنات النشطة للمستخدم
    long countByUserAndIsRevokedFalse(User user);
}
