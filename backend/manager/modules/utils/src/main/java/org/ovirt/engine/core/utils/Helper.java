package org.ovirt.engine.core.utils;

public final class Helper {
    public static <T> java.util.ArrayList<T> ToList(Iterable<T> inList) {
        java.util.ArrayList<T> outList = new java.util.ArrayList<T>();
        for (T item : inList) {
            outList.add(item);
        }
        return outList;
    }
}
