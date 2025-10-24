package com.pbl6.mapper;


import com.pbl6.dtos.response.ReviewDto;
import com.pbl6.entities.ReviewEntity;
import com.pbl6.entities.ReviewMediaEntity;
import com.pbl6.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final OrderItemRepository orderItemRepository;
    private final MediaMapper mediaMapper;

    public ReviewDto toDto(ReviewEntity review) {
//        boolean isPurchased = orderItemRepository.existsByUserIdAndProductId(
//                review.getUser().getId(),
//                review.getProduct().getId()
//        );

        return ReviewDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .productId(review.getProduct().getId())
                .isPurchased(false)
                .customer(ReviewDto.CustomerDto.builder()
                        .id(review.getUser().getId())
                        .name(review.getUser().getName())
                        .build())
                .medias(review.getMedias().stream().map(mediaMapper::toDto).toList())
                .build();
    }

}
