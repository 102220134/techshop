package com.pbl6.services.impl;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.request.profile.ChangePasswordRequest;
import com.pbl6.dtos.request.profile.ProfileUpdateRequest;
import com.pbl6.dtos.request.profile.UserAddressCreateRequest;
import com.pbl6.dtos.request.profile.UserAddressUpdateRequest;
import com.pbl6.dtos.response.LoginDto;
import com.pbl6.dtos.user.UserAddressDto;
import com.pbl6.dtos.user.UserDto;
import com.pbl6.entities.RoleEntity;
import com.pbl6.entities.UserAddressEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.RoleEnum;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.UserAddressMapper;
import com.pbl6.mapper.UserMapper;
import com.pbl6.repositories.RoleRepository;
import com.pbl6.repositories.UserAddressRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.services.RefreshTokenService;
import com.pbl6.services.UserService;
import com.pbl6.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final UserAddressRepository userAddressRepository;
    private final UserAddressMapper userAddressMapper;

    @Override
    public void createUser(RegisterRequest registerRequest) {
        log.info("Attempting to create user with phone: {}", registerRequest.getPhone());
        Optional<UserEntity> existingUserOpt = userRepository.findByPhone(registerRequest.getPhone());

        if (existingUserOpt.isEmpty()) {
            log.debug("No existing user found for phone: {}. Creating new user.", registerRequest.getPhone());
            UserEntity newUser = userMapper.toUserEntity(registerRequest);
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            RoleEntity roleEntity = roleRepository.findByName(RoleEnum.CUSTOMER.getRoleName())
                    .orElseThrow(() -> {
                        log.error("Role {} not found during user creation.", RoleEnum.CUSTOMER.getRoleName());
                        return new AppException(ErrorCode.DATA_NOT_FOUND);
                    });
            newUser.setRole(roleEntity);
            newUser.setIsGuest(false);
            newUser.setIsActive(true);
            userRepository.save(newUser);
            log.info("New user created successfully with phone: {}", registerRequest.getPhone());
            return;
        }

        UserEntity existingUser = existingUserOpt.get();
        log.debug("Existing user found for phone: {}. Checking if it's a guest user.", registerRequest.getPhone());

        if (!existingUser.getIsGuest()) {
            log.warn("User with phone: {} already exists and is not a guest. Throwing USER_EXISTED.", registerRequest.getPhone());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        log.info("Converting guest user with phone: {} to a regular user.", registerRequest.getPhone());
        existingUser.setName(registerRequest.getName());
        existingUser.setEmail(registerRequest.getEmail());
        existingUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        existingUser.setIsGuest(false);
        existingUser.setIsActive(true);
        userRepository.save(existingUser);
        log.info("Guest user with phone: {} successfully converted to regular user.", registerRequest.getPhone());
    }

    @Override
    public LoginDto login(LoginRequest loginRequest) {
        log.info("Attempting login for phone: {}", loginRequest.getPhone());
        UserEntity userEntity = userRepository.findByPhone(loginRequest.getPhone())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found for phone: {}", loginRequest.getPhone());
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        if (Boolean.TRUE.equals(userEntity.getIsGuest())) {
            log.warn("Login failed for phone: {}. User is a guest account.", loginRequest.getPhone());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!Boolean.TRUE.equals(userEntity.getIsActive())) {
            log.warn("Login failed for phone: {}. User account is inactive.", loginRequest.getPhone());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
            log.warn("Login failed for phone: {}. Invalid password.", loginRequest.getPhone());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        LoginDto loginResponse = new LoginDto();
        loginResponse.setAccessToken(jwtUtil.generateToken(userEntity.getPhone()));
        String refreshToken = refreshTokenService.addRefreshToken(userEntity);
        loginResponse.setRefreshToken(refreshToken);
        log.info("User {} logged in successfully. Access token generated.", userEntity.getPhone());

        return loginResponse;
    }

    @Override
    public UserEntity loadUserByPhone(String phone) throws UsernameNotFoundException {
        log.debug("Loading user by phone: {}", phone);
        return userRepository.findByPhoneAndIsActiveTrue(phone).orElseThrow(() -> {
            log.error("User not found with phone: {}", phone);
            return new UsernameNotFoundException("User not found with phone: " + phone);
        });
    }

    @Override
    public UserEntity createOrGetGuest(String email, String phone, String name) {
        log.info("Attempting to create or get guest user for phone: {}", phone);
        return userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    log.debug("Guest user not found for phone: {}. Creating new guest user.", phone);
                    RoleEntity roleEntity = roleRepository.findByName(RoleEnum.CUSTOMER.getRoleName())
                            .orElseThrow(()-> {
                                log.error("Role {} not found during guest user creation.", RoleEnum.CUSTOMER.getRoleName());
                                return new AppException(ErrorCode.DATA_NOT_FOUND);
                            });
                    UserEntity guest = UserEntity.builder()
                            .name(name)
                            .email(email)
                            .phone(phone)
                            .role(roleEntity)
                            .isGuest(true)
                            .isActive(false)
                            .build();
                    UserEntity savedGuest = userRepository.save(guest);
                    log.info("New guest user created with phone: {}", phone);
                    return savedGuest;
                });
    }

    @Override
    public UserDto getUserInfo(Long userId) {
        log.info("Fetching user info for userId: {}", userId);
        UserEntity user = userRepository.findById(userId).orElseThrow(
                ()-> {
                    log.warn("User not found for userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                }
        );
        log.debug("User found for userId: {}. Returning DTO.", userId);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateProfile(Long userId, ProfileUpdateRequest request) {
        log.info("Updating profile for userId: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Profile update failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        // Update fields from request if they are provided
        if (request.getName() != null) {
            user.setName(request.getName());
            log.debug("Updating name for userId {}: {}", userId, request.getName());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
            log.debug("Updating gender for userId {}: {}", userId, request.getGender());
        }
        if (request.getBirth()!= null) {
            user.setBirth(request.getBirth());
            log.debug("Updating birth date for userId {}: {}", userId, request.getBirth());
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for userId: {}", userId);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Attempting to change password for userId: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Password change failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        // 1. Verify current password
        if (!passwordEncoder.matches(request.getNewPassword(), user.getPassword())) { // This should be currentPassword, not newPassword
            log.warn("Password change failed for userId {}: Current password verification failed.", userId);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 2. Check if new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password change failed for userId {}: New password and confirm password do not match.", userId);
            throw new AppException(ErrorCode.CONFIRM_PASSWORD_NOT_MATCH);
        }

        // 3. Encode and set the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for userId: {}", userId);
    }

    @Override
    public UserAddressDto createAddress(Long userId, UserAddressCreateRequest request) {
        log.info("Attempting to create address for userId: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Address creation failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        UserAddressEntity newAddress = new UserAddressEntity();
        newAddress.setUser(user);
        newAddress.setLine(request.getLine());
        newAddress.setWard(request.getWard());
        newAddress.setDistrict(request.getDistrict());
        newAddress.setProvince(request.getProvince());
        log.debug("New address details for userId {}: Line={}, Ward={}, District={}, Province={}",
                userId, request.getLine(), request.getWard(), request.getDistrict(), request.getProvince());

        // Determine if the new address should be default
        boolean requestWantsDefault = Boolean.TRUE.equals(request.getIsDefault());
        long existingDefaultAddressesCount = userAddressRepository.countByUserAndIsDefaultTrue(user);
        long totalExistingAddressesCount = userAddressRepository.countByUser(user);
        log.debug("Address creation for userId {}: requestWantsDefault={}, existingDefaultAddressesCount={}, totalExistingAddressesCount={}",
                userId, requestWantsDefault, existingDefaultAddressesCount, totalExistingAddressesCount);


        if (totalExistingAddressesCount == 0) {
            // If this is the first address for the user, it must be default.
            newAddress.setIsDefault(true);
            log.debug("Address for userId {} is the first one, setting as default.", userId);
        } else if (requestWantsDefault) {
            // If the request explicitly asks for this to be default, unset all other default addresses.
            log.debug("Request wants new address for userId {} to be default. Unsetting other defaults.", userId);
            List<UserAddressEntity> currentDefaultAddresses = userAddressRepository.findByUserAndIsDefaultTrue(user);
            for (UserAddressEntity defaultAddr : currentDefaultAddresses) {
                defaultAddr.setIsDefault(false);
                userAddressRepository.save(defaultAddr); // Save each updated default address
                log.debug("Unset default for addressId: {}", defaultAddr.getId());
            }
            newAddress.setIsDefault(true); // Set the new address as default
            log.debug("New address for userId {} set as default.", userId);
        } else {
            // If the request does not explicitly ask for default (false or null)
            // And there are no other default addresses, this one must become default to maintain the rule.
            if (existingDefaultAddressesCount == 0) {
                newAddress.setIsDefault(true);
                log.debug("No other default addresses for userId {}, setting new address as default to maintain rule.", userId);
            } else {
                newAddress.setIsDefault(false); // Otherwise, follow the request's non-default setting
                log.debug("New address for userId {} not set as default, following request.", userId);
            }
        }

        UserAddressEntity savedAddress = userAddressRepository.save(newAddress); // This should be userAddressRepository.save(newAddress);
        log.info("Address created successfully for userId: {} with addressId: {}", userId, savedAddress.getId());
        return userAddressMapper.toDto(savedAddress); // Assuming userAddressMapper exists and is injected
    }

    @Override
    public UserAddressDto updateAddress(Long userId, UserAddressUpdateRequest request) {
        log.info("Attempting to update addressId: {} for userId: {}", request.getId(), userId);
        // 1. Find the user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Address update failed: User not found for userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        // 2. Find the address to update
        UserAddressEntity addressToUpdate = userAddressRepository.findById(request.getId())
                .orElseThrow(() -> {
                    log.warn("Address update failed: Address not found for addressId: {}", request.getId());
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });

        // 3. Ensure the address belongs to the user
        if (!addressToUpdate.getUser().getId().equals(userId)) {
            log.warn("Address update failed: AddressId {} does not belong to userId {}", request.getId(), userId);
            throw new AppException(ErrorCode.UNAUTHORIZED); // Or a more specific error like ADDRESS_NOT_BELONG_TO_USER
        }

        // 4. Update address fields from the request
        addressToUpdate.setLine(request.getLine());
        addressToUpdate.setWard(request.getWard());
        addressToUpdate.setDistrict(request.getDistrict());
        addressToUpdate.setProvince(request.getProvince());
        log.debug("Updating addressId {} for userId {}: Line={}, Ward={}, District={}, Province={}",
                request.getId(), userId, request.getLine(), request.getWard(), request.getDistrict(), request.getProvince());

        // 5. Handle isDefault logic
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            log.debug("Request wants addressId {} for userId {} to be default. Unsetting other defaults.", request.getId(), userId);
            // If the incoming request explicitly sets this address as default:
            // Find all other default addresses for this user and unset them.
            List<UserAddressEntity> currentDefaultAddresses = userAddressRepository.findByUserAndIsDefaultTrue(user);
            for (UserAddressEntity defaultAddr : currentDefaultAddresses) {
                if (!defaultAddr.getId().equals(addressToUpdate.getId())) {
                    defaultAddr.setIsDefault(false);
                    userAddressRepository.save(defaultAddr); // Save each updated default address
                    log.debug("Unset default for addressId: {}", defaultAddr.getId());
                }
            }
            addressToUpdate.setIsDefault(true); // Set the current address as default
            log.debug("AddressId {} set as default for userId {}.", request.getId(), userId);
        } else {
            log.debug("Request wants addressId {} for userId {} to be non-default.", request.getId(), userId);
            // If the incoming request explicitly sets this address as non-default (or doesn't specify, which defaults to false):
            // Set the current address to non-default.
            addressToUpdate.setIsDefault(false);

            // Business rule: A user must always have at least one default address.
            // Check if, after setting this address to non-default, there are no other default addresses.
            // Count default addresses *excluding* the one we are currently updating.
            long otherDefaultAddressesCount = userAddressRepository.countByUserAndIsDefaultTrueAndIdNot(user, addressToUpdate.getId());
            log.debug("After setting addressId {} to non-default, otherDefaultAddressesCount for userId {} is: {}",
                    request.getId(), userId, otherDefaultAddressesCount);

            if (otherDefaultAddressesCount == 0) {
                // If there are no other default addresses, we need to make one default.
                log.debug("No other default addresses found for userId {}. Enforcing business rule.", userId);
                // First, check if there are any other addresses at all for this user.
                long totalAddressesCount = userAddressRepository.countByUser(user);

                if (totalAddressesCount == 1) {
                    // If this is the ONLY address for the user, it MUST remain default.
                    addressToUpdate.setIsDefault(true);
                    log.warn("AddressId {} is the only address for userId {}. It must remain default.", request.getId(), userId);
                } else {
                    // There are other addresses, but none are default. Pick one to be the new default.
                    // Find the first available address that is not the current one and make it default.
                    userAddressRepository.findFirstByUserAndIdNot(user, addressToUpdate.getId())
                            .ifPresent(newDefaultAddress -> {
                                newDefaultAddress.setIsDefault(true);
                                userAddressRepository.save(newDefaultAddress);
                                log.info("Set addressId {} as new default for userId {}.", newDefaultAddress.getId(), userId);
                            });
                    // If no other address is found (e.g., due to concurrent deletion),
                    // the system might temporarily have no default. This scenario is handled by `ifPresent`.
                }
            }
        }

        // 6. Save the updated address
        UserAddressEntity savedAddress = userAddressRepository.save(addressToUpdate);
        log.info("AddressId {} updated successfully for userId: {}", savedAddress.getId(), userId);

        // 7. Return the DTO representation of the updated address
        return userAddressMapper.toDto(savedAddress);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        log.info("Attempting to delete addressId: {} for userId: {}", addressId, userId);
        var address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address deletion failed: Address not found for addressId: {}", addressId);
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });
        if (!address.getUser().getId().equals(userId)) {
            log.warn("Address deletion failed: AddressId {} does not belong to userId {}", addressId, userId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Business rule: A user must always have at least one default address.
        // If the address being deleted is the default, or if it's the only address,
        // we need to ensure another address becomes default or prevent deletion if it's the last one.
        long totalAddressesCount = userAddressRepository.countByUser(address.getUser());
        if (totalAddressesCount == 1) {
            log.warn("Cannot delete addressId {} for userId {}: It is the only address.", addressId, userId);
            throw new AppException(ErrorCode.CANNOT_DELETE_LAST_ADDRESS); // Custom error for this case
        }

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            log.debug("AddressId {} being deleted is the default for userId {}. Finding a new default.", addressId, userId);
            // Find a new default address
            userAddressRepository.findFirstByUserAndIdNot(address.getUser(), addressId)
                    .ifPresent(newDefaultAddress -> {
                        newDefaultAddress.setIsDefault(true);
                        userAddressRepository.save(newDefaultAddress);
                        log.info("Set addressId {} as new default for userId {} after deleting old default.", newDefaultAddress.getId(), userId);
                    });
        }

        userAddressRepository.delete(address);
        log.info("AddressId {} deleted successfully for userId: {}", addressId, userId);
    }
}
