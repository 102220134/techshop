package com.pbl6.services.impl;

import com.pbl6.dtos.request.review.CreateReviewRequest;
import com.pbl6.dtos.request.review.SearchReviewRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.ReviewDto;
import com.pbl6.entities.*;
import com.pbl6.enums.MediaType;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.ReviewMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.ReviewService;
import com.pbl6.utils.AuthenticationUtil;
import com.pbl6.utils.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final AuthenticationUtil authUtil;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final ReviewRepository reviewRepo;
    private final ReviewMediaRepository mediaRepo;
    private final CloudinaryUtil cloudinary;
    private final ReviewMapper reviewMapper;

    private static final int MAX_IMAGES = 5;

    @Override
    @Transactional
    public void createOrUpdateReview(Long productId, CreateReviewRequest request) {
        Long userId = authUtil.getCurrentUserId();

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));

        // 🔍 Tìm hoặc tạo review mới
        ReviewEntity review = reviewRepo.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    ReviewEntity r = new ReviewEntity();
                    r.setUser(user);
                    r.setProduct(product);
                    r.setCreatedAt(LocalDateTime.now());
                    return r;
                });

        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setUpdatedAt(LocalDateTime.now());
        review = reviewRepo.save(review);

        // 🧹 Xử lý ảnh — sau khi DB commit
        if (request.getMedias() != null && !request.getMedias().isEmpty()) {
            List<MultipartFile> newFiles = request.getMedias();

            if (newFiles.size() > MAX_IMAGES) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Tối đa 5 ảnh mỗi review");
            }

            // ✅ upload async sau khi commit
            asyncUploadReviewImages(user.getId(), product.getId(), review.getId(), newFiles);
        }
    }

    @Async
    protected void asyncUploadReviewImages(Long userId, Long productId, Long reviewId, List<MultipartFile> files) {
        try {
            // 🧹 Xóa ảnh cũ
            List<ReviewMediaEntity> old = mediaRepo.findAllByReviewId(reviewId);
            old.forEach(m -> cloudinary.deleteImage(m.getUrl()));
            mediaRepo.deleteAll(old);

            // 🚀 Upload song song
            List<CompletableFuture<ReviewMediaEntity>> uploadTasks = files.stream()
                    .filter(f -> f.getContentType() != null && f.getContentType().startsWith("image/"))
                    .map(f -> CompletableFuture.supplyAsync(() -> {
                        String folder = "reviews/u" + userId + "-p" + productId + "-" + System.nanoTime();
                        String url = cloudinary.uploadThumbnail(f, folder);
                        ReviewMediaEntity e = new ReviewMediaEntity();
                        e.setReview(ReviewEntity.builder().id(reviewId).build());
                        e.setMediaType(MediaType.IMAGE);
                        e.setUrl(url);
                        return e;
                    }))
                    .collect(Collectors.toList());

            List<ReviewMediaEntity> result = uploadTasks.stream()
                    .map(CompletableFuture::join)
                    .toList();

            mediaRepo.saveAll(result);
        } catch (Exception e) {
            log.error("Upload review images failed", e);
        }
    }

    @Override
    public PageDto<ReviewDto> searchReviews(SearchReviewRequest req) {
        productRepo.findById(req.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));

        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize(), Sort.by("createdAt").descending());

        Page<ReviewEntity> page = (req.getRating() == null)
                ? reviewRepo.findByProductId(req.getProductId(), pageable)
                : reviewRepo.findByProductIdAndRating(req.getProductId(), req.getRating(), pageable);

        return new PageDto<>(page.map(reviewMapper::toDto));
    }
}
