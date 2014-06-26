package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class ExistingPoolInstanceTypeManager extends InstanceTypeManager {

    private VM pool;

    public ExistingPoolInstanceTypeManager(UnitVmModel model, VM pool) {
        super(model);

        this.pool = pool;
    }

    @Override
    protected void doUpdateManagedFieldsFrom(VmBase vmBase) {
        boolean numOfSocketsChangable = getModel().getNumOfSockets().getIsChangable();
        boolean coresPerSocket = getModel().getCoresPerSocket().getIsChangable();

        super.doUpdateManagedFieldsFrom(vmBase);

        deactivate();
        getModel().getNumOfSockets().setIsChangable(numOfSocketsChangable);
        getModel().getCoresPerSocket().setIsChangable(coresPerSocket);
        activate();
    }

    @Override
    protected VmBase getSource() {
        return pool.getStaticData();
    }

    @Override
    protected Guid getSelectedInstanceTypeId() {
        return super.getSelectedInstanceTypeId() == null ? pool.getInstanceTypeId() : super.getSelectedInstanceTypeId();
    }

    protected void maybeSetSingleQxlPci(VmBase vmBase) {
        maybeSetEntity(getModel().getIsSingleQxlEnabled(), pool.getSingleQxlPci());
        getModel().getIsSingleQxlEnabled().setEntity(pool.getSingleQxlPci() && getModel().getIsQxlSupported());
    }
}
