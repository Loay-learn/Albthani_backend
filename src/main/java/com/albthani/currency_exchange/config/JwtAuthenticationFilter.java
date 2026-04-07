package com.albthani.currency_exchange.config;

import com.albthani.currency_exchange.service.JwtService;
import com.albthani.currency_exchange.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void  doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // جيب الـ Authorization header
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("access_token"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // لو ما فيه توكن، كمّل بدون مصادقة
        if (token == null   ) {
            filterChain.doFilter(request, response);
            return;
        }


        try {
            // تحقق إن التوكن Access وليس Refresh
            if (!jwtService.isAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (Exception e) { }

        filterChain.doFilter(request, response);
    }
}
