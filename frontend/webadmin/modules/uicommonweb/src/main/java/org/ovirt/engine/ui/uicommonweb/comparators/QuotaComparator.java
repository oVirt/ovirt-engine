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
    public static final Comparator<Quota> DESCRIPTION = new Comparator<Quota>() {
        @Override
        public int compare(Quota quota1, Quota quota2) {
            return lexoNumeric.compare(quota1.getDescription(), quota2.getDescription());
        }
    };

    /**
     * Comparator for the quota name field in {@code Quota}.
     */
    public static final Comparator<Quota> NAME = new Comparator<Quota>() {
        @Override
        public int compare(Quota quota1, Quota quota2) {
            return lexoNumeric.compare(quota1.getQuotaName(), quota2.getQuotaName());
        }
    };

    /**
     * Comparator for the description field in {@code Quota}.
     */
    public static final Comparator<Quota> DATA_CENTER = new Comparator<Quota>() {
        @Override
        public int compare(Quota quota1, Quota quota2) {
            return lexoNumeric.compare(quota1.getStoragePoolName(), quota2.getStoragePoolName());
        }
    };

    /**
     * Comparator adapter. Quota with topId will be the first in sorted list.
     */
    public static Comparator<Quota> withTopId(final Guid topId, final Comparator<Quota> defaultComparator) {
        return new Comparator<Quota>() {
            @Override
            public int compare(Quota quota1, Quota quota2) {
                boolean top1 = quota1.getId().equals(topId);
                boolean top2 = quota2.getId().equals(topId);

                if (top1 && !top2) {
                    return -1;
                }
                if (top2 && !top1) {
                    return 1;
                }
                return defaultComparator.compare(quota1, quota2);
            }
        };
    }
}
