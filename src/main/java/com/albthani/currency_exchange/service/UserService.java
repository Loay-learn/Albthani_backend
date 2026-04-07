package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.dto.request.UpdateProfileRequest;
import com.albthani.currency_exchange.model.dto.response.UserResponse;
import com.albthani.currency_exchange.model.entity.User;
import com.albthani.currency_exchange.model.enums.Role;
import com.albthani.currency_exchange.repository.UserRepo;
import com.albthani.currency_exchange.response.exception.BusinessException;
import com.albthani.currency_exchange.response.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepo userRepo;
    private final FileUploadService fileUploadService;

    public List<UserResponse> getAllAdmins() {
        // 1. جلب المستخدمين من القاعدة
        List<User> admins = userRepo.findByRole(Role.ADMIN);

        // 2. التحويل إلى DTO (Mapping)
        return admins.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .isActive(user.getIsActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }

    // ─── كل المستخدمين (Admin) ───
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepo.findByRole(Role.USER, pageable);
    }

    // ─── البحث عن مستخدم (Admin) ───
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String name, Pageable pageable) {
        return userRepo.findByFullNameContainingIgnoreCase(name, pageable);
    }

    // ─── جيب مستخدم بالـ ID ───
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));
    }

    // ─── حظر/تفعيل مستخدم (Admin) ───
    public void toggleUser(UUID id) {
        User user = getUserById(id);

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("لا يمكن حظر حساب أدمن");
        }

        user.setIsActive(!user.getIsActive());
        userRepo.save(user);
    }

    // ─── عدد المستخدمين النشطين ───
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepo.findByIsActive(true, Pageable.unpaged()).getTotalElements();
    }

    // ─── تحديث الملف الشخصي ───
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        return userRepo.save(user);
    }

    // ─── رفع صورة الملف الشخصي ───
    public User updateProfilePicture(UUID userId, MultipartFile file) {
        User user = getUserById(userId);
        String url = fileUploadService.uploadProfilePicture(file); // نفس الـ Service
        user.setProfilePicture(url);
        return userRepo.save(user);
    }
}
