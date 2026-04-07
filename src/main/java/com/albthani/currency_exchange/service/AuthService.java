package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.dto.request.LoginRequest;
import com.albthani.currency_exchange.model.dto.request.RegisterRequest;
import com.albthani.currency_exchange.model.dto.response.AuthResponse;
import com.albthani.currency_exchange.model.entity.PendingRegistration;
import com.albthani.currency_exchange.model.entity.RefreshToken;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.Role;
import com.albthani.currency_exchange.repository.PendingRegistrationRepository;
import com.albthani.currency_exchange.repository.UserRepo;
import com.albthani.currency_exchange.response.exception.BusinessException;
import com.albthani.currency_exchange.response.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PendingRegistrationRepository pendingRepo;
    private final EmailService emailService;

    public String register(RegisterRequest request){
        if(userRepo.existsByEmail(request.getEmail())) {
            throw new BusinessException("البريد الالكتروني مستخدم مسبقا");
        }

        // احذف أي طلب قديم لنفس الإيميل
        pendingRepo.deleteByEmail(request.getEmail());

        // ولّد OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // احفظ في pending_registrations
        PendingRegistration pending = PendingRegistration.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .otpCode(otp)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .build();

        pendingRepo.save(pending);

        // أرسل OTP على الإيميل
        emailService.sendOtp(request.getEmail(), otp);

        return "تم التسجيل، تحقق من بريدك الإلكتروني";
    }


    // ─── التحقق من الإيميل ───
    public AuthResponse verifyEmail(
            String email, String code, HttpServletResponse response) {

        // جيب الطلب المعلق
        PendingRegistration pending = pendingRepo
                .findByEmailAndOtpCodeAndIsUsedFalse(email, code)
                .orElseThrow(() -> new BusinessException("الكود غير صحيح"));

        // تحقق من الصلاحية
        if (pending.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            pendingRepo.delete(pending);
            throw new BusinessException("الكود منتهي الصلاحية، سجّل من جديد");
        }

        // انقل البيانات لـ users
        User user = User.builder()
                .fullName(pending.getFullName())
                .email(pending.getEmail())
                .password(pending.getPassword())
                .role(Role.USER)
                .isActive(true)
                .build();

        userRepo.save(user);

        // احذف من pending
        pendingRepo.delete(pending);

        // أرجع التوكن
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        addAccessTokenCookie(response, accessToken);
        addRefreshTokenCookie(response, refreshToken.getToken());

        return AuthResponse.builder()
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    // ─── إعادة إرسال OTP ───
    public String resendOtp(String email) {

        PendingRegistration pending = pendingRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "لا يوجد طلب تسجيل لهذا البريد"));

        // ولّد كود جديد
        String otp = String.format("%06d", new Random().nextInt(999999));
        pending.setOtpCode(otp);
        pending.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        pending.setIsUsed(false);
        pendingRepo.save(pending);

        emailService.sendOtp(email, otp);

        return "تم إرسال كود جديد";
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {

        // جيب المستخدم
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("البريد أو كلمة المرور غلط"));

        // تحقق إن الحساب مفعل
        if (!user.getIsActive()) {
            throw new BusinessException("حسابك محظور، تواصل مع الدعم");
        }

        // تحقق من كلمة المرور
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("البريد أو كلمة المرور غلط");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // ✅ احفظ في Cookie
        addAccessTokenCookie(response, accessToken);
        addRefreshTokenCookie(response, refreshToken.getToken());

        return AuthResponse.builder()
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    public void createAdmin(RegisterRequest request) {
        // 1. التحقق من التكرار
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new BusinessException("البريد الإلكتروني مستخدم مسبقاً");
        }

        // 2. بناء كائن الأدمن وحفظه
        User admin = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        userRepo.save(admin);

        // 3. لاحظ: حذفنا أسطر jwtService و refreshTokenService و addCookie
        // العملية الآن تنتهي هنا بحفظ البيانات فقط.
    }
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        // 1. امسح الـ Access Token (مساره دائمًا "/")
        clearCookie(response, "access_token", "/");

        // 2. امسح الـ Refresh Token (يجب أن يتطابق المسار مع "/api/auth/refresh-token")
        clearCookie(response, "refresh_token", "/api/auth/refresh-token");

        // 3. إلغاء الـ Token من قاعدة البيانات
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(c -> "refresh_token".equals(c.getName()))
                    .findFirst()
                    .ifPresent(c -> refreshTokenService.revokeToken(c.getValue()));
        }
    }

    // تعديل الـ Helper Method لتقبل المسار (Path)
    private void clearCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // تأكد أن هذا يطابق بيئة العمل (HTTPS)
        cookie.setPath(path);   // 👈 الآن المسار ديناميكي ومطابق للإنشاء
        cookie.setMaxAge(0);    // حذف فوري
        response.addCookie(cookie);
    }


    // ─── Helper Methods ───
    private void addAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);   // ✅ JavaScript لا يستطيع قراءته
        cookie.setSecure(true);     // ✅ HTTPS فقط
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 24 ساعة
        response.addCookie(cookie);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth/refresh-token");
        cookie.setMaxAge(60 * 60 * 24 * 360); // 360 يوم
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // ✅ يحذف الـ Cookie فوراً
        response.addCookie(cookie);
    }
}
