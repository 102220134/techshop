package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    InventoryLocationEntity inventoryLocation;

    @Column(nullable = false, length = 200)
    String name;

    boolean isActive;

    LocalTime timeOpen;
    LocalTime timeClose;

    Float latitude;
    Float longitude;


    String ward;
    String district;
    String province;
    String line;

    public String getDisplayAddress() {
        return java.util.stream.Stream.of(line, ward, district, province)
                .filter(s -> s != null && !s.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }

//
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;

}
