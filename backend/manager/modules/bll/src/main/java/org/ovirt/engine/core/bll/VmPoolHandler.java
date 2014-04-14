package org.ovirt.engine.core.bll;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VmPoolHandler {

    /**
     * VM should be return to pool after it stopped unless Manual Return VM To Pool chosen.
     *
     * @param vmId
     *            The VM's id.
     * @FIXME BLL commands should invoke IVDSEventListener.processOnVmStop instead of directly calling this class. This
     *        is not duable now since callers which aren't on BLL don't know CommandContext to avid bugs this method
     *        must be treated as the real implementor of VdsEventListener.processOnVmStop meanwhile till a better
     *        solution supplied
     */
    public static void processVmPoolOnStopVm(Guid vmId, CommandContext context) {
        VmPoolMap map = DbFacade.getInstance().getVmPoolDao().getVmPoolMapByVmGuid(vmId);
        List<DbUser> users = DbFacade.getInstance().getDbUserDao().getAllForVm(vmId);
        // Check if this is a Vm from a Vm pool, and is attached to a user
        if (map != null && users != null && !users.isEmpty()) {
            VmPool pool = DbFacade.getInstance().getVmPoolDao().get(map.getvm_pool_id());
            if (pool != null && pool.getVmPoolType() == VmPoolType.Automatic) {
                // should be only one user in the collection
                for (DbUser dbUser : users) {
                    Backend.getInstance().runInternalAction(VdcActionType.DetachUserFromVmFromPool,
                            new VmPoolSimpleUserParameters(map.getvm_pool_id(), dbUser.getId(), vmId), context);
                }
            }
        } else {
            // If we are dealing with a prestarted Vm or a regular Vm - clean stateless images
            // Otherwise this was already done in DetachUserFromVmFromPoolCommand
            removeVmStatelessImages(vmId, context);
        }

        QuotaManager.getInstance().rollbackQuotaByVmId(vmId);
        VmHandler.removeStatelessVmUnmanagedDevices(vmId);

        ApplyNextRunConfiguration(vmId);
    }

    /**
     * Update vm configuration with NEXT_RUN configuration, if exists
     * @param vmId
     */
    private static void ApplyNextRunConfiguration(Guid vmId) {
        // Remove snpashot first, in case other update is in progress, it will block this one with exclusive lock
        // and any newer update should be preffered to this one.
        Snapshot runSnap = DbFacade.getInstance().getSnapshotDao().get(vmId, SnapshotType.NEXT_RUN);
        if (runSnap != null) {
            DbFacade.getInstance().getSnapshotDao().remove(runSnap.getId());
            VM vm = DbFacade.getInstance().getVmDao().get(vmId);
            if (vm != null) {
                Date originalCreationDate = vm.getVmCreationDate();
                new SnapshotsManager().updateVmFromConfiguration(vm, runSnap.getVmConfiguration());
                // override creation date because the value in the config is the creation date of the config, not the vm
                vm.setVmCreationDate(originalCreationDate);

                VmManagementParametersBase updateVmParams = createUpdateVmParameters(vm);

                Backend.getInstance().runInternalAction(VdcActionType.UpdateVm, updateVmParams);
            }
        }
    }

    private static VmManagementParametersBase createUpdateVmParameters(VM vm) {
        // clear non updateable fields got from config
        vm.setExportDate(null);
        vm.setOvfVersion(null);

        VmManagementParametersBase updateVmParams = new VmManagementParametersBase(vm);
        updateVmParams.setUpdateWatchdog(true);
        updateVmParams.setSoundDeviceEnabled(false);
        updateVmParams.setBalloonEnabled(false);
        updateVmParams.setVirtioScsiEnabled(false);
        updateVmParams.setClearPayload(true);

        for (VmDevice device : vm.getManagedVmDeviceMap().values()) {
            switch (device.getType()) {
                case WATCHDOG:
                    updateVmParams.setWatchdog(new VmWatchdog(device));
                    break;
                case SOUND:
                    updateVmParams.setSoundDeviceEnabled(true);
                    break;
                case BALLOON:
                    updateVmParams.setBalloonEnabled(true);
                    break;
                case CONTROLLER:
                    if (VmDeviceType.VIRTIOSCSI.getName().equals(device.getDevice())) {
                        updateVmParams.setVirtioScsiEnabled(true);
                    }
                    break;
                case DISK:
                    if (VmPayload.isPayload(device.getSpecParams())) {
                        updateVmParams.setVmPayload(new VmPayload(VmDeviceType.getByName(device.getDevice()), device.getSpecParams()));
                    }
                    break;
                case CONSOLE:
                    updateVmParams.setConsoleEnabled(true);
                    break;
                default:
            }
        }

        // clear these fields as these are non updatable
        vm.getManagedVmDeviceMap().clear();
        vm.getVmUnamagedDeviceList().clear();

        return updateVmParams;
    }

    public static void removeVmStatelessImages(Guid vmId, CommandContext context) {
        if (DbFacade.getInstance().getSnapshotDao().exists(vmId, SnapshotType.STATELESS)) {
            log.infoFormat("VdcBll.VmPoolHandler.processVmPoolOnStopVm - Deleting snapshot for stateless vm {0}", vmId);
            Backend.getInstance().runInternalAction(VdcActionType.RestoreStatelessVm,
                    new VmOperationParameterBase(vmId),
                    context);
        }
    }

    private static final Log log = LogFactory.getLog(VmPoolHandler.class);
}
