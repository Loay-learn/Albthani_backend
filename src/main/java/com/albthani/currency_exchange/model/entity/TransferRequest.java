package com.albthani.currency_exchange.model.entity;

import com.albthani.currency_exchange.model.enums.TransferStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer_request")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "BIGINT GENERATED ALWAYS AS IDENTITY",
            updatable = false,
            insertable = false)
    private Long referenceNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", nullable = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private User processedBy;

    @Column(nullable = false)
    private String fromCurrency;

    @Column(nullable = false)
    private String toCurrency;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String customerBankName;

    @Column(nullable = false)
    private String customerAccountName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_by_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User reservedBy;

    @Column(nullable = false)
    private String customerAccountNumber; // ✅

    @Column(precision = 15, scale = 2)
    private BigDecimal convertedAmount;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal rateUsed;

    @Column(nullable = false)
    private String whatsappNumber;

    @Column(nullable = false)
    private String screenshotUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    private String adminNote;

    private String confirmationImage;

    private LocalDateTime processedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
