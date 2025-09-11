package com.pbl6.services.impl;

import com.pbl6.dtos.response.LoginDto;
import com.pbl6.entities.RefreshTokenEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.RefreshTokenRepository;
import com.pbl6.repositories.UserRepositoty;
import com.pbl6.services.RefeshTokenService;
import com.pbl6.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefeshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepositoty userRepository;
    private final JwtUtil jwtUtil;

    @Value("${expiration_refresh}")
    private long expirationRefresh;

    @Override
    public String addRefreshToken(UserEntity user) {

        List<RefreshTokenEntity> refreshTokenEntityList = refreshTokenRepository.findByUserId(user.getId());

        if (refreshTokenEntityList.size()>=2) {
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
                    .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

            if (refreshTokenEntityOld.getExpiresAt().isBefore(LocalDateTime.now()) || refreshTokenEntityOld.getIsRevoked()) {
                throw new AppException(ErrorCode.TOKEN_INVALID);
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
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }
    }

}
