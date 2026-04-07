package com.albthani.currency_exchange.model.dto.response;

import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private UUID id;
    private String whatsappNumber;
    private String customerFullName;
    private String referenceNumber;
    private String fromCurrency;
    private String screenshotUrl;
    private String confirmationImage;
    private String toCurrency;
    private BigDecimal amount;
    private String customerBankName;
    private String customerAccountName;
    private String customerAccountNumber;
    private BigDecimal convertedAmount;
    private TransferStatus status;
    private String reservedStatus;
    private User reservedBy;
    private User processedBy;
    private LocalDateTime createdAt;
}