package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public class BusinessEntityGuidComparator<T extends BusinessEntity<Guid>> extends BusinessEntityComparator<T, Guid> {

    public static <T extends BusinessEntity<Guid>> BusinessEntityGuidComparator<T> newInstance() {
        return new BusinessEntityGuidComparator<T>();
    }


}
