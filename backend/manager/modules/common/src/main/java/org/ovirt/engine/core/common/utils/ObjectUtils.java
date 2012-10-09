package org.ovirt.engine.core.common.utils;

public class ObjectUtils {

    /**
     * Compares if two objects are equal, handling the cases where one of both of them may be null
     * @param obj1
     * @param obj2
     * @return
     */
    public static <T> boolean objectsEqual(T obj1, T obj2) {
        if(obj1 == null) {
            return obj2 == null;
        } else {
            return obj1.equals(obj2);
        }
    }

}
