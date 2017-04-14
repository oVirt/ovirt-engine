package org.ovirt.engine.ui.uicommonweb.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.compat.Guid;

public class QuotaComparator {
    private static final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

    /**
     * Comparator for the description field in {@code Quota}.
     */
    public static final Comparator<Quota> DESCRIPTION = Comparator.comparing(Quota::getDescription, lexoNumeric);

    /**
     * Comparator for the quota name field in {@code Quota}.
     */
    public static final Comparator<Quota> NAME = Comparator.comparing(Quota::getName, lexoNumeric);

    /**
     * Comparator for the description field in {@code Quota}.
     */
    public static final Comparator<Quota> DATA_CENTER = Comparator.comparing(Quota::getStoragePoolName, lexoNumeric);

    /**
     * Comparator adapter. Quota with topId will be the first in sorted list.
     */
    public static Comparator<Quota> withTopId(final Guid topId, final Comparator<Quota> defaultComparator) {
        return Comparator.comparing((Quota q) -> !q.getId().equals(topId)).thenComparing(defaultComparator);
    }
}
