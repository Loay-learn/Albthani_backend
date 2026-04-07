package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.dto.response.AuthResponse;
import com.albthani.currency_exchange.model.entity.RefreshToken;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.repository.RefreshTokenRepo;
import com.albthani.currency_exchange.repository.UserRepo;
import com.albthani.currency_exchange.response.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepository;
    private final UserRepo userRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    // ─── إنشاء Refresh Token ───
    public RefreshToken createRefreshToken(User user) {

        // احذف التوكنات القديمة للمستخدم
        refreshTokenRepository.deleteByUser(user);

        String token = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // ─── تجديد الـ Access Token ───
    public AuthResponse refreshAccessToken(String token, HttpServletResponse response) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh Token غير صالح"));

        // تحقق إن التوكن ما انتهى أو ما انلغى
        if (refreshToken.getIsRevoked()) {
            throw new UnauthorizedException("Refresh Token ملغى");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh Token منتهي الصلاحية، سجل دخول من جديد");
        }

        // ولّد Access Token جديد
        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        // ✅ احفظ الـ Access Token الجديد في Cookie
        addAccessTokenCookie(response, newAccessToken);


        return AuthResponse.builder()
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    // ─── إلغاء التوكن عند تسجيل الخروج ───
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("التوكن غير موجود"));

        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    // ─── Helper ───
    private void addAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 24 ساعة
        response.addCookie(cookie);
    }
}
