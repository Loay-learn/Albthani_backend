package com.albthani.currency_exchange.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class RegisterRequest {
    @NotBlank(message = "الاسم مطلوب")
    private String fullName;

    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "البريد الإلكتروني غير صحيح")
    private String email;

    private String phone;

    @NotBlank(message = "كلمة المرور مطلوبة")
    @Size(min = 8, message = "كلمة المرور يجب أن تكون 8 أحرف على الأقل")
    private String password;
}
