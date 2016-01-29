package com.dingding.milou.dbunit.threadLocal;

public class MilouDBThreadLocal {

    private static ThreadLocal<Boolean> milouDBListener = new ThreadLocal<Boolean>();

    public static void setMilouDBListener(Boolean value) {
        milouDBListener.set(value);
    }

    public static Boolean getMilouDBListener() {
        Boolean b = milouDBListener.get();
        if (b == null) {
            return false;
        }
        return b;
    }

    public static void clearMilouDBListener() {
        milouDBListener.remove();
    }
}
