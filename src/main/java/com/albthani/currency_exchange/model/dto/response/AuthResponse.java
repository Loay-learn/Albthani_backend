package com.albthani.currency_exchange.model.dto.response;

import com.albthani.currency_exchange.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String fullName;
    private Role role;
}