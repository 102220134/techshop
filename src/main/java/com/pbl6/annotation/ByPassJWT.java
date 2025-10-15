package com.pbl6.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ByPassJWT {
    // Không cần thêm field, chỉ là marker annotation
    boolean optional() default false;
}
