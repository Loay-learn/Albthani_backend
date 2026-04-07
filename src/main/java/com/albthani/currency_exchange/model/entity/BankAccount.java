package com.albthani.currency_exchange.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountName;

    private String accountNumber;
    private String iban;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private String currency;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL)
    private List<TransferRequest> transferRequests;
}
