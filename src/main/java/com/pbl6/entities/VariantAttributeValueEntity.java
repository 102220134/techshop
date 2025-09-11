package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="variant_attribute_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantAttributeValueEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="variant_id", nullable=false)
    private VariantEntity variant;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="attribute_id", nullable=false)
    private AttributeEntity attribute;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="value_id", nullable=false)
    private AttributeValueEntity attributeValue;
}
