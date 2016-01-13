package com.dingding.milou.stub;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Stub {
    /**
     * 桩数据id
     */
    String value() default DEFAULT;

    String DEFAULT = "null";
}
