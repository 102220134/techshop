package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(name="uk_review_order_item", columnNames = "order_item_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // reviewer
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_reviews_user"))
    private UserEntity user;

    // product reviewed
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_reviews_product"))
    private ProductEntity product;

    // unique per order item
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_item_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_reviews_order_item"))
    private OrderItemEntity orderItem;

    @Column(nullable=false)
    private Short rating; // 1..5

    @Column(length=200)
    private String title;

    @Column(columnDefinition="TEXT")
    private String content;

    private Boolean isApproved = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
    private List<ReviewMediaEntity> mediaList;
}
