package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String fullName;
    private String phone;
    private String line1;
    private String line2;
    private String ward;
    private String district;
    private String province;
    private String country = "VN";
    private Boolean isDefault = false;

    private Date createdAt;
    private Date updatedAt;
}
