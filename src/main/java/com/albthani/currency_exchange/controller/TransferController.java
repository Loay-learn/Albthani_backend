package com.albthani.currency_exchange.controller;

import com.albthani.currency_exchange.model.dto.request.TransferRequestDto;
import com.albthani.currency_exchange.model.dto.response.TransferResponse;
import com.albthani.currency_exchange.model.entity.TransferRequest;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.response.GlobalResponse;
import com.albthani.currency_exchange.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    // ─── إنشاء طلب تحويل جديد ───
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse<TransferResponse>> createTransfer(
            @RequestParam("fromCurrency") String fromCurrency,
            @RequestParam("toCurrency") String toCurrency,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("bankAccountId") UUID bankAccountId,
            @RequestParam("customerBankName") String customerBankName,
            @RequestParam("customerAccountName") String customerAccountName,
            @RequestParam("customerAccountNumber") String customerAccountNumber,
            @RequestParam("whatsappNumber") String whatsappNumber,
            @RequestPart("screenshot") MultipartFile screenshot,
            @AuthenticationPrincipal User currentUser) {

        TransferRequestDto request = new TransferRequestDto();
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(amount);
        request.setBankAccountId(bankAccountId);
        request.setCustomerBankName(customerBankName);
        request.setCustomerAccountName(customerAccountName);
        request.setCustomerAccountNumber(customerAccountNumber);
        request.setWhatsappNumber(whatsappNumber);

        TransferResponse response = transferService
                .createTransfer(request, screenshot, currentUser);

        return ResponseEntity.status(201).body(new GlobalResponse<>(response));
    }

    // ─── طلبات المستخدم الحالي ───
    @GetMapping("/my-transfers")
    public ResponseEntity<GlobalResponse<Page<TransferRequest>>> getMyTransfers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                new GlobalResponse<>(
                        transferService.getUserTransfers(currentUser, pageable)
                )
        );
    }

    // ─── تفاصيل طلب معين ───
    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<TransferResponse>> getTransfer(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        TransferResponse response = transferService.getTransferResponseById(id, currentUser);
        return ResponseEntity.ok(new GlobalResponse<>(response));
    }
}
