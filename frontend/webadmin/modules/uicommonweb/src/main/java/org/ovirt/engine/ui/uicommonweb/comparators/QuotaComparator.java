package org.ovirt.engine.ui.uicommonweb.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;

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
}
