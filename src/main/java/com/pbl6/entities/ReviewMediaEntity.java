package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="review_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_review_media_review"))
    private ReviewEntity review;

    @Column(nullable=false, length=40) // 'image','video'
    private String mediaType = "image";

    @Column(nullable=false, length=255)
    private String url;

    private Integer sortOrder = 0;
}
