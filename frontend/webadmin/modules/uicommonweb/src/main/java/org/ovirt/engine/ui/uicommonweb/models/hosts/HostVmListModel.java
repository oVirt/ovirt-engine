package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.VmErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.VmAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestContainerListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class HostVmListModel extends VmListModel<VDS> {

    private HostVmFilter viewFilterType;

    @Inject
    public HostVmListModel(final VmGeneralModel vmGeneralModel,
            final VmInterfaceListModel vmInterfaceListModel,
            final VmDiskListModel vmDiskListModel,
            final VmSnapshotListModel vmSnapshotListModel,
            final VmEventListModel vmEventListModel,
            final VmAppListModel<VM> vmAppListModel,
            final PermissionListModel<VM> permissionListModel,
            final VmAffinityGroupListModel vmAffinityGroupListModel,
            final VmGuestInfoModel vmGuestInfoModel,
            final Provider<ImportVmsModel> importVmsModelProvider,
            final VmHostDeviceListModel vmHostDeviceListModel,
            final VmDevicesListModel<VM> vmDevicesListModel,
            final VmAffinityLabelListModel vmAffinityLabelListModel,
            final VmErrataCountModel vmErrataCountModel,
            final VmGuestContainerListModel vmGuestContainerListModel,
            final ConfirmationModelSettingsManager confirmationModelSettingsManager) {
        super(vmGeneralModel, vmInterfaceListModel, vmDiskListModel,
                vmSnapshotListModel, vmEventListModel, vmAppListModel,
                permissionListModel, vmAffinityGroupListModel, vmGuestInfoModel,
                importVmsModelProvider, vmHostDeviceListModel, vmDevicesListModel,
                vmAffinityLabelListModel, vmErrataCountModel, vmGuestContainerListModel,
                confirmationModelSettingsManager);
        getSearchNextPageCommand().setIsAvailable(false);
        getSearchPreviousPageCommand().setIsAvailable(false);
    }

    @Override
    protected void syncSearch() {
        search();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        search();
    }

    @Override
    public void search() {
        // Override standard search query mechanism.
        if (getEntity() != null) {
            viewFilterType.executeQuery(getEntity().getId(), new SetRawItemsAsyncQuery());
        } else {
            setItems(new ArrayList<VM>());
        }
        startGridTimer();
    }

    @Override
    public boolean supportsServerSideSorting() {
        //Because this uses a non standard search mechanism, we can't use the build in sort feature and thus have
        //to fall back to using client side sorting.
        return false;
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("vds_name")) { //$NON-NLS-1$
            search();
        }
    }

    public HostVmFilter getViewFilterType() {
        return viewFilterType;
    }

    public void setViewFilterType(HostVmFilter viewFilterType) {
        this.viewFilterType = viewFilterType;
        search();
    }
}
