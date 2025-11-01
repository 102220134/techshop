package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, length = 20)
    private String phone;

    private String gender;
    private LocalDate birth;

    private String avatar;
    private Boolean isActive = true;
    private Boolean isGuest = false;

    private Long storeId;
    private Long warehouseId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_product_likes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<ProductEntity> likedProducts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserAddressEntity> addresses;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<RefreshTokenEntity> refreshTokens;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<CartItemEntity> cartItems;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<OrderEntity> orders;

    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY)
    private List<OrderEntity> salesOrders;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<DebtEntity> debts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<VoucherUsageEntity> voucherUsages;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews;


    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> authorities = roles.stream()
                .flatMap(role -> {
                    Set<String> rolePerms = role.getPermissions().stream()
                            .map(PermissionEntity::getName)
                            .collect(Collectors.toSet());
                    rolePerms.add("ROLE_" + role.getName());
                    return rolePerms.stream();
                })
                .collect(Collectors.toSet());

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isCustomer() {
        return roles.stream()
                .anyMatch(role -> role.equals("CUSTOMER"));
    }

    public boolean isStaff() {
        return roles.stream()
                .anyMatch(role -> role.getName().startsWith("STAFF_"));
    }
    public boolean isAdmin() {
        return roles.stream()
                .anyMatch(role -> role.equals("ADMIN"));
    }


    public boolean hasAuthority(String authority) {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
