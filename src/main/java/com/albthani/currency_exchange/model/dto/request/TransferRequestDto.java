package com.albthani.currency_exchange.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequestDto {
    @NotBlank(message = "عملة المصدر مطلوبة")
    private String fromCurrency;

    @NotBlank(message = "عملة الهدف مطلوبة")
    private String toCurrency;

    @NotNull(message = "المبلغ مطلوب")
    @DecimalMin(value = "0.01", message = "المبلغ يجب أن يكون أكبر من صفر")
    private BigDecimal amount;

    @NotNull(message = "الحساب البنكي مطلوب")
    private UUID bankAccountId;

    @NotBlank(message = "اسم البنك مطلوب")
    private String customerBankName;

    @NotBlank(message = "اسم الحساب مطلوب")
    private String customerAccountName;

    @NotBlank(message = "رقم حساب المستخدم مطلوب")
    private String customerAccountNumber; // ✅

    @NotBlank(message = "رقم الواتساب مطلوب")
    private String whatsappNumber;

    @NotBlank(message = "الاشعار مطلوب")
    private String screenshotUrl;

}
