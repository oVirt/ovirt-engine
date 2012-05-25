package org.ovirt.engine.core.common.utils.gluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;

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

    /**
     * Returns a random number between zero and the given {@code maxNum}
     * @param maxNum
     * @return
     */
    public static int random(int maxNum) {
        return (int) (Math.random() * (maxNum + 1));
    }

    public static final List<String> getQualifiedBrickList(Collection<GlusterBrickEntity> bricks) {
        List<String> qualifiedBricks = new ArrayList<String>();
        for (GlusterBrickEntity GlusterBrick : bricks) {
            qualifiedBricks.add(GlusterBrick.getQualifiedName());
        }
        return qualifiedBricks;
    }
}
