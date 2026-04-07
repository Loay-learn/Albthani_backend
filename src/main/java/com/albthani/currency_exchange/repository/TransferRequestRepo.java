package com.albthani.currency_exchange.repository;

import com.albthani.currency_exchange.model.entity.TransferRequest;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRequestRepo extends JpaRepository<TransferRequest, UUID> {
    // طلبات مستخدم معين — للمستخدم نفسه
    Page<TransferRequest> findByUser(User user, Pageable pageable);
    // طلبات حسب الحالة — للأدمن
    Page<TransferRequest> findByStatus(TransferStatus status, Pageable pageable);
    // طلبات حسب الأدمن اللي عالجها
    Page<TransferRequest> findByProcessedBy(User admin, Pageable pageable);
    // طلب بالـ referenceNumber — للبحث
    Optional<TransferRequest> findByReferenceNumber(Long referenceNumber);
    // إحصائيات — عدد طلبات أدمن معين
    long countByProcessedBy(User admin);
    // إحصائيات — عدد الطلبات حسب الحالة
    long countByStatus(TransferStatus status);

    // التقارير — طلبات بين تاريخين
    @Query("""
         SELECT t FROM TransferRequest t
         WHERE t.createdAt BETWEEN :startDate AND :endDate
         ORDER BY t.createdAt DESC
    """)
    List<TransferRequest> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // التقارير — مجموع المبالغ المحولة
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransferRequest t " +
            "WHERE t.status = :status " +
            "AND t.fromCurrency = :currency " +
            "AND t.createdAt >= :startOfDay")
    BigDecimal sumTodayCompletedAmountByCurrency(
            @Param("currency") String currency,
            @Param("status") TransferStatus status,
            @Param("startOfDay") LocalDateTime startOfDay
    );

    Page<TransferRequest> findByUserId(UUID userId, Pageable pageable);


}
