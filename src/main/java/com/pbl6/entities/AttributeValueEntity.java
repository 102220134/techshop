package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="attribute_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="attribute_id", nullable=false)
    private AttributeEntity attribute;

    @Column(nullable=false, length=100)
    private String value;

    private String label;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="attributeValue", fetch=FetchType.LAZY)
    private List<ProductAttributeValueEntity> productValues;

    @OneToMany(mappedBy="attributeValue", fetch=FetchType.LAZY)
    private List<VariantAttributeValueEntity> variantValues;
}
