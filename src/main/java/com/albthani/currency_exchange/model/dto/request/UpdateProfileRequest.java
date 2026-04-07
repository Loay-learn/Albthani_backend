package com.albthani.currency_exchange.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "الاسم مطلوب")
    private String fullName;

    private String phone;
}
