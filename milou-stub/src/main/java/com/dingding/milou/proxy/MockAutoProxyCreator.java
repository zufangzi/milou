package com.dingding.milou.proxy;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

/**
 * Spring实现mock对象的aop
 * 
 * @author al
 * 
 */
@SuppressWarnings("serial")
@Component
public class MockAutoProxyCreator extends AbstractAutoProxyCreator {

    private static final Logger logger = LoggerFactory.getLogger(MockAutoProxyCreator.class);

    private static Set<String> BEAN_ID_SET = new HashSet<String>();

    public MockAutoProxyCreator() {
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource)
            throws BeansException {
        if (BEAN_ID_SET.contains(beanName)) {
            logger.info("add proxy for 【{}】", beanClass.getName());
            return new Object[] { new MockMethodInterceptor(beanName) };
        }
        return DO_NOT_PROXY;
    }

    public static void putIntoBeanIdSet(String beanId) {
        BEAN_ID_SET.add(beanId);
    }

    public static void clear() {
        BEAN_ID_SET.clear();
    }
}
