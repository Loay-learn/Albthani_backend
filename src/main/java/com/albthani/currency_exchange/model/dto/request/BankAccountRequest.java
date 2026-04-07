package com.albthani.currency_exchange.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class BankAccountRequest {

    @NotBlank(message = "اسم البنك مطلوب")
    private String bankName;

    @NotBlank(message = "اسم صاحب الحساب مطلوب")
    private String accountName;

    // اختياري — حسب النوع
    private String accountNumber;

    // اختياري — حسب النوع
    private String iban;

    @NotBlank(message = "العملة مطلوبة")
    private String currency;
}
