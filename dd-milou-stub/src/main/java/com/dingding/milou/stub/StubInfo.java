package com.dingding.milou.stub;

import java.lang.reflect.Method;

/**
 * 桩信息，封装了桩id，桩所在的Class对象，桩的Method对象
 * 
 * @author al
 * 
 */
public class StubInfo {
    // 桩的方法id
    private String stubId;
    // 桩的类Class
    private Class<?> stubClass;
    // 桩的方法Method对象
    private Method stubMethod;

    public String getStubId() {
        return stubId;
    }

    public void setStubId(String stubId) {
        this.stubId = stubId;
    }

    public Class<?> getStubClass() {
        return stubClass;
    }

    public void setStubClass(Class<?> stubClass) {
        this.stubClass = stubClass;
    }

    public Method getStubMethod() {
        return stubMethod;
    }

    public void setStubMethod(Method stubMethod) {
        this.stubMethod = stubMethod;
    }
}
