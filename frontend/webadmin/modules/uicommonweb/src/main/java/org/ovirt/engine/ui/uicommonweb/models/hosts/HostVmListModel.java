package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class HostVmListModel extends VmListModel {
    @Inject
    public HostVmListModel(VmGeneralModel vmGeneralModel,
            VmInterfaceListModel vmInterfaceListModel,
            VmDiskListModel vmDiskListModel,
            VmSnapshotListModel vmSnapshotListModel,
            VmEventListModel vmEventListModel,
            VmAppListModel vmAppListModel,
            PermissionListModel permissionListModel,
            VmAffinityGroupListModel vmAffinityGroupListModel,
            VmSessionsModel vmSessionsModel,
            Provider<ImportVmsModel> importVmsModelProvider) {
        super(vmGeneralModel, vmInterfaceListModel, vmDiskListModel, vmSnapshotListModel, vmEventListModel,
                vmAppListModel, permissionListModel, vmAffinityGroupListModel, vmSessionsModel, importVmsModelProvider);
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
    public VDS getEntity() {
        return (VDS) super.getEntity();
    }

    @Override
    public void search() {
        // Override standard search query mechanism.
        // During the migration, the VM should be visible on source host (Migrating From), and also
        // on destination host (Migrating To)
        if (getEntity() != null) {
            AsyncDataProvider.getInstance().getVmsRunningOnOrMigratingToVds(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    @SuppressWarnings("unchecked")
                    final ArrayList<VM> list = (ArrayList<VM>) returnValue;
                    final HostVmListModel model = (HostVmListModel) target;
                    model.setItems(list);
                }
            }), getEntity().getId());
        } else {
            setItems(new ArrayList<VM>());
        }
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
}
