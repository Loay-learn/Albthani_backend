package com.albthani.currency_exchange.controller;

import com.albthani.currency_exchange.model.dto.request.BankAccountRequest;
import com.albthani.currency_exchange.model.entity.BankAccount;
import com.albthani.currency_exchange.response.GlobalResponse;
import com.albthani.currency_exchange.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    // ─── الحسابات المتاحة حسب العملة (User) ───
    @GetMapping
    public ResponseEntity<GlobalResponse<List<BankAccount>>> getActiveByCurrency(
            @RequestParam String currency) {

        return ResponseEntity.ok(
                new GlobalResponse<>(bankAccountService.getActiveByCurrency(currency))
        );
    }

    // ─── جلب كل الحسابات بدون استثناء (Admin) ───
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<List<BankAccount>>> getAllAccounts() {
        return ResponseEntity.ok(
                new GlobalResponse<>(bankAccountService.getAllAccounts())
        );
    }

    // ─── إضافة حساب (Admin) ───
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<BankAccount>> addAccount(
            @Valid @RequestBody BankAccountRequest request) {

        BankAccount account = bankAccountService.addAccount(request);
        return ResponseEntity.status(201).body(new GlobalResponse<>(account));
    }

    // ─── تفعيل/تعطيل حساب (Admin) ───
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<String>> toggleAccount(
            @PathVariable UUID id) {

        bankAccountService.toggleAccount(id);
        return ResponseEntity.ok(new GlobalResponse<>("تم تحديث حالة الحساب"));
    }
}
