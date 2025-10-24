package com.pbl6.services;

import com.pbl6.dtos.request.review.CreateReviewRequest;
import com.pbl6.dtos.request.review.SearchReviewRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.ReviewDto;
import org.springframework.data.domain.Page;

public interface ReviewService {
    void createOrUpdateReview(Long productId ,CreateReviewRequest request);
    PageDto<ReviewDto> searchReviews(SearchReviewRequest req);
}
