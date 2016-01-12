package com.dingding.milou.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dingding.milou.repo.StubInfoRepo;
import com.dingding.milou.repo.StubObjectRepo;
import com.dingding.milou.situation.SituationParser;
import com.dingding.milou.util.ArrayUtils;

public class MockMethodInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MockMethodInterceptor.class);

    // 当前被代理对象在spring中的beanId
    private String beanId;

    public MockMethodInterceptor() {
    }

    public MockMethodInterceptor(String beanId) {
        this.beanId = beanId;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String stubId = getStubId(invocation);
        if (stubId == null) {
            return callTargetMethod(invocation);
        }
        // 桩实例
        Object stubObject = getStubObject(stubId);
        // 桩的Method对象
        Method stubMethod = getStubMethod(stubId);
        if (stubObject != null && stubMethod != null) {
            Object[] args = checkMethodArgs(stubMethod, invocation.getArguments());
            return stubMethod.invoke(stubObject, args);
        }
        return callTargetMethod(invocation);
    }

    /**
     * 执行目标对象的方法
     * 
     * @param invocation
     * @return 目标方法返回结果
     * @throws Throwable 目标方法执行过程中可能抛出的异常
     */
    private Object callTargetMethod(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (InvocationTargetException e) {
            logger.error("target method invoke error,e={}", e.getTargetException());
            throw e.getTargetException();
        }
    }

    /**
     * 根据被代理的类名和方法名的拼接结果从StubInfoRepo获取stubId
     * 
     * @param invocation
     * @return String stubId
     */
    private String getStubId(MethodInvocation invocation) {
        Method targetMethod = invocation.getMethod();
        Class<?>[] paramType = targetMethod.getParameterTypes();
        String methodName = targetMethod.getName();
        String stubId = StubInfoRepo.getStubId(SituationParser.createClassMethodExp(beanId, methodName));
        if (stubId == null && ArrayUtils.isNotEmpty(paramType)) {
            stubId = StubInfoRepo.getStubId(SituationParser.createClassMethodExp(beanId, methodName, paramType));
        }
        if (stubId == null) {
            logger.warn("beanId={},method={}的方法没有对应的桩stubId，需要检查是否需要stub,如果需要stub,请检查@Situation的书写格式", beanId,
                    methodName);
        }
        return stubId;
    }

    /**
     * 根据stubId获取桩对象
     * 
     * @param stubId
     * @return
     * @throws Exception
     */
    private Object getStubObject(String stubId) throws Exception {
        Object stub = StubObjectRepo.getStubObject(stubId);
        if (stub == null) {
            throw new Exception("没有id为【" + stubId + "】的桩的Object对象");
        }
        return stub;
    }

    /**
     * 根据stubId获取桩的Method对象
     * 
     * @param stubId 桩id
     * @return
     */
    private Method getStubMethod(String stubId) {
        Method method = StubInfoRepo.getStubMethod(stubId);
        if (method == null) {
            throw new RuntimeException("没有id为【" + stubId + "】的桩的Method对象");
        }
        return method;
    }

    /**
     * 检查桩方法形参个数；类型由底层代理检查
     * 
     * @param stubMethod
     * @param args
     * @return Object[] 桩方法入参的参数数组
     */
    private Object[] checkMethodArgs(Method stubMethod, Object...args) {
        Class<?>[] stubMethodParam = stubMethod.getParameterTypes();
        // 允许桩方法无参
        if (ArrayUtils.isEmpty(stubMethodParam)) {
            return new Object[] {};
        }
        // 只要桩方法有参，就必须和目标方法形参个数一致。
        if (stubMethodParam.length != args.length) {
            throw new RuntimeException("桩方法的形参个数和被代理的目标方法形参个数不一致");
        }
        return args;
    }

}
