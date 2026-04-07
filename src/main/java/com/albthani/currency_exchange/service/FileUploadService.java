package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.response.exception.BusinessException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    // لقطات الشاشة (التحويلات) - حد 15MB
    public String uploadScreenshot(MultipartFile file) {
        return upload(file, "albathani/screenshots", 15);
    }

    // صورة التأكيد (الأدمن) - حد 5MB
    public String uploadConfirmationImage(MultipartFile file) {
        return upload(file, "albathani/confirmations", 5);
    }

    // صور البروفايل - حد 15MB
    public String uploadProfilePicture(MultipartFile file) {
        return upload(file, "albathani/profiles", 15);
    }

    // المنطق المشترك
    private String upload(MultipartFile file, String folder, int maxMB) {
        // 1. تحقق من النوع
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new BusinessException("يجب أن يكون الملف صورة");

        // 2. تحقق من الحجم
        if (file.getSize() > (long) maxMB * 1024 * 1024)
            throw new BusinessException("حجم الصورة يجب أن لا يتجاوز " + maxMB + "MB");

        // 3. ارفع لـ Cloudinary
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );
            // يرجع URL مباشر تحفظه في DB
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new BusinessException("فشل في رفع الملف");
        }
    }
}