package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class HostDeviceListModel extends HostDeviceListModelBase<VDS> {
    @Override
    protected String getListName() {
        return "HostDeviceListModel"; //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        if (getEntity() != null) {
            syncSearch(VdcQueryType.GetExtendedHostDevicesByHostId, new IdQueryParameters(getEntity().getId()));
        }
    }
}
