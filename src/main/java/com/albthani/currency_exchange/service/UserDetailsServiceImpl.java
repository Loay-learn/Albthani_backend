package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("المستخدم غير موجود"));

        if (!user.getIsActive()) {
            throw new DisabledException("الحساب محظور");
        }

        return user;
    }
}