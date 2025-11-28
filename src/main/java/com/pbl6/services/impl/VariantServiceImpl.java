package com.pbl6.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl6.dtos.request.product.AttributeRequest;
import com.pbl6.dtos.request.product.CreateVariantRequest;
import com.pbl6.dtos.request.product.UpdateVariantRequest;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.VariantMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.VariantService;
import com.pbl6.utils.CloudinaryUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {
    private final VariantRepository variantRepository;
    private final VariantMapper variantMapper;
    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final VariantAttributeValueRepository variantAttributeValueRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryUtil cloudinaryUtil;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public VariantDto getVariantById(long variantId) {
        VariantEntity variant = variantRepository.findById(variantId).orElseThrow(
                () -> new AppException(ErrorCode.VALIDATION_ERROR, "Variant not found")
        );
        return variantMapper.toDto(variant);
    }

    @Override
    @Transactional
    public VariantDto createVariant(CreateVariantRequest vReq) {
        // Check trùng SKU
        if(variantRepository.findBySku(vReq.getSku()).isPresent()){
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Sku already exists");
        }

        VariantEntity variant = new VariantEntity();
        ProductEntity product = productRepository.findById(vReq.getProductId()).orElseThrow(
                () -> new AppException(ErrorCode.VALIDATION_ERROR, "Product not found")
        );

        variant.setProduct(product);
        variant.setSku(vReq.getSku());
        variant.setPrice(vReq.getPrice());
        variant.setIsActive(vReq.getIsActive());
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        if(vReq.getThumbnail() != null) {
            String imageUrl = cloudinaryUtil.uploadThumbnail(vReq.getThumbnail(), vReq.getSku());
            variant.setThumbnail(imageUrl);
        }

        // 1. Save lần đầu để có ID
        VariantEntity savedVariant = variantRepository.save(variant);

        // 2. Lưu Attributes
        if (vReq.getOptions() != null && !vReq.getOptions().isBlank()) {
            try {
                List<AttributeRequest> optionList = Arrays.asList(
                        objectMapper.readValue(vReq.getOptions(), AttributeRequest[].class)
                );
                // Save attributes (đã có check trùng bên trong hàm process)
                variantAttributeValueRepository.saveAll(processVariantAttributes(optionList, savedVariant));
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Format JSON của 'options' không hợp lệ");
            }
        }

        // 3. QUAN TRỌNG: Đẩy dữ liệu xuống DB trước khi Refresh
        variantRepository.flush();

        // 4. Load lại dữ liệu từ DB (để lấy @Formula stock, sold...)
        entityManager.refresh(savedVariant);

        return variantMapper.toDto(savedVariant);
    }

    @Override
    @Transactional
    public VariantDto editVariant(long variantId, UpdateVariantRequest vReq) {
        VariantEntity variant = variantRepository.findById(variantId).orElseThrow(
                () -> new AppException(ErrorCode.VALIDATION_ERROR, "Variant not found")
        );

        // --- Cập nhật thông tin cơ bản ---
        if (vReq.getProductId() != null) {
            ProductEntity product = productRepository.findById(vReq.getProductId()).orElseThrow(
                    () -> new AppException(ErrorCode.VALIDATION_ERROR, "Product not found")
            );
            variant.setProduct(product);
        }

        if (vReq.getSku() != null) variant.setSku(vReq.getSku());
        if (vReq.getIsActive() != null) variant.setIsActive(vReq.getIsActive());
        if (vReq.getPrice() != null) variant.setPrice(vReq.getPrice());

        if(vReq.getThumbnail() != null) {
            String imageUrl = cloudinaryUtil.uploadThumbnail(vReq.getThumbnail(), variant.getSku());
            variant.setThumbnail(imageUrl);
        }

        variant.setUpdatedAt(LocalDateTime.now());

        // --- Cập nhật Attributes ---
        if (vReq.getOptions() != null && !vReq.getOptions().isBlank()) {
            // 1. Xóa cũ
            List<VariantAttributeValueEntity> oldVavs = variantAttributeValueRepository.findByVariant(variant);
            if(!oldVavs.isEmpty()){
                variantAttributeValueRepository.deleteAllInBatch(oldVavs); // Dùng InBatch cho nhanh
                variantAttributeValueRepository.flush(); // FLUSH NGAY: Để tránh lỗi unique khi insert cái mới trùng cái cũ
            }

            // 2. Thêm mới
            try {
                List<AttributeRequest> optionList = Arrays.asList(
                        objectMapper.readValue(vReq.getOptions(), AttributeRequest[].class)
                );
                variantAttributeValueRepository.saveAll(processVariantAttributes(optionList, variant));
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Format JSON của 'options' không hợp lệ");
            }
        }

        // --- QUY TRÌNH LÀM MỚI DỮ LIỆU ---

        // Bước 1: Save các thay đổi cơ bản (Price, SKU...)
        VariantEntity savedVariant = variantRepository.save(variant);

        // Bước 2: Bắt buộc đẩy tất cả lệnh (Update, Insert Attribute...) xuống Database
        variantRepository.flush();

        // Bước 3: Tải lại state từ Database lên RAM (Lúc này DB đã có dữ liệu mới nhất nhờ bước 2)
        // Việc này giúp cập nhật lại các trường @Formula hoặc @Generated
        entityManager.refresh(savedVariant);

        return variantMapper.toDto(savedVariant);
    }

    // Helper method: Đã thêm logic lọc trùng lặp
    private List<VariantAttributeValueEntity> processVariantAttributes(List<AttributeRequest> options, VariantEntity variant) {
        List<VariantAttributeValueEntity> vavList = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>(); // Set dùng để check trùng lặp (Attribute + Value)

        for (AttributeRequest attrReq : options) {
            AttributeEntity attribute = attributeRepository.findByCodeAndIsOptionTrue(attrReq.getCode()).orElseThrow(
                    () -> new AppException(ErrorCode.NOT_FOUND, "Option not found: " + attrReq.getCode())
            );
            for(String value : attrReq.getValues()){
                // Tạo key unique: ví dụ "color-red"
                String key = attrReq.getCode() + "-" + value;

                // Nếu đã có trong danh sách xử lý thì bỏ qua
                if(processedKeys.contains(key)) continue;
                processedKeys.add(key);

                VariantAttributeValueEntity vav = new VariantAttributeValueEntity();
                vav.setVariant(variant);
                AttributeValueEntity attributeValue = attributeValueRepository
                        .findByValueAndAttributeId(value, attribute.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Option value not found: " + value));

                vav.setAttribute(attribute);
                vav.setAttributeValue(attributeValue);
                vavList.add(vav);
            }
        }
        return vavList;
    }
}