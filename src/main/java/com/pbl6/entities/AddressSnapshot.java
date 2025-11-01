package com.pbl6.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
public class AddressSnapshot {

    @Column(name = "snapshot_name")
    private String name;

    @Column(name = "snapshot_phone")
    private String phone;

    @Column(name = "snapshot_line")
    private String line;

    @Column(name = "snapshot_ward")
    private String ward;

    @Column(name = "snapshot_district")
    private String district;

    @Column(name = "snapshot_province")
    private String province;

    public String getDeliveryAddress() {
        return java.util.stream.Stream.of(line, ward, district, province)
                .filter(s -> s != null && !s.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }
}

