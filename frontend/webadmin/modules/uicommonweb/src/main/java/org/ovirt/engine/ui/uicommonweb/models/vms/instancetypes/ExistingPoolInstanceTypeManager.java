package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.templates.LatestVmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.CustomInstanceType;
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
        boolean threadsPerCore = getModel().getThreadsPerCore().getIsChangable();

        super.doUpdateManagedFieldsFrom(vmBase);

        deactivate();
        getModel().getNumOfSockets().setIsChangeable(numOfSocketsChangable);
        getModel().getCoresPerSocket().setIsChangeable(coresPerSocket);
        getModel().getThreadsPerCore().setIsChangeable(threadsPerCore);
        activate();
    }

    @Override
    protected VmBase getSource() {
        boolean customInstanceTypeUsed = getModel().getInstanceTypes() != null &&
                getModel().getInstanceTypes().getSelectedItem() instanceof CustomInstanceType;

        if (!customInstanceTypeUsed) {
            return (VmBase) getModel().getInstanceTypes().getSelectedItem();
        }

        if (getModel().getTemplateWithVersion() == null ||
                getModel().getTemplateWithVersion().getSelectedItem() == null ||
                getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion() == null) {
            return pool.getStaticData();
        }

        VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();

        boolean isLatestPropertyChanged = pool.isUseLatestVersion() != (template instanceof LatestVmTemplate);

        // template ID changed but latest is not set, as it would cause false-positives
        boolean isTemplateIdChangedSinceInit = !pool.getVmtGuid().equals(template.getId()) && !pool.isUseLatestVersion();

        if (isTemplateIdChangedSinceInit || isLatestPropertyChanged) {
            return template;
        }

        return pool.getStaticData();
    }

    @Override
    protected Guid getSelectedInstanceTypeId() {
        return super.getSelectedInstanceTypeId() == null ? pool.getInstanceTypeId() : super.getSelectedInstanceTypeId();
    }
}
