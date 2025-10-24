package com.pbl6.entities;

import com.pbl6.enums.MediaType;
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
    @JoinColumn(name="review_id", nullable=false)
    private ReviewEntity review;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Column(nullable=false, length=255)
    private String url;

}
