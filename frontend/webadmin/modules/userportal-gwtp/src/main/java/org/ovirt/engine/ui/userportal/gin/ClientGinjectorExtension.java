package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.auth.LoggedInExtendedPlaceGatekeeper;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmDiskListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmInterfaceListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;
import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;

/**
 * UserPortal {@code Ginjector} extension interface.
 */
public interface ClientGinjectorExtension extends UserPortalGinUiBinderWidgets {

    // Core GWTP components

    @DefaultGatekeeper
    LoggedInExtendedPlaceGatekeeper getDefaultGatekeeper();

    // Application-level components

    ApplicationConstants getApplicationConstants();

    // UiCommon model providers

    // VirtualMachine

    VmSnapshotListModelProvider getVmSnapshotListModelProvider();

    UserPortalSearchableDetailModelProvider<AuditLog, UserPortalListModel, UserPortalVmEventListModel>
        getVmEventListModelProvider();

    VmMonitorModelProvider getVmMonitorModelProvider();

    UserPortalDetailModelProvider<UserPortalListModel, VmGeneralModel> getVmGeneralModelProvider();

    UserPortalDetailModelProvider<UserPortalListModel, PoolGeneralModel> getPoolGeneralModelProvider();

    VmInterfaceListModelProvider getVmInterfaceListModelProvider();

    UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel, PoolInterfaceListModel>
        getPoolInterfaceListModelProvider();

    VmDiskListModelProvider getVmDiskListModelProvider();

    UserPortalSearchableDetailModelProvider<Disk, UserPortalListModel, PoolDiskListModel>
        getPoolDiskListModelProvider();

    UserPortalDetailModelProvider<UserPortalListModel, VmGuestInfoModel> getVmGuestInfoModelProvider();

}
