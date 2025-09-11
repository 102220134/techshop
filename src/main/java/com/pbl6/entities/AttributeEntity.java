package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=50)
    private String code;

    @Column(nullable=false, length=100)
    private String label;

    private Boolean isOption = false;
    private Boolean isFilter = false;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="attribute", fetch=FetchType.LAZY)
    private List<AttributeValueEntity> values;

    @OneToMany(mappedBy="attribute", fetch=FetchType.LAZY)
    private List<ProductAttributeValueEntity> productValues;

    @OneToMany(mappedBy="attribute", fetch=FetchType.LAZY)
    private List<VariantAttributeValueEntity> variantValues;
}
