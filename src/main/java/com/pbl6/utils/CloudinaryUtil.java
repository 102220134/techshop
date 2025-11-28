package com.pbl6.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation; // 👈 Quan trọng: Import class này
import com.cloudinary.utils.ObjectUtils;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudinaryUtil {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;
    private final Cloudinary cloudinary;

    public String uploadThumbnail(MultipartFile file, String publicId) {
        try {
            // ✅ FIX: Dùng Transformation Builder thay vì Map
            Transformation transformation = new Transformation()
                    .width(400)
                    .height(400)
                    .crop("fill")
                    .gravity("auto")
                    .quality("auto:eco")
                    .fetchFormat("auto"); // Tự động chuyển sang webp/avif tùy trình duyệt

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", false,
                            "unique_filename", true,
                            "resource_type", "image",
                            "transformation", transformation // 👈 Truyền object Transformation vào đây
                    )
            );

            return getCleanUrl(uploadResult.get("secure_url").toString());

        } catch (Exception e) {
            log.error("❌ Upload Thumbnail Failed: {}", e.getMessage());
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    public String uploadImage(MultipartFile file, String publicId) {
        try {
            // ✅ FIX: Dùng Transformation cho hàm uploadImage luôn
            Transformation transformation = new Transformation()
                    .width(1080)
                    .height(1080)
                    .crop("limit")
                    .quality("auto:eco")
                    .fetchFormat("auto");

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", false,
                            "unique_filename", false,
                            "resource_type", "image",
                            "transformation", transformation
                    )
            );

            return getCleanUrl(uploadResult.get("secure_url").toString());

        } catch (Exception e) {
            log.error("❌ Upload Image Failed: {}", e.getMessage());
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Lỗi upload ảnh: " + e.getMessage());
        }
    }

    public void deleteImage(String url) {
        if (url == null || url.isBlank()) return;
        try {
            String publicId = extractPublicId(url);
            if (publicId == null) return;

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            log.error("❌ Delete Image Failed: {}", e.getMessage());
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Xóa ảnh thất bại");
        }
    }

    private String getCleanUrl(String fullUrl) {
        String baseUrl = "https://res.cloudinary.com/" + cloudName + "/image/upload";
        if (fullUrl.startsWith(baseUrl)) {
            return fullUrl.substring(baseUrl.length());
        }
        return fullUrl;
    }

    public String extractPublicId(String url) {
        try {
            String path = url;
            String prefix = "https://res.cloudinary.com/" + cloudName + "/image/upload/";
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            }
            if (path.startsWith("/v")) {
                int firstSlashAfterVersion = path.indexOf('/', 2);
                if (firstSlashAfterVersion != -1) {
                    path = path.substring(firstSlashAfterVersion + 1);
                }
            } else if (path.startsWith("v")) {
                int firstSlash = path.indexOf('/');
                if (firstSlash != -1) {
                    path = path.substring(firstSlash + 1);
                }
            }
            int dot = path.lastIndexOf('.');
            if (dot != -1) {
                path = path.substring(0, dot);
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }
}