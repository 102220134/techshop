package com.pbl6.services.impl;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.response.auth.LoginDto;
import com.pbl6.dtos.response.auth.UserLogin;
import com.pbl6.entities.PermissionEntity;
import com.pbl6.entities.RefreshTokenEntity;
import com.pbl6.entities.RoleEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.RefreshTokenRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.services.AuthService;
import com.pbl6.services.EmailService;
import com.pbl6.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.expiration.refresh}")
    private long expirationRefresh;

    @Override
    public String addRefreshToken(UserEntity user) {

        List<RefreshTokenEntity> refreshTokenEntityList = refreshTokenRepository.findByUserId(user.getId());

        if (refreshTokenEntityList.size() >= 2) {
            RefreshTokenEntity refreshTokenEntity = refreshTokenEntityList.get(0);
            refreshTokenRepository.delete(refreshTokenEntity);
        }
        String refreshToken = jwtUtil.generateRefreshTokenRaw();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(expirationRefresh));
        refreshTokenRepository.save(refreshTokenEntity);
        return refreshToken;
    }

    @Override
    public LoginDto refreshToken(String oldRefreshToken) {
        try {
            RefreshTokenEntity refreshTokenEntityOld = refreshTokenRepository.findByToken(oldRefreshToken)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

            if (refreshTokenEntityOld.getExpiresAt().isBefore(LocalDateTime.now()) || refreshTokenEntityOld.getIsRevoked()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            String refreshToken = jwtUtil.generateRefreshTokenRaw();

            RefreshTokenEntity refreshTokenEntityNew = new RefreshTokenEntity();
            refreshTokenEntityNew.setUser(refreshTokenEntityOld.getUser());
            refreshTokenEntityNew.setToken(refreshToken);
            refreshTokenEntityNew.setExpiresAt(LocalDateTime.now().plusSeconds(expirationRefresh));

            refreshTokenRepository.delete(refreshTokenEntityOld);
            refreshTokenRepository.save(refreshTokenEntityNew);

            String accessToken = jwtUtil.generateToken(refreshTokenEntityOld.getUser().getPhone());

            LoginDto loginResponse = new LoginDto();
            loginResponse.setAccessToken(accessToken);
            loginResponse.setRefreshToken(refreshToken);

            return loginResponse;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public void resetPasswordAndSend(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmailAndIsActive(email,true);
        if (userOpt.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND,"Email không tồn tại");
        }

        UserEntity user = userOpt.get();

        if(user.getIsGuest()){
            throw new AppException(ErrorCode.NOT_FOUND,"Email không tồn tại");
        }

        // Tạo mật khẩu ngẫu nhiên
        String newPassword = generateRandomPassword(8);

        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Gửi email thông báo
        String subject = "Mật khẩu mới của bạn";
        String text = "Xin chào,\n\nMật khẩu mới của bạn là: " + newPassword +
                      "\n\nVui lòng đăng nhập và đổi mật khẩu ngay sau khi vào hệ thống.\n\nTrân trọng!";
        emailService.resetPassword(email, subject, text);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public LoginDto login(LoginRequest loginRequest) {
        UserEntity userEntity = userRepository.findByPhone(loginRequest.getPhone())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found for phone: {}", loginRequest.getPhone());
                    return new AppException(ErrorCode.UNAUTHORIZED,"User not found");
                });

        if (Boolean.TRUE.equals(userEntity.getIsGuest())) {
            log.warn("Login failed for phone: {}. User is a guest account.", loginRequest.getPhone());
            throw new AppException(ErrorCode.UNAUTHORIZED,"User not found");
        }

        if (!Boolean.TRUE.equals(userEntity.getIsActive())) {
            log.warn("Login failed for phone: {}. User account is inactive.", loginRequest.getPhone());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
            log.warn("Login failed for phone: {}. Invalid password.", loginRequest.getPhone());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        LoginDto loginResponse = new LoginDto();
        loginResponse.setAccessToken(jwtUtil.generateToken(userEntity.getPhone()));
        String refreshToken = addRefreshToken(userEntity);
        loginResponse.setRefreshToken(refreshToken);

        UserLogin userLogin = new UserLogin();
        userLogin.setId(userEntity.getId());
        userLogin.setName(userEntity.getName());
        userLogin.setRoles(userEntity.getRoles().stream().map(RoleEntity::getName).toList());
        userLogin.setPermissions(userEntity.getRoles().stream().flatMap(role->{
            Set<String> permissions = role.getPermissions().stream()
                    .map(PermissionEntity::getName)
                    .collect(Collectors.toSet());
            return permissions.stream();
        }).toList());
        loginResponse.setUserInfo(userLogin);

        return loginResponse;
    }


}
