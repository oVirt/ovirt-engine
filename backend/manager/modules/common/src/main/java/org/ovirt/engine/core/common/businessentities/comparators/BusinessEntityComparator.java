package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;

/**
 * Generic for implementing a business entity comparator based on ID comparison
 */
public class BusinessEntityComparator<T extends BusinessEntity<ID>, ID extends Serializable & Comparable<? super ID>> implements Comparator<T>, Serializable {

    @Override
    public int compare(T o1, T o2) {
        return o1.getId().compareTo(o2.getId());
    }

    public static <T extends BusinessEntity<ID>, ID extends Serializable & Comparable<? super ID>> BusinessEntityComparator<T, ID> newInstance() {
        return new BusinessEntityComparator<>();
    }
}
