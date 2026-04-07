package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.dto.request.ExchangeRateRequest;
import com.albthani.currency_exchange.model.dto.response.ExchangeRateResponse;
import com.albthani.currency_exchange.model.entity.ExchangeRate;
import com.albthani.currency_exchange.repository.ExchangeRateRepo;
import com.albthani.currency_exchange.response.GlobalExceptionHandler;
import com.albthani.currency_exchange.response.exception.BusinessException;
import com.albthani.currency_exchange.response.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepo exchangeRateRepo;

    // ─── حساب قيمة التحويل ───
    public ExchangeRateResponse calculate(String from, String to, BigDecimal amount) {

        ExchangeRate rate = exchangeRateRepo
                .findApplicableRate(from, to, amount)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "لا يوجد سعر صرف من " + from + " إلى " + to
                ));

        BigDecimal convertedAmount = amount.multiply(rate.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        return ExchangeRateResponse.builder()
                .fromCurrency(from)
                .toCurrency(to)
                .amount(amount)
                .rate(rate.getRate())
                .convertedAmount(convertedAmount)
                .build();
    }

    // ─── كل الأسعار النشطة ───
    public List<ExchangeRate> getAllActive() {
        return exchangeRateRepo.findByIsActiveTrue();
    }

    // ─── إضافة سعر جديد (أدمن) ───
    public ExchangeRate addRate(ExchangeRateRequest request) {

        // 1. التحقق من تداخل النطاقات لنفس العملات
        boolean isOverlapping = exchangeRateRepo.existsOverlappingRate(
                request.getFromCurrency(),
                request.getToCurrency(),
                request.getMinAmount(),
                request.getMaxAmount()
        );

        if (isOverlapping) {
            throw new BusinessException("يوجد تداخل في نطاق المبالغ لهذه التحويلة مع سجل مسبق.");
            // نصيحة: يفضل استخدام Custom Exception مثل RangeOverlapException
        }

        // 2. إذا كان النطاق متاحاً، يتم الحفظ
        ExchangeRate rate = ExchangeRate.builder()
                .fromCurrency(request.getFromCurrency())
                .toCurrency(request.getToCurrency())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .rate(request.getRate())
                .isActive(true)
                .build();

        return exchangeRateRepo.save(rate);
    }

    // ─── تعديل سعر (أدمن) ───
    public ExchangeRate updateRate(UUID id, ExchangeRateRequest request) {
        ExchangeRate rate = exchangeRateRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("السعر غير موجود"));

        rate.setRate(request.getRate());
        rate.setMinAmount(request.getMinAmount());
        rate.setMaxAmount(request.getMaxAmount());

        return exchangeRateRepo.save(rate);
    }

    public ExchangeRate updateOnlyRate(UUID id, BigDecimal newRate) {
        ExchangeRate rate = exchangeRateRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("سعر الصرف غير موجود"));

        rate.setRate(newRate);
        // يمكنك إضافة منطق إضافي هنا مثل تسجيل من قام بالتعديل أو وقت التعديل
        return exchangeRateRepo.save(rate);
    }

    // ─── تفعيل/تعطيل سعر (أدمن) ───
    public void toggleRate(UUID id) {
        ExchangeRate rate = exchangeRateRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("السعر غير موجود"));

        rate.setIsActive(!rate.getIsActive());
        exchangeRateRepo.save(rate);
    }

    // ─── جلب كل الأسعار (الأدمن - جميع الحالات) ───
    public List<ExchangeRate> getAllRates() {
        return exchangeRateRepo.findAll();
    }


}
