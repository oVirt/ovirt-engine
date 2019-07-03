package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class CloneVmModelBehavior extends ExistingVmModelBehavior implements IEventListener<EventArgs> {

    public CloneVmModelBehavior(VM vm) {
        super(vm);
    }

    @Override
    public void initialize() {
        super.initialize();
        getModel().getVmId().setIsAvailable(false);
        getModel().getDataCenterWithClustersList().setIsChangeable(false);
        getModel().getDataCenterWithClustersList().getSelectedItemChangedEvent().addListener(this);
        getModel().getInstanceTypes().setIsChangeable(false);
        getModel().getInstanceImages().setIsAvailable(false);
        getModel().getStorageDomain().setIsChangeable(false);
        getModel().getCopyPermissions().setIsAvailable(false);
        initDisks(vm);
    }

    @Override
    public void templateWithVersion_SelectedItemChanged() {
        super.templateWithVersion_SelectedItemChanged();

        getModel().getName().setEntity(""); //$NON-NLS-1$
        getModel().getDescription().setEntity(""); //$NON-NLS-1$
        getModel().getComment().setEntity(""); //$NON-NLS-1$
        getModel().getProvisioning().setEntity(true);
        getModel().getProvisioning().setIsAvailable(true);
        getModel().getProvisioning().setIsChangeable(false);
    }

    private void initDisks(VM vm) {
        AsyncDataProvider.getInstance().getVmDiskList(asyncQuery(this::initTemplateDisks), vm.getId());
    }

    @Override
    public void updateIsDisksAvailable() {
        getModel().setIsDisksAvailable(getModel().getDisks() != null && !getModel().getDisks().isEmpty());
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        if (getModel().getSelectedDataCenter() != null) {
            postInitStorageDomains();
        }
    }
}
