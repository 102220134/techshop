package com.pbl6.entities;

import com.pbl6.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private String url;
    private String altText;
    private Integer sortOrder = 0;
    private LocalDateTime createdAt;


}
