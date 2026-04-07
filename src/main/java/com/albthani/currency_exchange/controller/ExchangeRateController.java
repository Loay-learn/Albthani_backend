package com.albthani.currency_exchange.controller;

import com.albthani.currency_exchange.model.dto.request.ExchangeRateRequest;
import com.albthani.currency_exchange.model.dto.response.ExchangeRateResponse;
import com.albthani.currency_exchange.model.entity.ExchangeRate;
import com.albthani.currency_exchange.response.GlobalResponse;
import com.albthani.currency_exchange.service.ExchangeRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    // ─── حساب قيمة التحويل (Public) ───
    @GetMapping("/calculate")
    public ResponseEntity<GlobalResponse<ExchangeRateResponse>> calculate(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        ExchangeRateResponse response = exchangeRateService.calculate(from, to, amount);
        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    // ─── كل الأسعار النشطة (Public) ───
    @GetMapping
    public ResponseEntity<GlobalResponse<List<ExchangeRate>>> getAllActive() {
        return ResponseEntity.ok(
                new GlobalResponse<>(exchangeRateService.getAllActive())
        );
    }

    // ─── إضافة سعر (Admin) ───
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<ExchangeRate>> addRate(
            @Valid @RequestBody ExchangeRateRequest request) {

        ExchangeRate rate = exchangeRateService.addRate(request);
        return ResponseEntity.status(201).body(new GlobalResponse<>(rate));
    }

    // ─── تعديل سعر (Admin) ───
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<ExchangeRate>> updateRate(
            @PathVariable UUID id,
            @Valid @RequestBody ExchangeRateRequest request) {

        ExchangeRate rate = exchangeRateService.updateRate(id, request);
        return ResponseEntity.ok(new GlobalResponse<>(rate));
    }

    @PatchMapping("/{id}/rate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<ExchangeRate>> updateOnlyRate(
            @PathVariable UUID id,
            @RequestParam BigDecimal newRate) { // نستخدم RequestParam لإرسال القيمة في الرابط أو Body بسيط

        ExchangeRate updatedRate = exchangeRateService.updateOnlyRate(id, newRate);
        return ResponseEntity.ok(new GlobalResponse<>(updatedRate));
    }

    // ─── تفعيل/تعطيل سعر (Admin) ───
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<String>> toggleRate(
            @PathVariable UUID id) {

        exchangeRateService.toggleRate(id);
        return ResponseEntity.ok(new GlobalResponse<>("تم تحديث حالة السعر"));
    }

    // ─── جلب كل الأسعار للوحة التحكم (Admin) ───
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<List<ExchangeRate>>> getAllRates() {
        return ResponseEntity.ok(
                new GlobalResponse<>(exchangeRateService.getAllRates())
        );
    }
}
