package com.pbl6.services;

import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.request.user.*;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.user.UserAddressDto;
import com.pbl6.dtos.user.UserDetailDto;
import com.pbl6.dtos.user.UserDto;
import com.pbl6.entities.UserEntity;

public interface UserService {
    UserDto createUser(RegisterRequest registerRequest);
    UserEntity loadUserByPhone(String phone);
    UserEntity createOrGetGuest(String email, String phone, String name);
    UserDetailDto getUserInfo(Long userId);
    UserDetailDto updateUserInfo(Long userId, UserUpdateInfoRequest request);
    UserDetailDto updateUserRole(Long userId, UserUpdateRoleRequest request);
    UserDetailDto updateUserStatus(Long userId, UserUpdateStatusRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    UserAddressDto createAddress(Long userId, UserAddressCreateRequest request);
    UserAddressDto updateAddress(Long userId, UserAddressUpdateRequest request);
    void deleteAddress(Long userId, Long addressId);
//    void deleteUser(Long userId);
    PageDto<UserDto> searchCustomers(SearchUserRequest request);
    PageDto<UserDto> searchStaffs(SearchUserRequest request);
    UserDetailDto getUserInfoByPhone(String phone);
}
