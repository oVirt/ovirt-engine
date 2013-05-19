package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.Linq;

public class UserPortalVmEventListModel extends VmEventListModel {

    @Override
    protected void refreshModel() {
        if (getEntity() == null) {
            return;
        }

        VM vm = (VM) getEntity();

        super.syncSearch(VdcQueryType.GetAllAuditLogsByVMName, new NameQueryParameters(vm.getName()));
    }

    @Override
    protected void preSearchCalled(VM vm) {
        // do nothing - only the webadmin sets the search string
    }

    @Override
    public void setItems(Iterable value) {
        List<AuditLog> list = (List<AuditLog>) value;
        if (list != null) {
            Collections.sort(list, Collections.reverseOrder(new Linq.AuditLogComparer()));
        }
        super.setItems(list);
    }
}
