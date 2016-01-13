package com.dingding.milou.repo;

import java.util.HashMap;
import java.util.Map;

/**
 * 维护桩对象的池子，{@code stubRepo}的key是桩id，value是桩所在的实例对象。
 * 
 * @author al
 * 
 */
public class StubObjectRepo {

    private static Map<String, Object> stubRepo = new HashMap<String, Object>();

    private StubObjectRepo() {
    }

    public static Map<String, Object> getStubRepo() {
        return stubRepo;
    }

    /**
     * 根据stubId获取其实例对象
     * 
     * @param stubId
     * @return
     * @throws Exception
     */
    public static Object getStubObject(String stubId) throws Exception {
        Object stubObject = stubRepo.get(stubId);
        if (stubObject == null) {
            Class<?> stubClass = getClassFromStubInfoRepo(stubId);
            if (stubClass != null) {
                stubObject = newStubInstance(stubClass);
                putIntoStubRepo(stubId, stubObject);
            }
        }
        return stubObject;
    }

    private static Class<?> getClassFromStubInfoRepo(String stubId) {
        return StubInfoRepo.getStubClass(stubId);
    }

    private static Object newStubInstance(Class<?> stubClass) throws Exception {
        return stubClass.newInstance();
    }

    private static void putIntoStubRepo(String key, Object value) {
        stubRepo.put(key, value);
    }

    public static void clear() {
        stubRepo.clear();
    }
}
