package com.pbl6.services.impl;

import com.pbl6.dtos.request.category.CategoryCreateRequest;
import com.pbl6.dtos.request.category.CategoryUpdateRequest;
import com.pbl6.dtos.response.BreadcrumbDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.entities.ProductEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.CategoryMapper;
import com.pbl6.repositories.CategoryRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.services.CategoryService;
import com.pbl6.utils.CloudinaryUtil;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper cateMapper;
    private final EntityUtil entityUtil;
    private final ProductRepository productRepository;
    private final CloudinaryUtil cloudinaryUtil;

    @Value("${default_root_category}")
    private String defaultRootCategory;


    private Long getRootId() {
        return categoryRepository.findBySlugAndParentIdAndIsActiveTrue(defaultRootCategory, null)
                .map(CategoryEntity::getId)
                .orElseThrow(() -> new AppException(ErrorCode.DATABASE_ERROR,"Root category not found"));
    }

    public CategoryEntity resolveBySlugPath(String slugPath) {
        String[] parts = slugPath.split("/");
        Long parentId = getRootId();

        CategoryEntity current = null;
        for (String slug : parts) {
            current = categoryRepository.findBySlugAndParentIdAndIsActiveTrue(slug, parentId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,"category not found"));
            parentId = current.getId();
        }
        return current;
    }


    @Override
    public CategoryDto getChildrenByType(String slugPath, String type) {

        CategoryEntity node = resolveBySlugPath(slugPath);

        List<CategoryEntity> children = (type == null || type.isBlank())
                ? categoryRepository.findByParentId(node.getId())
                : categoryRepository.findByParentIdAndCategoryType(node.getId(), type);

        CategoryDto dto = cateMapper.toDto(node);
        dto.setChildren(children.stream().filter(CategoryEntity::getIsActive).map(cateMapper::toDto).toList());
        dto.setBreadcrumb(new BreadcrumbDto(buildBreadcrumbItems(node.getParent()), cateMapper.toBreadcrumbItem(node)));
        return dto;
    }

    @Override
    public List<CategoryDto> getCategoryByRoot() {
        List<CategoryEntity> categoryEntities = categoryRepository.findByParentId(getRootId());
        return categoryEntities.stream()
                .filter(CategoryEntity::getIsActive)
                .map(cateMapper::toDto)
                .toList();
    }


    @Override
    public List<CategoryDto> getCategoryTree(Boolean isOnlyActive) {
        List<CategoryEntity> mainCategories = categoryRepository.findByParentId(getRootId());
        return mainCategories.stream()
                .filter(category -> !Boolean.TRUE.equals(isOnlyActive) || category.getIsActive())
                .map(category -> this.buildCategoryTree(category, isOnlyActive))
                .toList();
    }

    @Override
    public CategoryDto createCategory(CategoryCreateRequest request) {
        String logoUrl = null;
        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            MultipartFile file = request.getLogo();
            try{
                logoUrl = cloudinaryUtil.uploadImage(file, request.getSlug());
            }catch (Exception e){
                throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR,"Error upload logo");
            }
        }

        CategoryEntity category = new CategoryEntity();
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setCategoryType(request.getCategoryType());
        category.setIsActive(request.getIsActive());
        category.setLogo(logoUrl);
        category.setCreatedAt(LocalDateTime.now());

        if (request.getParentId() != null) {
            CategoryEntity parent = entityUtil.ensureExists(categoryRepository.findById(request.getParentId()));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        }

        return cateMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long id, CategoryUpdateRequest request) {

        CategoryEntity category = entityUtil.ensureExists(categoryRepository.findById(id));

        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName().trim());
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            category.setSlug(request.getSlug().trim());
        }

        if (request.getCategoryType() != null) {
            category.setCategoryType(request.getCategoryType());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        if (request.getParentId() != null) {
            CategoryEntity parent = entityUtil.ensureExists(categoryRepository.findById(request.getParentId()));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        }

        // 🔹 3. Xử lý upload logo mới (nếu có)
        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            String logoUrl = category.getLogo();
            MultipartFile file = request.getLogo();
            try{
                logoUrl = cloudinaryUtil.uploadImage(file, request.getSlug());
            }catch (Exception e){
                throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR,"Error upload logo");
            }
            category.setLogo(logoUrl);
        }

        category.setUpdatedAt(LocalDateTime.now());

        CategoryEntity saved = categoryRepository.save(category);

        return cateMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = entityUtil.ensureExists(categoryRepository.findById(id));

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,"Cannot delete category has child or product");
        }

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,"Cannot delete category has child or product");
        }

//        if (category.getLogo() != null) {
//            fileStorageService.deleteFile(category.getLogo());
//        }
        categoryRepository.delete(category);
    }

    @Override
    public BreadcrumbDto getBreadcrumbByProductSlug(String productSlug) {
        ProductEntity product = entityUtil.ensureExists(productRepository.findBySlug(productSlug));
        if (!product.getIsActive()) {
            throw new AppException(ErrorCode.NOT_FOUND, "product not found");
        }

        BreadcrumbDto.BreadcrumbItem current =
                new BreadcrumbDto.BreadcrumbItem(product.getName(), product.getSlug());

        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            return new BreadcrumbDto(null, current);
        }

        // ✅ Chỉ lấy category sâu nhất mà toàn bộ cha đều active
        CategoryEntity deepestCategory = product.getCategories().stream()
                .filter(this::allParentsActive) // kiểm tra chain cha
                .max(Comparator.comparingInt(CategoryEntity::getLevel))
                .orElse(null);

        if (deepestCategory == null) {
            // Không có category nào hợp lệ
            return new BreadcrumbDto(null, current);
        }

        List<BreadcrumbDto.BreadcrumbItem> items = buildBreadcrumbItems(deepestCategory);
        return new BreadcrumbDto(items, current);
    }


    private CategoryDto buildCategoryTree(CategoryEntity category, Boolean isOnlyActive) {
        if (Boolean.TRUE.equals(isOnlyActive) && !category.getIsActive()) {
            return null;
        }
        CategoryDto dto = cateMapper.toDto(category);
        List<CategoryEntity> childrenEntities = categoryRepository.findByParentId(category.getId());

        List<CategoryDto> childrenDtos = childrenEntities.stream()
                .map(subCate -> this.buildCategoryTree(subCate, isOnlyActive)) // đệ quy
                .filter(Objects::nonNull)
                .toList();

        dto.setChildren(childrenDtos);
        return dto;
    }


    private List<BreadcrumbDto.BreadcrumbItem> buildBreadcrumbItems(CategoryEntity category) {
        List<BreadcrumbDto.BreadcrumbItem> items = new ArrayList<>();
        CategoryEntity temp = category;

        // Chỉ add nếu category active
        while (temp != null && temp.getIsActive()) {
            items.add(cateMapper.toBreadcrumbItem(temp));
            temp = temp.getParent();
        }

        Collections.reverse(items);
        return items;
    }

    private boolean allParentsActive(CategoryEntity category) {
        CategoryEntity current = category;
        while (current != null) {
            if (!Boolean.TRUE.equals(current.getIsActive())) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

}
