package com.albthani.currency_exchange.controller;

import com.albthani.currency_exchange.model.dto.request.LoginRequest;
import com.albthani.currency_exchange.model.dto.request.RegisterRequest;
import com.albthani.currency_exchange.model.dto.request.UpdateProfileRequest;
import com.albthani.currency_exchange.model.dto.response.AuthResponse;
import com.albthani.currency_exchange.model.dto.response.UserResponse;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.response.GlobalResponse;
import com.albthani.currency_exchange.response.exception.BusinessException;
import com.albthani.currency_exchange.response.exception.UnauthorizedException;
import com.albthani.currency_exchange.service.AuthService;
import com.albthani.currency_exchange.service.RefreshTokenService;
import com.albthani.currency_exchange.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    // ─── تسجيل مستخدم جديد ───
    @PostMapping("/register")
    public ResponseEntity<GlobalResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);
        return ResponseEntity.status(201).body(new GlobalResponse<>(message));
    }

    // ─── التحقق من الإيميل ───
    @PostMapping("/verify-email")
    public ResponseEntity<GlobalResponse<AuthResponse>> verifyEmail(
            @RequestParam String email,
            @RequestParam String code,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.verifyEmail(email, code, response);
        return ResponseEntity.ok(new GlobalResponse<>(authResponse));
    }

    // ─── إعادة إرسال OTP ───
    @PostMapping("/resend-otp")
    public ResponseEntity<GlobalResponse<String>> resendOtp(
            @RequestParam String email) {

        String message = authService.resendOtp(email);
        return ResponseEntity.ok(new GlobalResponse<>(message));
    }

    // ─── تسجيل الدخول ───
    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(new GlobalResponse<>(authResponse));
    }

    // ─── تجديد الـ Access Token ───
    @PostMapping("/refresh-token")
    public ResponseEntity<GlobalResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. تحقق من وجود كوكيز في الطلب لتجنب NullPointerException
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new BusinessException("لم يتم العثور على أي جلسة (Cookies)، يرجى تسجيل الدخول");
        }

        // 2. البحث عن الكوكي المطلوب بأمان
        String token = Arrays.stream(cookies)
                .filter(c -> "refresh_token".equals(c.getName()))
                .findFirst()
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElseThrow(() -> new BusinessException("جلسة التجديد منتهية أو غير موجودة"));

        AuthResponse authResponse = refreshTokenService.refreshAccessToken(token, response);
        return ResponseEntity.ok(new GlobalResponse<>(authResponse));
    }

    // ─── جيب بيانات المستخدم الحالي ───
    @GetMapping("/me")
    public ResponseEntity<GlobalResponse<UserResponse>> me(
            @AuthenticationPrincipal User user) {
        if (user == null)
            return ResponseEntity.status(401).body(
                    new GlobalResponse<>(List.of(new GlobalResponse.ErrorItem("غير مصرح")))
            );
        return ResponseEntity.ok(new GlobalResponse<>(
                UserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .profilePicture(user.getProfilePicture())
                        .role(user.getRole())
                        .build()
        ));
    }

    // ─── تحديث الملف الشخصي ───
    @PutMapping("/me")
    public ResponseEntity<GlobalResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {

        User updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(new GlobalResponse<>(
                UserResponse.builder()
                        .fullName(updated.getFullName())
                        .phone(updated.getPhone())
                        .profilePicture(updated.getProfilePicture())
                        .role(updated.getRole())
                        .build()
        ));
    }

    // ─── رفع صورة الملف الشخصي ───
    @PatchMapping(value = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse<String>> updatePicture(
            @RequestPart("picture") MultipartFile file,
            @AuthenticationPrincipal User user) {

        User updated = userService.updateProfilePicture(user.getId(), file);
        return ResponseEntity.ok(new GlobalResponse<>(updated.getProfilePicture()));
    }

    // ─── تسجيل الخروج ───
    @PostMapping("/logout")
    public ResponseEntity<GlobalResponse<String>> logout(
            HttpServletRequest request,
            HttpServletResponse response){

            authService.logout(request, response);
            return ResponseEntity.ok(new GlobalResponse<>("تم تسجيل الخروج بنجاح"));

    }
}

