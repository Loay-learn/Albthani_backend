package com.albthani.currency_exchange.config;

import com.albthani.currency_exchange.service.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final OAuth2Service oauth2Service;
    @Value("${frontend.url}")
    private String frontendUrl;


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();


        // عالج تسجيل الدخول
        oauth2Service.processOAuth2Login(oauth2User, response);

        // وجّه للـ Frontend بعد النجاح
        response.sendRedirect(frontendUrl + "/");
    }
}
