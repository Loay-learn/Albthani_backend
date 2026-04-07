package com.albthani.currency_exchange.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateResponse {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal convertedAmount;
}
