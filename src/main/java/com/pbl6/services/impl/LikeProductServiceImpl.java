package com.pbl6.services.impl;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.ProductEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.mapper.ProductMapper;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.services.LikeProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeProductServiceImpl implements LikeProductService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void likeProduct(Long userId, Long productId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!user.getLikedProducts().contains(product)) {
            user.getLikedProducts().add(product);
            userRepository.save(user);
        } else {
            return;
        }
    }

    @Override
    @Transactional
    public void unlikeProduct(Long userId, Long productId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (user.getLikedProducts().contains(product)) {
            user.getLikedProducts().remove(product);
            userRepository.save(user);
        } else {
            return;
        }
    }
    @Override
    public PageDto<ProductDto> getLikedByUser(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page-1, size, Sort.by("id").descending());

        Page<ProductEntity> productPage = productRepository.findLikedProductsByUserId(userId, pageable);
        return new PageDto<ProductDto>(productPage.map(productMapper::toDto));
    }
}
