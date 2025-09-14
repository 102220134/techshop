package com.pbl6.utils;

import com.pbl6.entities.Activatable;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EntityUtil {
    public void ensureActive(Activatable entity, boolean includeInactive) {
        if (!includeInactive && !Boolean.TRUE.equals(entity.getIsActive())) {
            throw new AppException(ErrorCode.DATA_NOT_FOUND);
        }
    }
    public <T> T ensureExists(Optional<T> optionalEntity) {
        return optionalEntity.orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
    }
}
