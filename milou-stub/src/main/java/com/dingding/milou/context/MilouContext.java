package com.dingding.milou.context;

import java.lang.reflect.Method;

public interface MilouContext {

    /**
     * 获取当前测试类的全限定名
     * 
     * @return
     */
    Class getTestClassName();

    /**
     * 获取当前测试的方法
     * 
     * @return
     */
    Method getTestMethodName();

}
