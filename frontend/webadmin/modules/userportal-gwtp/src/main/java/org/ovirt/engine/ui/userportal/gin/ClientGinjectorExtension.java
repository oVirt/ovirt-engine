package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.auth.LoggedInExtendedPlaceGatekeeper;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.PoolDiskListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.PoolGeneralModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.PoolInterfaceListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmDiskListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmEventListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmGeneralModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmInterfaceListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSessionsModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;

import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;

/**
 * UserPortal {@code Ginjector} extension interface.
 */
public interface ClientGinjectorExtension {

    // Core GWTP components

    @DefaultGatekeeper
    LoggedInExtendedPlaceGatekeeper getDefaultGatekeeper();

    // Application-level components

    ApplicationConstants getApplicationConstants();

    // UiCommon model providers

    // VirtualMachine

    VmSnapshotListModelProvider getVmSnapshotListModelProvider();

    VmEventListModelProvider getVmEventListModelProvider();

    VmMonitorModelProvider getVmMonitorModelProvider();

    VmGeneralModelProvider getVmGeneralModelProvider();

    PoolGeneralModelProvider getPoolGeneralModelProvider();

    VmInterfaceListModelProvider getVmInterfaceListModelProvider();

    PoolInterfaceListModelProvider getPoolInterfaceListModelProvider();

    VmDiskListModelProvider getVmDiskListModelProvider();

    PoolDiskListModelProvider getPoolDiskListModelProvider();

    VmSessionsModelProvider getVmSessionsModelProvider();

}
