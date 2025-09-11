package com.pbl6.dtos.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class FileRequest {
    private String sku;
    private MultipartFile file;
}
