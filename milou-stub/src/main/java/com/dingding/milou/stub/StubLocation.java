package com.dingding.milou.stub;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface StubLocation {
    /**
     * 桩的存放路径，递归扫描这个路径下的全部桩类。
     */
    String value() default LOCAL;

    String LOCAL = "local";
}
