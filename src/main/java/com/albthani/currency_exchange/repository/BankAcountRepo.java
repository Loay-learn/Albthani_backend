package com.albthani.currency_exchange.repository;

import com.albthani.currency_exchange.model.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BankAcountRepo extends JpaRepository<BankAccount, UUID> {
    // الحسابات المتاحة حسب العملة — يظهر للمستخدم
    List<BankAccount> findByCurrencyAndIsActiveTrue(String currency);
}
