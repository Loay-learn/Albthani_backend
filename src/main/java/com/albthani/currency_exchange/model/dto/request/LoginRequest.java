package com.albthani.currency_exchange.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "البريد الإلكتروني غير صحيح")
    private String email;

    @NotBlank(message = "كلمة المرور مطلوبة")
    private String password;
}
