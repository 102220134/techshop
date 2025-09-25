package com.pbl6.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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

    public String uploadImage(MultipartFile file, String publicId) throws IOException {
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
    }
}
