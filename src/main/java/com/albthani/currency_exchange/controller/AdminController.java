package com.albthani.currency_exchange.controller;

import com.albthani.currency_exchange.model.dto.request.RegisterRequest;
import com.albthani.currency_exchange.model.dto.response.AuthResponse;
import com.albthani.currency_exchange.model.dto.response.TransferResponse;
import com.albthani.currency_exchange.model.dto.response.UserResponse;
import com.albthani.currency_exchange.model.entity.TransferRequest;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.TransferStatus;
import com.albthani.currency_exchange.response.GlobalResponse;
import com.albthani.currency_exchange.service.AuthService;
import com.albthani.currency_exchange.service.TransferService;
import com.albthani.currency_exchange.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TransferService transferService;
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')") // حماية أمنية
    public ResponseEntity<GlobalResponse<List<UserResponse>>> getAdmins() {
        return ResponseEntity.ok(
                new GlobalResponse<>(userService.getAllAdmins())
        );
    }

    @GetMapping("/transfers")
    public ResponseEntity<GlobalResponse<Page<TransferResponse>>> getAllTransfers(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // الخدمة الآن تعيد Page<TransferResponseDto>
        return ResponseEntity.ok(
                new GlobalResponse<>(transferService.getAllTransfers(status, pageable))
        );
    }

    // ─── حجز طلب لتنفيذه (منع التعارض بين الأدمنين) ───
    @PatchMapping("/transfers/{id}/reserve")
    public ResponseEntity<GlobalResponse<String>> reserveTransfer(
            @PathVariable UUID id,
            @AuthenticationPrincipal User adminUser) {

        transferService.reserveTransfer(id, adminUser);
        return ResponseEntity.ok(new GlobalResponse<>("تم حجز الطلب بنجاح"));
    }

    // ─── قبول أو رفض طلب مع إضافة إشعار تأكيد ───
    @PatchMapping(value = "/transfers/{id}/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse<String>> processTransfer(
            @PathVariable UUID id,
            @RequestParam TransferStatus status,
            @RequestParam(required = false) String note,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt, // 👈 الحقل الجديد
            @AuthenticationPrincipal User adminUser) {

        transferService.processTransfer(id, status, note, receipt, adminUser);
        return ResponseEntity.ok(new GlobalResponse<>("تم تحديث حالة الطلب وإضافة التأكيد"));
    }

    // ─── كل المستخدمين ───
    @GetMapping("/users")
    public ResponseEntity<GlobalResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                new GlobalResponse<>(userService.getAllUsers(pageable))
        );
    }

    @GetMapping("/users/{id}/transfers")
    public ResponseEntity<GlobalResponse<Page<TransferResponse>>> getUserTransfers(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                new GlobalResponse<>(transferService.getTransfersByUserId(id, pageable))
        );
    }

    // ─── حظر/تفعيل مستخدم ───
    @PatchMapping("/users/{id}/toggle")
    public ResponseEntity<GlobalResponse<String>> toggleUser(
            @PathVariable UUID id) {

        userService.toggleUser(id);
        return ResponseEntity.ok(new GlobalResponse<>("تم تحديث حالة المستخدم"));
    }

    // ─── إنشاء أدمن جديد ───
    @PostMapping("/create-admin")
    public ResponseEntity<GlobalResponse<String>> createAdmin(
            @Valid @RequestBody RegisterRequest request) {

        authService.createAdmin(request);
        return ResponseEntity.status(201).body(new GlobalResponse<>("تم إنشاء حساب الأدمن بنجاح، يمكنك الآن تسجيل الدخول"));
    }

    // ─── إحصائيات ───
    @GetMapping("/stats")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = transferService.getStats();
        return ResponseEntity.ok(new GlobalResponse<>(stats));
    }
}
