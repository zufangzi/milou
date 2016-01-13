package com.dingding.milou.dbunit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DBSituations {
    /**
     * 测试方法执行之前，数据库相关使用场景，例如插入本测试特有的数据；
     * 
     * @return
     */
    DBSetupSituation[] setup() default {};

    /**
     * 测试方法执行之后，数据库相关使用场景，例如还原表数据；
     * 
     * @return
     */
    DBTeardownSituation[] teardown() default {};
}
