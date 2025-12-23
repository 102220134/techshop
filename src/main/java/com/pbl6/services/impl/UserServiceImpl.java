package com.pbl6.services.impl;

import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.request.user.*;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.user.UserAddressDto;
import com.pbl6.dtos.response.user.UserDetailDto;
import com.pbl6.dtos.response.user.UserDto;
import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.entities.RoleEntity;
import com.pbl6.entities.UserAddressEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.OrderStatus;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.UserAddressMapper;
import com.pbl6.mapper.UserMapper;
import com.pbl6.repositories.InventoryLocationRepository;
import com.pbl6.repositories.RoleRepository;
import com.pbl6.repositories.UserAddressRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.services.AuthService;
import com.pbl6.services.UserService;
import com.pbl6.specifications.UserSpecification;
import com.pbl6.utils.AuthenticationUtil;
import com.pbl6.utils.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final UserAddressRepository userAddressRepository;
    private final UserAddressMapper userAddressMapper;
    private final EntityManager em;
    private final AuthenticationUtil authenticationUtil;
    private final InventoryLocationRepository inventoryLocationRepository;

    @Override
    @Transactional
    public UserDto createUser(RegisterRequest registerRequest) {
        Optional<UserEntity> existingUserOpt = userRepository.findByPhone(registerRequest.getPhone());

        if (existingUserOpt.isEmpty()) {
            UserEntity newUser = userMapper.toUserEntity(registerRequest);
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            RoleEntity roleEntity = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> {
                        log.error("Role {} not found during user creation.", "CUSTOMER");
                        return new AppException(ErrorCode.INTERNAL_ERROR,"error role not found");
                    });
            newUser.setRoles(Set.of(roleEntity));
            newUser.setIsGuest(false);
            newUser.setIsActive(true);
            newUser = userRepository.save(newUser);
            return userMapper.toUserDto(newUser);
        }

        UserEntity existingUser = existingUserOpt.get();

        if (!existingUser.getIsGuest()) {
            log.warn("User with phone: {} already exists and is not a guest. Throwing USER_EXISTED.", registerRequest.getPhone());
            throw new AppException(ErrorCode.ALREADY_EXISTS,"phone exists");
        }

        existingUser.setName(registerRequest.getName());
        existingUser.setEmail(registerRequest.getEmail());
        existingUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        existingUser.setIsGuest(false);
        existingUser.setIsActive(true);
        existingUser = userRepository.save(existingUser);
        return userMapper.toUserDto(existingUser);
    }

    @Override
    public UserEntity loadUserByPhone(String phone) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByPhoneAndIsActive(phone, true).orElseThrow(
                () -> new UsernameNotFoundException("phone not found")
        );
        return user;
    }

    @Override
    public UserEntity createOrGetGuest(String email, String phone, String name) {
        Optional<UserEntity> userOptional = userRepository.findByPhone(phone);

        if (userOptional.isPresent()) {
            UserEntity existingUser = userOptional.get();
            if (Boolean.FALSE.equals(existingUser.getIsActive())) {
                log.warn("Attempt to create guest with inactive phone: {}", phone);
                throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,"Phone locked");
            }
            // If user is present and active, return it
            log.info("Existing active user found for phone: {}", phone);
            return existingUser;
        }

        // No user found, or user is inactive (handled above), so create a new guest
        RoleEntity roleEntity = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> {
                    log.error("Role {} not found during guest user creation.", "CUSTOMER");
                    return new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,"error role not found");
                });

        UserEntity guest = UserEntity.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .roles(Set.of(roleEntity))
                .isGuest(true)
                .isActive(true)
                .build();

        log.info("Creating new guest account for phone: {}", phone);
        return userRepository.save(guest);
    }

    @Override
    public UserDetailDto getUserInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for userId: {}", userId);
                    return new AppException(ErrorCode.NOT_FOUND,"user not found");
                });

        UserEntity currentUser = authenticationUtil.getCurrentUser();

        if (Objects.equals(currentUser.getId(), userId)) {
            return userMapper.toUserDetailDto(user);
        }
        if(currentUser.isAdmin()){
            return userMapper.toUserDetailDto(user);
        }
        if(user.isStaff() && currentUser.hasAuthority("STAFF_READ")){
            return userMapper.toUserDetailDto(user);
        }
        if(user.isCustomer() && currentUser.hasAuthority("CUSTOMER_READ")){
            return userMapper.toUserDetailDto(user);
        }


        throw new AppException(ErrorCode.FORBIDDEN);
    }


    @Override
    public UserDetailDto updateUserInfo(Long userId, UserUpdateInfoRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Profile update failed: User not found for userId: {}", userId);
                    return  new AppException(ErrorCode.NOT_FOUND,"user not found");
                });

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirth() != null) {
            user.setBirth(request.getBirth());
        }

        UserEntity updatedUser = userRepository.save(user);
        return userMapper.toUserDetailDto(updatedUser);
    }

    @Override
    public UserDetailDto updateUserRole(Long userId, UserUpdateRoleRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Profile update failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.NOT_FOUND,"user not found");
                });

        List<String> newRoles = request.getRoles();
        if (newRoles == null || newRoles.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,"not empty or null");
        }
        Set<RoleEntity> roleEntities = new HashSet<>();
        for (String roleEnum : newRoles) {
            RoleEntity role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR,"not match with role name"));
            roleEntities.add(role);
        }

        user.setRoles(roleEntities);
        return userMapper.toUserDetailDto(userRepository.save(user));
    }

    @Override
    public UserDetailDto updateUserStatus(Long userId, UserUpdateStatusRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Status update failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.NOT_FOUND,"User not found");
                });


        UserEntity currentUser = authenticationUtil.getCurrentUser();
        if(currentUser.isAdmin()){
            user.setIsActive(request.getIsActive());
            UserEntity updatedUser = userRepository.save(user);

            return userMapper.toUserDetailDto(updatedUser);
        }
        if(user.isStaff() && currentUser.hasAuthority("STAFF_UPDATE")){
            user.setIsActive(request.getIsActive());
            UserEntity updatedUser = userRepository.save(user);

            return userMapper.toUserDetailDto(updatedUser);
        }
        if(user.isCustomer() && currentUser.hasAuthority("CUSTOMER_UPDATE")){
            user.setIsActive(request.getIsActive());
            UserEntity updatedUser = userRepository.save(user);

            return userMapper.toUserDetailDto(updatedUser);
        }
        throw new AppException(ErrorCode.FORBIDDEN);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Password change failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.NOT_FOUND,"User not found");
                });

        if (!passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password change failed for userId {}: Current password verification failed.", userId);
            throw new AppException(ErrorCode.UNAUTHORIZED, " Current password verification failed.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password change failed for userId {}: New password and confirm password do not match.", userId);
            throw new AppException(ErrorCode.UNAUTHORIZED, "New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserAddressDto createAddress(Long userId, UserAddressCreateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Address creation failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.NOT_FOUND,"User not found");
                });

        UserAddressEntity newAddress = new UserAddressEntity();
        newAddress.setUser(user);
        newAddress.setLine(request.getLine());
        newAddress.setWard(request.getWard());
        newAddress.setDistrict(request.getDistrict());
        newAddress.setProvince(request.getProvince());

        boolean requestWantsDefault = Boolean.TRUE.equals(request.getIsDefault());
        long existingDefaultAddressesCount = userAddressRepository.countByUserAndIsDefaultTrue(user);
        long totalExistingAddressesCount = userAddressRepository.countByUser(user);


        if (totalExistingAddressesCount == 0) {
            newAddress.setIsDefault(true);
        } else if (requestWantsDefault) {
            List<UserAddressEntity> currentDefaultAddresses = userAddressRepository.findByUserAndIsDefaultTrue(user);
            for (UserAddressEntity defaultAddr : currentDefaultAddresses) {
                defaultAddr.setIsDefault(false);
                userAddressRepository.save(defaultAddr);
            }
            newAddress.setIsDefault(true);
        } else {
            if (existingDefaultAddressesCount == 0) {
                newAddress.setIsDefault(true);
            } else {
                newAddress.setIsDefault(false);
            }
        }

        UserAddressEntity savedAddress = userAddressRepository.save(newAddress);
        return userAddressMapper.toDto(savedAddress);
    }

    @Override
    public UserAddressDto updateAddress(Long userId, UserAddressUpdateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Address update failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.NOT_FOUND,"user not found");
                });

        UserAddressEntity addressToUpdate = userAddressRepository.findById(request.getId())
                .orElseThrow(() -> {
                    log.warn("Address update failed: Address not found for addressId: {}", request.getId());
                    return new AppException(ErrorCode.NOT_FOUND,"address not found");
                });

        if (!addressToUpdate.getUser().getId().equals(userId)) {
            log.warn("Address update failed: AddressId {} does not belong to userId {}", request.getId(), userId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        addressToUpdate.setLine(request.getLine());
        addressToUpdate.setWard(request.getWard());
        addressToUpdate.setDistrict(request.getDistrict());
        addressToUpdate.setProvince(request.getProvince());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<UserAddressEntity> currentDefaultAddresses = userAddressRepository.findByUserAndIsDefaultTrue(user);
            for (UserAddressEntity defaultAddr : currentDefaultAddresses) {
                if (!defaultAddr.getId().equals(addressToUpdate.getId())) {
                    defaultAddr.setIsDefault(false);
                    userAddressRepository.save(defaultAddr);
                }
            }
            addressToUpdate.setIsDefault(true);
        } else {
            addressToUpdate.setIsDefault(false);

            long otherDefaultAddressesCount = userAddressRepository.countByUserAndIsDefaultTrueAndIdNot(user, addressToUpdate.getId());

            if (otherDefaultAddressesCount == 0) {
                long totalAddressesCount = userAddressRepository.countByUser(user);

                if (totalAddressesCount == 1) {
                    addressToUpdate.setIsDefault(true);
                    log.warn("AddressId {} is the only address for userId {}. It must remain default.", request.getId(), userId);
                } else {
                    userAddressRepository.findFirstByUserAndIdNot(user, addressToUpdate.getId())
                            .ifPresent(newDefaultAddress -> {
                                newDefaultAddress.setIsDefault(true);
                                userAddressRepository.save(newDefaultAddress);
                            });
                }
            }
        }

        UserAddressEntity savedAddress = userAddressRepository.save(addressToUpdate);
        return userAddressMapper.toDto(savedAddress);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        var address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address deletion failed: Address not found for addressId: {}", addressId);
                    return new AppException(ErrorCode.NOT_FOUND,"address not found");
                });
        if (!address.getUser().getId().equals(userId)) {
            log.warn("Address deletion failed: AddressId {} does not belong to userId {}", addressId, userId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        long totalAddressesCount = userAddressRepository.countByUser(address.getUser());
        if (totalAddressesCount == 1) {
            log.warn("Cannot delete addressId {} for userId {}: It is the only address.", addressId, userId);
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,"cant delete last address");
        }

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            userAddressRepository.findFirstByUserAndIdNot(address.getUser(), addressId)
                    .ifPresent(newDefaultAddress -> {
                        newDefaultAddress.setIsDefault(true);
                        userAddressRepository.save(newDefaultAddress);
                    });
        }

        userAddressRepository.delete(address);
    }

