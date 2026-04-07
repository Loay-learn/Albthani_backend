package com.albthani.currency_exchange.repository;

import com.albthani.currency_exchange.model.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepo extends JpaRepository<ExchangeRate, UUID> {

    // الأهم — يجيب السعر الصح حسب العملة والمبلغ
    @Query("""
        SELECT e FROM ExchangeRate e
        WHERE e.fromCurrency = :from
        AND e.toCurrency = :to
        AND e.minAmount <= :amount
        AND (e.maxAmount IS NULL OR e.maxAmount >= :amount)
        AND e.isActive = true
        """)
    Optional<ExchangeRate> findApplicableRate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("amount") BigDecimal amount
    );

    // كل الأسعار النشطة بين عملتين
    List<ExchangeRate> findByFromCurrencyAndToCurrencyAndIsActiveTrue(
            String fromCurrency,
            String toCurrency
    );

    // كل الأسعار النشطة — للعرض في الواجهة
    List<ExchangeRate> findByIsActiveTrue();

    @Query("SELECT COUNT(e) > 0 FROM ExchangeRate e " +
            "WHERE e.fromCurrency = :from " +
            "AND e.toCurrency = :to " +
            "AND (:max IS NULL OR e.minAmount <= :max) " +
            "AND (e.maxAmount IS NULL OR e.maxAmount >= :min)")
    boolean existsOverlappingRate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max);
}
