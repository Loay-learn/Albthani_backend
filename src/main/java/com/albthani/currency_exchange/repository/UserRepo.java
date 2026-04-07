package com.albthani.currency_exchange.repository;

import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    // للـ Authentication
    Optional<User> findByEmail(String email);

    // للتحقق عند التسجيل
    boolean existsByEmail(String email);

    // للأدمن — عرض كل المستخدمين
    Page<User> findByRole(Role role, Pageable pageable);

    // للأدمن — البحث عن مستخدم
    Page<User> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    // للأدمن — عرض المحظورين
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    List<User> findByRole(Role role);
}
