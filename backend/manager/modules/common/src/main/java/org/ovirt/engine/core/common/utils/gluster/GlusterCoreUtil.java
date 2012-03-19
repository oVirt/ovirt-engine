package org.ovirt.engine.core.common.utils.gluster;

import java.util.Collection;

public class GlusterCoreUtil {
    /**
     * Compares two lists for equality of all their elements. Returns true if both lists are of same size and every
     * element in first list has an equal in the second.
     *
     * @param firstList
     * @param secondList
     * @return
     */
    public static <T> boolean listsEqual(Collection<T> firstList, Collection<T> secondList) {
        if(firstList.size() != secondList.size()) {
            return false;
        }

        for(T entity : firstList) {
            if(!secondList.contains(entity)) {
                return false;
            }
        }

        return true;
    }
}
