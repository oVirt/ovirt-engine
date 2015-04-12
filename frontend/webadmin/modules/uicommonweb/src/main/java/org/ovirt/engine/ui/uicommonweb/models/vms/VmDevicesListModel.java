package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VmDevicesListModel <E extends BusinessEntity<Guid>> extends SearchableListModel<E, VmDevice> {
    public VmDevicesListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmDevicesTitle());
        setHelpTag(HelpTag.vm_devices);
        setHashName("vm_devices"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }


    @Override
    protected void syncSearch() {
        if (getEntity() != null) {
            super.syncSearch(VdcQueryType.GetVmDevicesForVm, new IdQueryParameters(getEntity().getId()));
        }
    }

    @Override
    protected String getListName() {
        return "VmDevicesListModel"; //$NON-NLS-1$
    }
}
