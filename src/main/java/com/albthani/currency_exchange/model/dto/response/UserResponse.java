package com.albthani.currency_exchange.model.dto.response;

import com.albthani.currency_exchange.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private Boolean isActive;
    private String profilePicture;
    private LocalDateTime createdAt;
    private Role role;
}
