package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.dto.request.BankAccountRequest;
import com.albthani.currency_exchange.model.entity.BankAccount;
import com.albthani.currency_exchange.repository.BankAcountRepo;
import com.albthani.currency_exchange.response.exception.BusinessException;
import com.albthani.currency_exchange.response.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAcountRepo bankAcountRepo;

    // ─── الحسابات المتاحة حسب العملة ───
    public List<BankAccount> getActiveByCurrency(String currency) {
        return bankAcountRepo.findByCurrencyAndIsActiveTrue(currency);
    }

    public List<BankAccount> getAllAccounts() {
        // نستخدم findAll() لجلب كل شيء (مفعل وغير مفعل)
        return bankAcountRepo.findAll();
    }

    // ─── إضافة حساب (أدمن) ───
    public BankAccount addAccount(BankAccountRequest request) {

        // تحقق إن الحساب عنده accountNumber أو IBAN حسب النوع
        if (request.getAccountNumber() == null && request.getIban() == null) {
            throw new BusinessException("رقم الحساب مطلوب للتحويل");
        }

        BankAccount account = BankAccount.builder()
                .bankName(request.getBankName())
                .accountName(request.getAccountName())
                .accountNumber(request.getAccountNumber())
                .iban(request.getIban())
                .currency(request.getCurrency())
                .isActive(true)
                .build();

        return bankAcountRepo.save(account);
    }

    // ─── تفعيل/تعطيل حساب (أدمن) ───
    public void toggleAccount(UUID id) {
        BankAccount account = bankAcountRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الحساب غير موجود"));

        account.setIsActive(!account.getIsActive());
        bankAcountRepo.save(account);
    }
}
