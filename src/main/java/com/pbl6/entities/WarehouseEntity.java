package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="location_id", nullable=false)
    private InventoryLocationEntity inventoryLocation;

    @Column(nullable=false, unique=true, length=50)
    private String code;

    @Column(nullable=false, length=100)
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Float latitude;
    private Float longitude;


    private String ward;
    private String district;
    private String province;
    private String line;

    public String getDisplayAddress() {
        return java.util.stream.Stream.of(line, ward, district, province)
                .filter(s -> s != null && !s.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }

}
