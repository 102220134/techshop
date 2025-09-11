package com.pbl6.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.request.ProductRequest;
import com.pbl6.dtos.response.ProductListItemDto;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.repositories.OrderItemRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.repositories.ReviewRepository;
import com.pbl6.services.CategoryService;
import com.pbl6.services.ProductService;
import com.pbl6.specifications.ProductSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSpecs productSpecs;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final ReviewRepository reviewRepository;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ObjectNode parseDetail(String json) {
        if (json == null) return objectMapper.createObjectNode();
        try {
            return (ObjectNode) objectMapper.readTree(json);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    @Override
    public Page<ProductListItemDto> getProductsByCategory(ProductRequest req, Pageable pageable) {
        Page<ProductProjection> page = productRepository.findProductsWithSlug(
                req.getSlugPath(),
                req.getIncludeInactive(),
                pageable
        );

        List<ProductListItemDto> dtos = page.getContent().stream()
                .map(p -> new ProductListItemDto(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        parseDetail(p.getDetail()),
                        p.getSlug(),
                        p.getThumbnail(),
                        p.getPrice(),
                        p.getStock(),
                        new ProductListItemDto.RatingSummary(
                                p.getTotal(),
                                p.getStar1(),
                                p.getStar2(),
                                p.getStar3(),
                                p.getStar4(),
                                p.getStar5(),
                                BigDecimal.valueOf(p.getAverage() == null ? 0 : p.getAverage())
                        )
                ))
                .toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

}
