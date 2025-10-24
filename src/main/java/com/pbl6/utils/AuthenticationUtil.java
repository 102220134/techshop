package com.pbl6.utils;

import com.pbl6.entities.UserEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationUtil {
    public Long getCurrentUserId() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            return user.getId();
        }
        catch (Exception e){
            log.warn("Không thể lấy user từ SecurityContextHolder");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
    public UserEntity getCurrentUser() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            return user;
        }
        catch (Exception e){
            log.warn("Không thể lấy user từ SecurityContextHolder");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}
