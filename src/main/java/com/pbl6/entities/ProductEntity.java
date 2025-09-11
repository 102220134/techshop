package com.pbl6.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pbl6.converters.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String name;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(columnDefinition="JSON")
//    @Convert(converter = JsonNodeConverter.class)
    private ObjectNode detail;

    @Column(nullable=false, unique=true, length=120)
    private String slug;

    private String thumbnail;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<VariantEntity> variants;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "category_products",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<CategoryEntity> categories;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<MediaEntity> media;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductAttributeValueEntity> productAttributeValues;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews;
}
