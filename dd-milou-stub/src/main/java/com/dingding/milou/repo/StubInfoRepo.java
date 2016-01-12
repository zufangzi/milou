package com.dingding.milou.repo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.dingding.milou.stub.StubInfo;

/**
 * 桩信息仓库
 * 
 */
public class StubInfoRepo {
    // key是stubId
    private static Map<String, StubInfo> stubInfoRepo = new HashMap<String, StubInfo>();
    // key是@situation中Class和Method的联合表达式，value是stubId
    private static Map<String, String> stubIdMap = new HashMap<String, String>();

    private StubInfoRepo() {
    }

    public static Map<String, StubInfo> getStubInfoRepo() {
        return stubInfoRepo;
    }

    public static StubInfo getStubInfo(String stubId) {
        return stubInfoRepo.get(stubId);
    }

    public static void setIntoStubInfoRepo(String stubId, StubInfo info) {
        stubInfoRepo.put(stubId, info);
    }

    public static void setIntoStubInfoRepo(Map<String, StubInfo> map) {
        stubInfoRepo.putAll(map);
    }

    public static String getStubId(String jointExp) {
        return stubIdMap.get(jointExp);
    }

    public static void setIntoStubIdMap(String k, String v) {
        stubIdMap.put(k, v);
    }

    public static Class<?> getStubClass(String stubId) {
        StubInfo info = stubInfoRepo.get(stubId);
        if (info != null) {
            return info.getStubClass();
        }
        return null;
    }

    public static Method getStubMethod(String stubId) {
        StubInfo stubInfo = stubInfoRepo.get(stubId);
        if (stubInfo != null) {
            return stubInfo.getStubMethod();
        }
        return null;
    }

    public static void clearStubInfoRepo() {
        stubInfoRepo.clear();
    }

    public static void clearStubIdMap() {
        stubIdMap.clear();
    }

}
