package com.dingding.milou.situation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Situation {
    /**
     * id 指明依赖的方法需要路由到哪一个桩的id
     */
    String StubId();

    /**
     * 被mock的目标对象 格式写法： 1、Class="beanId:xxxx" beanId是被mock对象在spring中的beanId； 默认可以不写"beanId"
     */
    String Class();

    /**
     * 被mock的目标方法 格式写法： Method="name:xxxx",xxx是方法名，默认可以不写"name";如果方法名无法确定唯一性，即有重载情况，采用：
     * Method="name:xxxx;paramType=aaa,bbbb", xxx是方法名，aaa和bbb分别是方法参数的类型的getSimpleName()的返回值；按照参数顺序，有多少个参数，有多少个类型。
     */
    String Method();
}
