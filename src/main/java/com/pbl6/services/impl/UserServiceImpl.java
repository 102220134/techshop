package com.pbl6.services.impl;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.response.LoginDto;
import com.pbl6.entities.RoleEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.RoleEnum;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.UserMapper;
import com.pbl6.repositories.RoleRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.services.RefreshTokenService;
import com.pbl6.services.UserService;
import com.pbl6.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    @Override
    public void createUser(RegisterRequest registerRequest) {
        Optional<UserEntity> user = userRepository.findByPhone(registerRequest.getPhone());

        if (user.isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserEntity userEntity = userMapper.toUserEntity(registerRequest);

        String encodedPassword = passwordEncoder.encode(userEntity.getPassword());
        userEntity.setPassword(encodedPassword);

        RoleEntity roleEntity = roleRepository.findByName(RoleEnum.CUSTOMER.getRoleName())
                .orElseThrow(()-> new AppException(ErrorCode.DATA_NOT_FOUND));
        userEntity.setRole(roleEntity);
        userRepository.save(userEntity);
    }

    @Override
    public LoginDto login(LoginRequest loginRequest) {
        UserEntity userEntity = userRepository.findByPhone(loginRequest.getPhone())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        if(!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        LoginDto loginResponse = new LoginDto();

        loginResponse.setAccessToken(jwtUtil.generateToken(userEntity.getPhone()));
        String refreshToken =  refreshTokenService.addRefreshToken(userEntity);
        loginResponse.setRefreshToken(refreshToken);
        return loginResponse;
    }

    @Override
    public UserEntity loadUserByPhone(String phone) throws UsernameNotFoundException {
        return userRepository.findByPhone(phone).orElseThrow(() ->
                new UsernameNotFoundException("User not found with phone: " + phone));
    }

}
