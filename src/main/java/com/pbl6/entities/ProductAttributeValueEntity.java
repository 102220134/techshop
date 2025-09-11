package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="product_attribute_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValueEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false)
    private ProductEntity product;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="attribute_id", nullable=false)
    private AttributeEntity attribute;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="value_id", nullable=false)
    private AttributeValueEntity attributeValue;
}
