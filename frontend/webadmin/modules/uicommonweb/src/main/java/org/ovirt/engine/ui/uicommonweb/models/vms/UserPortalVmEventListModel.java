package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetAllAuditLogsByVMNameParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class UserPortalVmEventListModel extends VmEventListModel {

    @Override
    protected void refreshModel() {
        if (getEntity() == null) {
            return;
        }

        VM vm = (VM) getEntity();

        super.SyncSearch(VdcQueryType.GetAllAuditLogsByVMName, new GetAllAuditLogsByVMNameParameters(vm.getName()));
    }

    @Override
    protected void preSearchCalled(VM vm) {
        // do nothing - only the webadmin sets the search string
    }

}
