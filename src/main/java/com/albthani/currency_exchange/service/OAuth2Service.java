package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.dto.response.AuthResponse;
import com.albthani.currency_exchange.model.entity.RefreshToken;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.Role;
import com.albthani.currency_exchange.repository.UserRepo;
import com.albthani.currency_exchange.response.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse processOAuth2Login(
            OAuth2User oauth2User,
            HttpServletResponse response) {

        // جيب البيانات من Google
        String email    = oauth2User.getAttribute("email");
        String fullName = oauth2User.getAttribute("name");

        // جيب المستخدم أو أنشئه تلقائياً
        User user = userRepo.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .fullName(fullName)
                            .email(email)
                            .password("GOOGLE_AUTH") // ← بدون كلمة مرور
                            .role(Role.USER)
                            .isActive(true)
                            .build();
                    return userRepo.save(newUser);
                });

        if (!user.getIsActive()) {
            throw new BusinessException("حسابك محظور، تواصل مع الدعم");
        }

        // ولّد التوكنات
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // احفظهم في Cookies
        addAccessTokenCookie(response, accessToken);
        addRefreshTokenCookie(response, refreshToken.getToken());

        return AuthResponse.builder()
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    private void addAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth/refresh-token");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);
    }
}
