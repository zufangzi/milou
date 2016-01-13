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
public @interface DBSetupSituation {
    /**
     * 测试方法执行之前，对数据库进行数据建立操作，默认选择的数据源是"dbUnitDatabaseConnection".
     * 
     * @return connection 数据源连接
     */
    String connection() default "";

    /**
     * 
     * 1、value可以是"classpath:xxx.sql",测试方法执行之前，执行该sql文件，多个sql文件，用逗号分隔;
     * 2、value可以是"xxx.xml",原有的DBUnit采用xml形式储存数据，此情况是为了保留底层的框架功能。
     * 
     * @return
     * 
     */
    String[] value();
}
