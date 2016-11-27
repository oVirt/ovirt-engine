package org.ovirt.engine.ui.uicommonweb.models.vms.register;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;

class VnicProfileMappingItemComparator implements Comparator<VnicProfileMappingItem> {
    private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

    @Override
    public int compare(VnicProfileMappingItem item1, VnicProfileMappingItem itrem2) {
        int retVal = lexoNumeric.compare(item1.getEntity().getExternalNetworkName(),
                itrem2.getEntity().getExternalNetworkName());

        return retVal == 0 ? lexoNumeric.compare(item1.getEntity().getExternalNetworkProfileName(),
                itrem2.getEntity().getExternalNetworkProfileName()) : retVal;
    }
}
