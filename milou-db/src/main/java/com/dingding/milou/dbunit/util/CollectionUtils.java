package com.dingding.milou.dbunit.util;

import java.util.Collection;
import java.util.Iterator;

public abstract class CollectionUtils {

    public static <E> boolean isEmpty(Collection<E> collection) {
        if (collection == null || collection.isEmpty()) {
            return true;
        }
        Iterator<E> it = collection.iterator();
        while (it.hasNext()) {
            E type = (E) it.next();
            if (type != null) {
                return false;
            }
        }
        return true;
    }

}
