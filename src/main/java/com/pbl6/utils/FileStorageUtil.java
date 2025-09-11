package com.pbl6.utils;

import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private final Path root;

    private final Set<String> allowedTypes = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            "image/gif"
    );

    public FileStorageUtil(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    /** Làm sạch SKU: chỉ giữ [A-Za-z0-9-_], còn lại thay bằng '-' */
    private String sanitizeSku(String sku) {
        if (sku == null) sku = "unknown";
        sku = sku.trim();
        if (sku.isEmpty()) sku = "unknown";
        // chống path traversal
        sku = sku.replace("..", "");
        return sku.replaceAll("[^A-Za-z0-9-_]", "-");
    }

    private String buildObjectKey(String sku, String originalFilename) {
        String ext = "";
        String original = StringUtils.cleanPath(
                originalFilename == null ? "image" : originalFilename
        );
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase(Locale.ROOT);

        String name = UUID.randomUUID() + "_" + Instant.now().toEpochMilli() + ext;
        String safeSku = sanitizeSku(sku);
        return "products/" + safeSku + "/images/" + name;
    }

    public StoredFile storeImage(String sku, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.FILE_EMPTY);

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new AppException(ErrorCode.INVALID_TYPE_FILE);
        }

        String objectKey = buildObjectKey(sku, file.getOriginalFilename());

        Path target = this.root.resolve(objectKey).normalize();
        // đảm bảo target vẫn nằm trong root (chống traversal)
        if (!target.startsWith(this.root)) {
            throw new AppException(ErrorCode.SECURITY_PATH_TRAVERSAL);
        }

        Files.createDirectories(target.getParent());
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return new StoredFile(
                objectKey,
                target.toString(),
                file.getSize(),
                contentType
        );
    }

    public record StoredFile(String path, String absolutePath, long size, String contentType) {}

}
