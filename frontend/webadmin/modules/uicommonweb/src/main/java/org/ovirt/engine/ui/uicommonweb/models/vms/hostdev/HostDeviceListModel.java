package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class HostDeviceListModel extends HostDeviceListModelBase<VDS> {

    @Inject
    public HostDeviceListModel(UIConstants constants) {
        setTitle(constants.hostDevicesTitle());
        setHelpTag(HelpTag.host_devices);
        setHashName("devices"); //$NON-NLS-1$
    }

    @Override
    protected String getListName() {
        return "HostDeviceListModel"; //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        if (getEntity() != null) {
            syncSearch(QueryType.GetExtendedHostDevicesByHostId, new IdQueryParameters(getEntity().getId()));
        }
    }
}
