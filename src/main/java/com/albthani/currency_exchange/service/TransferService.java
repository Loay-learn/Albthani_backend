package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.dto.request.TransferRequestDto;
import com.albthani.currency_exchange.model.dto.response.ExchangeRateResponse;
import com.albthani.currency_exchange.model.dto.response.TransferResponse;
import com.albthani.currency_exchange.model.entity.BankAccount;
import com.albthani.currency_exchange.model.entity.TransferRequest;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.Role;
import com.albthani.currency_exchange.model.enums.TransferStatus;
import com.albthani.currency_exchange.repository.BankAcountRepo;
import com.albthani.currency_exchange.repository.TransferRequestRepo;
import com.albthani.currency_exchange.response.exception.BusinessException;
import com.albthani.currency_exchange.response.exception.ResourceNotFoundException;
import com.albthani.currency_exchange.response.exception.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {
    private final TransferRequestRepo transferRepo;
    private final ExchangeRateService exchangeRateService;
    private final BankAcountRepo bankAcountRepo;
    private final FileUploadService fileUploadService;
    private final TelegramService telegramService;
    private final UserService userService;
    private final EmailService emailService;

    // ─── إنشاء طلب تحويل جديد ───
    public TransferResponse createTransfer(
            TransferRequestDto request,
            MultipartFile screenshot,
            User currentUser) {

        // 1. احسب قيمة التحويل
        ExchangeRateResponse rateResponse = exchangeRateService
                .calculate(request.getFromCurrency(),
                        request.getToCurrency(),
                        request.getAmount());

        // 2. تحقق من الحساب البنكي
        BankAccount bankAccount = bankAcountRepo
                .findById(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("الحساب البنكي غير موجود"));

        if (!bankAccount.getIsActive()) {
            throw new BusinessException("هذا الحساب غير متاح حالياً");
        }

        // 3. ارفع السكرين شوت
        String screenshotUrl = fileUploadService.uploadScreenshot(screenshot);

        // 4. احفظ الطلب
        TransferRequest transfer = TransferRequest.builder()
                .user(currentUser)
                .bankAccount(bankAccount)
                .fromCurrency(request.getFromCurrency())
                .toCurrency(request.getToCurrency())
                .amount(request.getAmount())
                .convertedAmount(rateResponse.getConvertedAmount())
                .rateUsed(rateResponse.getRate())
                .customerAccountName(request.getCustomerAccountName())
                .whatsappNumber(request.getWhatsappNumber())
                .customerBankName(request.getCustomerBankName())
                .customerAccountNumber(request.getCustomerAccountNumber())
                .screenshotUrl(screenshotUrl)
                .status(TransferStatus.PENDING)
                .build();

        transferRepo.save(transfer);

        // 5. أرسل إشعار تيليجرام
        telegramService.sendNewTransferNotification(transfer);

        return TransferResponse.builder()
                .id(transfer.getId())
                .referenceNumber("#" + transfer.getReferenceNumber())
                .fromCurrency(transfer.getFromCurrency())       // ✅ أضف
                .toCurrency(transfer.getToCurrency())           // ✅ أضف
                .amount(transfer.getAmount())                   // ✅ أضف
                .convertedAmount(transfer.getConvertedAmount())
                .customerBankName(request.getCustomerBankName())
                .customerAccountNumber(request.getCustomerAccountNumber()) // ✅
                .status(transfer.getStatus())
                .createdAt(transfer.getCreatedAt())             // ✅ أضف
                .build();
    }

    @Transactional
    public void reserveTransfer(UUID transferId, User adminUser) {
        TransferRequest transfer = transferRepo.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("الطلب غير موجود"));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BusinessException("لا يمكن حجز طلب منفز");
        }

        if (transfer.getReservedBy() != null) {
            String reserverName = transfer.getReservedBy().getFullName();
            throw new BusinessException("هذا الطلب محجوز حالياً من قِبَل " + reserverName);
        }

        transfer.setReservedBy(adminUser);
        transferRepo.save(transfer);
    }

    // ─── قبول أو رفض طلب (أدمن) ───
    @Transactional
    public void processTransfer(
            UUID transferId,
            TransferStatus status,
            String note,
            List<MultipartFile> receipts, // 👈 الحقل الجديد لاستقبال الصورة
            User adminUser) {

        TransferRequest transfer = transferRepo.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("الطلب غير موجود"));

        // 1. التحقق من حالة الطلب
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BusinessException("هذا الطلب تمت معالجته مسبقاً");
        }

        if (transfer.getReservedBy() != null &&
                !transfer.getReservedBy().getId().equals(adminUser.getId())) {
            throw new BusinessException("هذا الطلب محجوز من قِبَل " +
                    transfer.getReservedBy().getFullName());
        }

        // 2. معالجة الملف (إشعار التأكيد)
        if (receipts != null && !receipts.isEmpty()) {
            try {
                // رفع كل الصور لـ Cloudinary وجمع الروابط في قائمة
                List<String> imageUrls = receipts.stream()
                        .filter(file -> !file.isEmpty())
                        .map(fileUploadService::uploadConfirmationImage)
                        .toList();

                // 👈 التعديل هنا: إضافة الروابط الجديدة للقائمة المخزنة في الـ Entity (jsonb)
                if (transfer.getConfirmationImages() == null) {
                    transfer.setConfirmationImages(new ArrayList<>());
                }
                transfer.getConfirmationImages().addAll(imageUrls);
            } catch (Exception e) {
                throw new BusinessException("فشل حفظ إشعار التأكيد: " + e.getMessage());
            }
        }

        // 3. تحديث بيانات المعالجة
        transfer.setStatus(status);
        transfer.setAdminNote(note);
        transfer.setProcessedBy(adminUser);
        transfer.setProcessedAt(LocalDateTime.now());
        transfer.setReservedBy(null);

        transferRepo.save(transfer);

        // ─── أرسل إيميل للمستخدم عند القبول ───
        // داخل ميثود processTransfer في كلاس TransferService
        if (status == TransferStatus.COMPLETED) {
            try {
                String email = transfer.getUser().getEmail();

                // نمرر القائمة كاملة (حتى لو كانت فارغة سيمرر قائمة فارغة وليس null)
                List<String> allImages = transfer.getConfirmationImages() != null
                        ? transfer.getConfirmationImages()
                        : new ArrayList<>();

                // تحديث استدعاء الميثود لتقبل القائمة
                // الاستدعاء الجديد البسيط
                emailService.sendTransferCompleted(transfer.getUser().getEmail(), transfer);
            } catch (Exception e) {
                System.err.println("فشل إرسال إيميل الاكتمال: " + e.getMessage());
            }
        }

        // 4. أرسل إشعار للتيليجرام (يفضل أن يتضمن الإشعار رابط الصورة إذا وجدت)
        telegramService.sendStatusUpdateNotification(transfer);
    }

    // ─── طلبات المستخدم ───
    @Transactional(readOnly = true)
    public Page<TransferRequest> getUserTransfers(User user, Pageable pageable) {
        return transferRepo.findByUser(user, pageable);
    }

    // ─── كل الطلبات (أدمن) ───
    @Transactional(readOnly = true)
    public Page<TransferResponse> getAllTransfers(TransferStatus status, Pageable pageable) {
        Page<TransferRequest> transfers;

        if (status != null) {
            transfers = transferRepo.findByStatus(status, pageable);
        } else {
            transfers = transferRepo.findAll(pageable);
        }

        // تحويل كل صفحة من Entities إلى صفحة من DTOs
        return transfers.map(this::convertToDto);
    }

    private TransferResponse convertToDto(TransferRequest entity) {

        String name = "مستخدم غير معروف";
        try {
            // نتحقق من وجود الكائن وعدم كونه Proxy لمستخدم محذوف
            if (entity.getUser() != null) {
                name = entity.getUser().getFullName();
            }
        } catch (EntityNotFoundException e) {
            // في حال كان المستخدم محذوفاً من قاعدة البيانات
            name = "مستخدم محذوف";
        }

        return TransferResponse.builder()
                .id(entity.getId())
                .referenceNumber("#" + entity.getReferenceNumber())
                .customerFullName(name) // جلب الاسم من كائن المستخدم
                .customerBankName(entity.getCustomerBankName())
                .customerAccountNumber(entity.getCustomerAccountNumber())
                .whatsappNumber(entity.getWhatsappNumber())
                .convertedAmount(entity.getConvertedAmount())
                .screenshotUrl(entity.getScreenshotUrl())
                .fromCurrency(entity.getFromCurrency())
                .toCurrency(entity.getToCurrency())
                .confirmationImages(entity.getConfirmationImages())
                .customerAccountName(entity.getCustomerAccountName())
                .reservedBy(entity.getReservedBy())
                .processedBy(entity.getProcessedBy())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ─── إحصائيات للأدمن ───
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {

        Map<String, Object> stats = new HashMap<>();

        // عدد الطلبات حسب الحالة
        stats.put("totalPending",
                transferRepo.countByStatus(TransferStatus.PENDING));
        stats.put("totalCompleted",
                transferRepo.countByStatus(TransferStatus.COMPLETED));
        stats.put("totalRejected",
                transferRepo.countByStatus(TransferStatus.REJECTED));

        // إجمالي الطلبات
        stats.put("totalTransfers", transferRepo.count());

        // الحصول على بداية اليوم الحالي
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        // إجمالي المبالغ المحولة لكل عملة
        stats.put("totalAedCompleted",
                transferRepo.sumTodayCompletedAmountByCurrency("AED", TransferStatus.COMPLETED, startOfDay));
        stats.put("totalSarCompleted",
                transferRepo.sumTodayCompletedAmountByCurrency("SAR", TransferStatus.COMPLETED, startOfDay));
        stats.put("totalSdgCompleted",
                transferRepo.sumTodayCompletedAmountByCurrency("SDG", TransferStatus.COMPLETED, startOfDay));
        stats.put("totalEgpCompleted",
                transferRepo.sumTodayCompletedAmountByCurrency("EGP", TransferStatus.COMPLETED, startOfDay));

        // إجمالي المستخدمين النشطين
        stats.put("totalActiveUsers", userService.countActiveUsers());

        return stats;
    }

    // ─── جيب طلب معين ───
    @Transactional(readOnly = true)
    public TransferRequest getTransferById(UUID id, User currentUser) {

        TransferRequest transfer = transferRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الطلب غير موجود"));

        // تحقق إن الطلب يخص المستخدم الحالي
        // إلا إذا كان أدمن
        if (currentUser.getRole() != Role.ADMIN &&
                !transfer.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("ليس لديك صلاحية لعرض هذا الطلب");
        }

        return transfer;
    }

    public TransferResponse getTransferResponseById(UUID id, User currentUser) {
        // 1. جلب الـ Entity من قاعدة البيانات
        TransferRequest transfer = transferRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الطلب غير موجود"));

        // 2. التحقق من الصلاحية (اختياري: هل هذا الطلب يخص المستخدم الحالي أو هو أدمن؟)

        // 3. بناء الـ DTO يدوياً (أو باستخدام MapStruct)
        return TransferResponse.builder()
                .id(transfer.getId())
                .referenceNumber("#" + transfer.getReferenceNumber())
                .whatsappNumber(transfer.getWhatsappNumber())

                // --- البيانات التي طلبتها ---
                .customerFullName(transfer.getUser().getFullName()) // اسم العميل من علاقة الـ User
                .customerBankName(transfer.getCustomerBankName())   // بنك العميل (من الطلب)
                .customerAccountName(transfer.getCustomerAccountName())
                .customerAccountNumber(transfer.getCustomerAccountNumber()) // حساب العميل (من الطلب)
                // --------------------------

                .fromCurrency(transfer.getFromCurrency())
                .toCurrency(transfer.getToCurrency())
                .amount(transfer.getAmount())
                .status(transfer.getStatus())
                .createdAt(transfer.getCreatedAt())
                .build();
    }

    // ─── طلبات مستخدم معين (أدمن) ───
    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersByUserId(UUID userId, Pageable pageable) {
        return transferRepo.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

}
