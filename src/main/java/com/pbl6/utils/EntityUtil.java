package com.pbl6.utils;

import com.pbl6.entities.Activatable;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class EntityUtil {
    public void ensureActive(Activatable entity, boolean includeInactive) {
        if (!includeInactive && !Boolean.TRUE.equals(entity.getIsActive())) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
    }
    public <T> T ensureExists(Optional<T> optionalEntity) {
        return optionalEntity.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }
    public <T> T ensureExists(Optional<T> optionalEntity,String message) {
        return optionalEntity.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,message));
    }
}
