package com.dingding.milou.scanner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.dingding.milou.stub.Stub;
import com.dingding.milou.stub.StubInfo;

/**
 * 桩信息扫描器
 * 
 * @author al
 * 
 */
public class StubInfoScanner {

    private StubInfoScanner() {

    }

    /**
     * 扫描给定的List中全部的Class
     * 
     * @param scanList 扫描list
     * @param map 将扫描好的桩信息放入指定的集合
     */
    public static void scanAllStubClass(List<Class<?>> scanList, Map<String, StubInfo> map) {
        if (CollectionUtils.isEmpty(scanList)) {
            throw new RuntimeException("no classes to be scan from given path,see @StubLocation at TestClass");
        }
        for (Class<?> cls : scanList) {
            scanClassStubInfo(cls, map);
        }
    }

    /**
     * 扫描给定的桩的Class
     * 
     * @param cls 给定的Class
     * @param map 将扫描好的桩信息放入指定的集合
     */
    private static void scanClassStubInfo(Class<?> cls, Map<String, StubInfo> map) {
        if (cls == null) {
            throw new RuntimeException("待扫描的Class对象为null,Class=" + cls.getName());
        }
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            Stub stub = method.getAnnotation(Stub.class);
            if ((stub != null) && (!stub.value().equals(Stub.DEFAULT))) {
                String stubId = stub.value();
                if (map.containsKey(stubId)) {
                    throw new RuntimeException("same stubId,stubId=" + stubId);
                }
                StubInfo info = new StubInfo();
                info.setStubId(stubId);
                info.setStubClass(cls);
                info.setStubMethod(method);
                map.put(stubId, info);
            }
        }
    }
}
