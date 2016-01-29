package com.dingding.milou.util;

import java.util.Collection;

import org.springframework.util.StringUtils;

/**
 * 对比StringUtils和CollectionUtils做类似的断言
 * 
 * @author al
 * 
 */
public abstract class Assert {

    public static void assertHasLength(CharSequence str) {
        if (str == null || str.length() == 0) {
            fail("[" + str + "] has no length,maybe null or length = 0");
        }
    }

    public static void assertHasLength(String str) {
        if (str == null || str.length() == 0) {
            fail("[" + str + "] has no length,maybe null or length = 0");
        }
    }

    public static void assertHasNoLength(CharSequence str) {
        if (str != null && str.length() > 0) {
            fail("[" + str + "] has length");
        }
    }

    public static void assertHasNoLength(String str) {
        if (str != null && str.length() > 0) {
            fail("[" + str + "] has length");
        }
    }

    public static void assertHasText(CharSequence str) {
        if (!StringUtils.hasText(str)) {
            fail("[" + str + "] has no text,see org.springframework.util.StringUtils.hasText(CharSequence)");
        }
    }

    public static void assertHasText(String str) {
        if (!StringUtils.hasText(str)) {
            fail("[" + str + "] has no text,see org.springframework.util.StringUtils.hasText(CharSequence)");
        }
    }

    public static void assertNotEmpty(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            fail("[colletion] is empty,see org.springframework.util.CollectionUtils.isEmpty(Collection<?>)");
        }
    }

    public static void assertEmpty(Collection<?> collection) {
        if (collection != null && collection.size() > 0) {
            fail("[colletion] is not empty,see org.springframework.util.CollectionUtils.isEmpty(Collection<?>)");
        }
    }

    static public void fail(String message) {
        if (message == null) {
            throw new AssertionError();
        }
        throw new AssertionError(message);
    }
}
