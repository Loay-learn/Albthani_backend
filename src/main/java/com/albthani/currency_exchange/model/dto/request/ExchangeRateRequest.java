package com.albthani.currency_exchange.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateRequest {

    @NotBlank(message = "عملة المصدر مطلوبة")
    private String fromCurrency;

    @NotBlank(message = "عملة الهدف مطلوبة")
    private String toCurrency;

    @NotNull(message = "الحد الأدنى مطلوب")
    @DecimalMin(value = "0", message = "الحد الأدنى يجب أن يكون صفر أو أكثر")
    private BigDecimal minAmount;

    // اختياري — NULL يعني بلا حد أقصى
    private BigDecimal maxAmount;

    @NotNull(message = "السعر مطلوب")
    @DecimalMin(value = "0.0001", message = "السعر يجب أن يكون أكبر من صفر")
    private BigDecimal rate;
}
