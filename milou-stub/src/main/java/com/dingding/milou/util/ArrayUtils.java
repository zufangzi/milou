package com.dingding.milou.util;

import org.apache.poi.ss.formula.functions.T;

public class ArrayUtils {

    private ArrayUtils() {
    }

    public static boolean isEmpty(Class<?>[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(Class<?>[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isEmptyGeneric(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmptyGeneric(T[] array) {
        return array != null && array.length > 0;
    }
}
