package com.pbl6.services;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.request.profile.ChangePasswordRequest;
import com.pbl6.dtos.request.profile.ProfileUpdateRequest;
import com.pbl6.dtos.request.profile.UserAddressCreateRequest;
import com.pbl6.dtos.request.profile.UserAddressUpdateRequest;
import com.pbl6.dtos.response.LoginDto;
import com.pbl6.dtos.user.UserAddressDto;
import com.pbl6.dtos.user.UserDto;
import com.pbl6.entities.UserEntity;

public interface UserService {
    void createUser(RegisterRequest registerRequest);
    LoginDto login(LoginRequest loginRequest);
    UserEntity loadUserByPhone(String phone);
    UserEntity createOrGetGuest(String email, String phone, String name);
    UserDto getUserInfo(Long userId);
    UserDto updateProfile(Long userId, ProfileUpdateRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    UserAddressDto createAddress(Long userId, UserAddressCreateRequest request);
    UserAddressDto updateAddress(Long userId, UserAddressUpdateRequest request);
    void deleteAddress(Long userId, Long addressId);

}
