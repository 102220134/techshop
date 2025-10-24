package com.pbl6.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CloudinaryUtil {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;
    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String publicId)  {
        try{
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId, // path trong Cloudinary
                            "overwrite", true,
                            "resource_type", "image"
                    ));
            String fullUrl = uploadResult.get("secure_url").toString();
            String baseUrl = "https://res.cloudinary.com/" + cloudName + "/image/upload";

            String relativePath = fullUrl.replace(baseUrl, "");
            return relativePath;
        }catch (Exception e){
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR,"upload ảnh lỗi");
        }
    }

    public void deleteImage(String url) {
        try {
            String publicId =extractPublicId(url);
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image")
            );
        } catch (IOException e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Xóa ảnh Cloudinary thất bại");
        }
    }
    public String extractPublicId(String url) {
        if (url == null || url.isBlank()) return null;

        // Ví dụ url: /v1758777803/iphone-16-pro-max.webp
        // → tách ra phần sau dấu '/' cuối cùng và bỏ phần mở rộng
        int lastSlash = url.lastIndexOf('/');
        int dot = url.lastIndexOf('.');

        if (lastSlash == -1) return null;
        if (dot == -1 || dot < lastSlash) dot = url.length();

        return url.substring(lastSlash + 1, dot);
    }

}
