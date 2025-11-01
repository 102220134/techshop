package com.pbl6.utils;

import com.cloudinary.Cloudinary;
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
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", false,
                            "unique_filename", true,
                            "resource_type", "image",
                            "transformation", ObjectUtils.asMap(
                                    "width", 400,
                                    "height", 400,
                                    "crop", "fill",
                                    "gravity", "auto",
                                    "format", "webp",
                                    "quality", "auto:eco",
                                    "fetch_format", "auto"
                            )
                    )
            );

            String fullUrl = uploadResult.get("secure_url").toString();
            String baseUrl = "https://res.cloudinary.com/" + cloudName + "/image/upload";
            return fullUrl.replace(baseUrl, "");

        } catch (Exception e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Upload thumbnail lên Cloudinary thất bại");
        }
    }

    public String uploadImage(MultipartFile file, String publicId) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", false,
                            "unique_filename", false,
                            "resource_type", "image",
                            "format", "webp",
                            "transformation", ObjectUtils.asMap(
                                    "width", 1080,
                                    "height", 1080,
                                    "crop", "limit",
                                    "quality", "auto:eco",
                                    "fetch_format", "auto"
                            )
                    )
            );

            String fullUrl = uploadResult.get("secure_url").toString();
            String baseUrl = "https://res.cloudinary.com/" + cloudName + "/image/upload";
            return fullUrl.replace(baseUrl, "");

        } catch (Exception e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Upload ảnh user thất bại");
        }
    }

    public void deleteImage(String url) {
        if (url == null || url.isBlank()) return;

        try {
            String publicId = extractPublicId(url);
            if (publicId == null) {
                log.warn("⚠️ Không thể tách public_id từ URL: {}", url);
                return;
            }

            Map result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "image"));

        } catch (IOException e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Xóa ảnh Cloudinary thất bại");
        }
    }
    public String extractPublicId(String url) {
        try {
            // Ví dụ: /v1761278279/reviews-34-503-0.webp
            String path = url;

            // Loại bỏ phần domain nếu là absolute URL
            String prefix = "https://res.cloudinary.com/" + cloudName + "/image/upload/";
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            }

            // Nếu path bắt đầu bằng /vxxxx/
            if (path.startsWith("/v")) {
                // Bỏ version (từ /vxxxx/ đến sau dấu '/')
                int firstSlashAfterVersion = path.indexOf('/', 2); // bỏ qua /v
                if (firstSlashAfterVersion != -1) {
                    path = path.substring(firstSlashAfterVersion + 1);
                }
            } else if (path.startsWith("v")) {
                // vxxxx/... (không có slash đầu)
                int firstSlash = path.indexOf('/');
                if (firstSlash != -1) {
                    path = path.substring(firstSlash + 1);
                }
            }

            // Bỏ đuôi .webp hoặc .jpg, ...
            int dot = path.lastIndexOf('.');
            if (dot != -1) {
                path = path.substring(0, dot);
            }

            // => kết quả public_id
            return path;
        } catch (Exception e) {
            log.error("❌ extractPublicId lỗi với URL: {}", url, e);
            return null;
        }
    }


}
