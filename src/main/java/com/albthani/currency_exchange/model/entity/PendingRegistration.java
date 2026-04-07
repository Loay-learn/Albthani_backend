package com.albthani.currency_exchange.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // مشفر

    private String phone;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime otpExpiresAt;

    @Column(nullable = false)
    private Boolean isUsed = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