//    @Override
//    public void deleteUser(Long userId) {
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> {
//                    log.warn("User deletion failed: User not found for userId: {}", userId);
//                    return new AppException(ErrorCode.USER_NOT_FOUND);
//                });
//        userRepository.delete(user);
//    }

    @Override
    public PageDto<UserDto> searchCustomers(SearchUserRequest request) {
        Specification<UserEntity> spec = Specification
                .where(UserSpecification.hasKeyword(request.getKeyword()))
                .and(UserSpecification.isActive(request.getIsActive()))
                .and(UserSpecification.hasCustomerRole());
        return searchUsers(request, spec);
    }

    @Override
    public PageDto<UserDto> searchStaffs(SearchUserRequest request) {
        Specification<UserEntity> spec = Specification
                .where(UserSpecification.hasKeyword(request.getKeyword()))
                .and(UserSpecification.isActive(request.getIsActive()))
                .and(UserSpecification.hasStaffRole());
        return searchUsers(request, spec);
    }

    @Override
    public UserDetailDto getUserInfoByPhone(String phone) {
        UserEntity user = userRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    log.warn("User not found for phone: {}", phone);
                    return new AppException(ErrorCode.NOT_FOUND,"User not found");
                });
        return userMapper.toUserDetailDto(user);
    }

    @Transactional
    @Override
    public UserDto createStaff(CreateStaffRequest request) {
        // 1. Check phone tồn tại
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,"SDT đã tồn tại");
        }
        // 2. Check email tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,"Email đã tồn tại");
        }

            UserEntity newUser = new UserEntity();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPhone(request.getPhone());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setIsGlobalStaff(false);
            Set<InventoryLocationEntity> locations = inventoryLocationRepository.findByIds(request.getScops());
            newUser.setScops(locations);
            Set<RoleEntity> roles = roleRepository.findAllByNameIn(request.getRoles());
            newUser.setRoles(roles);
            newUser.setIsGuest(false);
            newUser.setIsActive(true);
            newUser = userRepository.save(newUser);
            return userMapper.toUserDto(newUser);
    }

    @Override
    @Transactional
    public UserDto updateStaff(Long userId, UpdateStaffRequest request) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR,"Nhân viên không tồn tại"));

        // 1️⃣ Check email trùng (trừ chính nó)
        if (!user.getEmail().equals(request.getEmail())
            && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,"Email đã được sử dụng");
        }

        // 3️⃣ Update basic info
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setBirth(request.getBirth());
        user.setIsActive(request.getIsActive());
        user.setIsGlobalStaff(request.getIsGlobalStaff());

        // 4️⃣ Update password (nếu có)
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 5️⃣ Update roles
        Set<RoleEntity> roles = roleRepository.findAllByNameIn(request.getRoles());
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,"Có role không tồn tại");
        }
        user.setRoles(roles);

        // 6️⃣ Update scopes
        if (request.getScops() != null) {
            Set<InventoryLocationEntity> locations =
                    inventoryLocationRepository.findAllById(request.getScops())
                            .stream()
                            .collect(Collectors.toSet());


//            if (locations.size() != request.getScops().size()) {
//                throw new BusinessException("Có kho / địa điểm không tồn tại");
//            }

            user.setScops(locations);
        }

        // 7️⃣ Save
        UserEntity savedUser = userRepository.save(user);
        return userMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto createGuest(CreateGuestRequest request) {
        return userMapper.toUserDto(createOrGetGuest(request.getEmail(),request.getPhone(),request.getName()));
    }


    private PageDto<UserDto> searchUsers(SearchUserRequest request, Specification<UserEntity> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<UserEntity> root = cq.from(UserEntity.class);

        // JOIN orders
        Join<Object, Object> orders = root.join("orders", JoinType.LEFT);
        orders.on(cb.equal(orders.get("status"), OrderStatus.COMPLETED));

        // Áp dụng filter từ Specification
        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) cq.where(predicate);

        // GROUP BY
        cq.groupBy(root.get("id"));

        // Tính toán
        Expression<Long> totalOrders = cb.count(orders.get("id"));
        Expression<BigDecimal> totalSpent = cb.sum(
                cb.coalesce(orders.get("totalAmount"), cb.literal(BigDecimal.ZERO))
        );

        // ORDER BY động
        boolean desc = "desc".equalsIgnoreCase(request.getDir());
        switch (request.getOrder()) {
            case "total_orders" -> cq.orderBy(desc ? cb.desc(totalOrders) : cb.asc(totalOrders));
            case "total_amount_spent" -> cq.orderBy(desc ? cb.desc(totalSpent) : cb.asc(totalSpent));
            default -> cq.orderBy(desc ? cb.desc(root.get("createdAt")) : cb.asc(root.get("createdAt")));
        }

        // SELECT
        cq.multiselect(
                root.get("id").alias("id"),
                root.get("name").alias("name"),
                root.get("email").alias("email"),
                root.get("phone").alias("phone"),
                root.get("isActive").alias("isActive"),
                root.get("createdAt").alias("createdAt"),
                totalOrders.alias("totalOrders"),
                totalSpent.alias("totalAmountSpent")
        );

        // PHÂN TRANG
        int page = request.getPage() - 1;
        int size = request.getSize();
        List<Tuple> tuples = em.createQuery(cq)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        // COUNT QUERY
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UserEntity> countRoot = countQuery.from(UserEntity.class);
        Predicate countPredicate = spec.toPredicate(countRoot, countQuery, cb);
        if (countPredicate != null) countQuery.where(countPredicate);
        countQuery.select(cb.countDistinct(countRoot));
        long total = em.createQuery(countQuery).getSingleResult();

        // MAP DTO
        List<UserDto> userDtos = tuples.stream().map(t -> UserDto.builder()
                .id(t.get("id", Long.class))
                .name(t.get("name", String.class))
                .email(t.get("email", String.class))
                .phone(t.get("phone", String.class))
                .isActive(t.get("isActive", Boolean.class))
                .createdAt(t.get("createdAt", LocalDateTime.class))
                .totalOrders(Optional.ofNullable(t.get("totalOrders", Long.class)).orElse(0L).intValue())
                .totalAmountSpent(Optional.ofNullable(t.get("totalAmountSpent", BigDecimal.class)).orElse(BigDecimal.ZERO))
                .build()).toList();

        Page<UserDto> pageResult = new PageImpl<>(userDtos, PageRequest.of(page, size), total);
        return new PageDto<>(pageResult);
    }

}
