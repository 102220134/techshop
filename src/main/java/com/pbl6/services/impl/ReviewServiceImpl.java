package com.pbl6.services.impl;

import com.pbl6.dtos.request.review.CreateReviewRequest;
import com.pbl6.dtos.request.review.SearchReviewRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.ReviewDto;
import com.pbl6.entities.ProductEntity;
import com.pbl6.entities.ReviewEntity;
import com.pbl6.entities.ReviewMediaEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.MediaType;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.ReviewMapper;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.repositories.ReviewMediaRepository;
import com.pbl6.repositories.ReviewRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.services.ReviewService;
import com.pbl6.utils.AuthenticationUtil;
import com.pbl6.utils.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final AuthenticationUtil authenticationUtil;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private final ReviewMediaRepository reviewMediaRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public void createOrUpdateReview(Long productId,CreateReviewRequest request) {
        Long userId = authenticationUtil.getCurrentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 🔍 Kiểm tra user đã review sản phẩm này chưa
        ReviewEntity review = reviewRepository.findByUserIdAndProductId(userId, product.getId())
                .orElseGet(() -> {
                    ReviewEntity newReview = new ReviewEntity();
                    newReview.setUser(user);
                    newReview.setProduct(product);
                    newReview.setCreatedAt(LocalDateTime.now());
                    return newReview;
                });

        // ✅ Cập nhật nội dung / rating
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setUpdatedAt(LocalDateTime.now());

        review = reviewRepository.save(review);

        List<ReviewMediaEntity> reviewMedias = reviewMediaRepository.findAllByReviewId(review.getId());
        if(!reviewMedias.isEmpty()){
            reviewMedias.forEach(reviewMedia->{
                cloudinaryUtil.deleteImage(reviewMedia.getUrl());
            });
            reviewMediaRepository.deleteAll(reviewMedias);
        }

        if (request.getMedias() != null && !request.getMedias().isEmpty()) {
            List<ReviewMediaEntity> medias = new ArrayList<>();

            for (int i = 0; i < request.getMedias().size(); i++) {
                var file = request.getMedias().get(i);

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) continue;

                // 🔹 public_id có định danh rõ ràng
                String folderPrefix = "reviews-" + user.getId() + "-" + product.getId()+"-"+i;
                String url = cloudinaryUtil.uploadImage(file, folderPrefix);

                ReviewMediaEntity media = new ReviewMediaEntity();
                media.setReview(review);
                media.setMediaType(MediaType.IMAGE);
                media.setUrl(url);
                medias.add(media);
            }

            if (!medias.isEmpty()) {
                reviewMediaRepository.saveAll(medias);
            }
        }
    }

    @Override
    public PageDto<ReviewDto> searchReviews(SearchReviewRequest req) {
        productRepository.findById(req.getProductId()).orElseThrow(
                ()->new AppException(ErrorCode.NOT_FOUND,"Product not found")
        );
        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize());

        Page<ReviewEntity> page = (req.getRating() == null)
                ? reviewRepository.findByProductId(req.getProductId(), pageable)
                : reviewRepository.findByProductIdAndRating(req.getProductId(), req.getRating(), pageable);

        return new PageDto<ReviewDto>( page.map(reviewMapper::toDto));
    }


}
