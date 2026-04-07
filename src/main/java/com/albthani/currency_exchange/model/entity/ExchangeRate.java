package com.albthani.currency_exchange.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String fromCurrency;

    @Column(nullable = false)
    private String toCurrency;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false, precision = 15, scale = 7)
    private BigDecimal rate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
