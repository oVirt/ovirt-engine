package org.ovirt.engine.core.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.Managed;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmWithStatusForExclusiveLock;

public final class ActionUtils {

    private static final Map<Class<?>, Map<Enum<?>, Set<ActionType>>> _matrix =
            new HashMap<>();

    static {
        // this matrix contains the actions that CANNOT run per status
        // ("black list")
        Map<Enum<?>, Set<ActionType>> vdsMatrix = new HashMap<>();
        vdsMatrix.put(
                VDSStatus.Maintenance,
                EnumSet.of(ActionType.MaintenanceVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.ApproveVds));
        vdsMatrix.put(
                VDSStatus.Up,
                EnumSet.of(ActionType.ActivateVds, ActionType.RemoveVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.ApproveVds, ActionType.StartVds,
                        ActionType.StopVds, ActionType.VdsPowerDown));
        vdsMatrix.put(
                VDSStatus.Error,
                EnumSet.of(ActionType.RemoveVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.ApproveVds,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Installing,
                EnumSet.of(ActionType.RemoveVds, ActionType.ActivateVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.ApproveVds, ActionType.MaintenanceVds, ActionType.StartVds,
                        ActionType.StopVds, ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost, ActionType.VdsPowerDown));
        vdsMatrix.put(
                VDSStatus.NonResponsive,
                EnumSet.of(ActionType.RemoveVds, ActionType.ActivateVds,
                        ActionType.ApproveVds, ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck, ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.PreparingForMaintenance,
                EnumSet.of(ActionType.RemoveVds, ActionType.MaintenanceVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.ApproveVds, ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck, ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Reboot,
                EnumSet.of(ActionType.ActivateVds, ActionType.RemoveVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.ApproveVds,
                        ActionType.MaintenanceVds,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Unassigned,
                EnumSet.of(ActionType.ActivateVds,
                        ActionType.RemoveVds,
                        ActionType.MaintenanceVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.ApproveVds,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Initializing,
                EnumSet.of(ActionType.ActivateVds, ActionType.RemoveVds,
                        ActionType.ClearNonResponsiveVdsVms, ActionType.ApproveVds,
                        ActionType.MaintenanceVds, ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck, ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.NonOperational,
                EnumSet.of(ActionType.RemoveVds,
                        ActionType.ApproveVds));
        vdsMatrix.put(
                VDSStatus.PendingApproval,
                EnumSet.of(ActionType.UpdateVds,
                        ActionType.ActivateVds, ActionType.MaintenanceVds,
                        ActionType.AttachVdsToTag,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.InstallingOS,
                EnumSet.of(ActionType.UpdateVds,
                        ActionType.ActivateVds,
                        ActionType.MaintenanceVds,
                        ActionType.AttachVdsToTag,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.RefreshHostCapabilities,
                        ActionType.ApproveVds,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.InstallFailed,
                EnumSet.of(ActionType.ApproveVds, ActionType.RefreshHostCapabilities));
        vdsMatrix.put(
                VDSStatus.Connecting,
                EnumSet.of(ActionType.MaintenanceVds, ActionType.RemoveVds,
                        ActionType.ActivateVds,
                        ActionType.ApproveVds,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Down,
                EnumSet.of(ActionType.ActivateVds,
                        ActionType.ApproveVds,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost,
                        ActionType.SshHostReboot,
                        ActionType.VdsPowerDown));

        vdsMatrix.put(
                VDSStatus.Kdumping,
                EnumSet.of(
                        ActionType.ActivateVds,
                        ActionType.ApproveVds,
                        ActionType.ClearNonResponsiveVdsVms,
                        ActionType.MaintenanceVds,
                        ActionType.RemoveVds,
                        ActionType.RefreshHostCapabilities,
                        ActionType.HostUpgradeCheck,
                        ActionType.UpgradeHost));

        _matrix.put(VDS.class, vdsMatrix);

        Map<Enum<?>, Set<ActionType>> vmMatrix = new HashMap<>();
        vmMatrix.put(
                VMStatus.WaitForLaunch,
                EnumSet.of(ActionType.HibernateVm, ActionType.RunVm,
                        ActionType.RunVmOnce, ActionType.AddVmTemplate, ActionType.RemoveVm,
                        ActionType.ExportVm, ActionType.ImportVm, ActionType.ChangeDisk,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.MigrateVm, ActionType.CancelMigrateVm,
                        ActionType.ExtendImageSize, ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.Up,
                EnumSet.of(ActionType.RunVm,
                        ActionType.RunVmOnce, ActionType.AddVmTemplate, ActionType.RemoveVm,
                        ActionType.ExportVm, ActionType.ImportVm,
                        ActionType.CancelMigrateVm));
        vmMatrix.put(
                VMStatus.PoweringDown,
                EnumSet.of(ActionType.HibernateVm, ActionType.RunVm,
                        ActionType.RunVmOnce,
                        ActionType.AddVmTemplate, ActionType.RemoveVm, ActionType.MigrateVm,
                        ActionType.ExportVm, ActionType.ImportVm,
                        ActionType.ChangeDisk, ActionType.AddVmInterface,
                        ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm,
                        ActionType.ExtendImageSize, ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.PoweringUp,
                EnumSet.of(ActionType.HibernateVm, ActionType.RunVm,
                        ActionType.RunVmOnce, ActionType.AddVmTemplate, ActionType.RemoveVm,
                        ActionType.ExportVm, ActionType.ImportVm, ActionType.ChangeDisk,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm,
                        ActionType.ExtendImageSize));
        vmMatrix.put(
                VMStatus.RebootInProgress,
                EnumSet.of(ActionType.HibernateVm, ActionType.RunVm,
                        ActionType.RunVmOnce, ActionType.AddVmTemplate, ActionType.RemoveVm,
                        ActionType.ExportVm, ActionType.ImportVm, ActionType.ChangeDisk,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm,
                        ActionType.ExtendImageSize, ActionType.RebootVm));
        vmMatrix.put(
                VMStatus.MigratingFrom,
                EnumSet.of(ActionType.RunVm,
                        ActionType.RunVmOnce, ActionType.AddVmTemplate, ActionType.RemoveVm,
                        ActionType.HibernateVm, ActionType.MigrateVm, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CreateSnapshotForVm,
                        ActionType.ExtendImageSize, ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.Suspended,
                EnumSet.of(ActionType.HibernateVm, ActionType.AddVmTemplate,
                        ActionType.RunVmOnce, ActionType.MigrateVm, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk, ActionType.RemoveVm,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm, ActionType.RebootVm,
                        ActionType.CreateSnapshotForVm, ActionType.CloneVm));
        vmMatrix.put(
                VMStatus.Paused,
                EnumSet.of(ActionType.RemoveVm, ActionType.HibernateVm,
                        ActionType.AddVmTemplate, ActionType.RunVmOnce, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ExtendImageSize,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm,
                        ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.SavingState,
                EnumSet.of(ActionType.RunVm, ActionType.CloneVm,
                        ActionType.RunVmOnce, ActionType.StopVm, ActionType.ShutdownVm,
                        ActionType.HibernateVm, ActionType.MigrateVm, ActionType.RemoveVm,
                        ActionType.AddVmTemplate, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm,
                        ActionType.ExtendImageSize, ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.RestoringState,
                EnumSet.of(ActionType.RunVm, ActionType.CloneVm,
                        ActionType.RunVmOnce, ActionType.StopVm, ActionType.ShutdownVm,
                        ActionType.HibernateVm, ActionType.MigrateVm, ActionType.RemoveVm,
                        ActionType.AddVmTemplate, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm, ActionType.ExtendImageSize,
                        ActionType.RebootVm, ActionType.ResetVm));

        vmMatrix.put(
                VMStatus.Down,
                EnumSet.of(ActionType.StopVm, ActionType.ShutdownVm,
                        ActionType.HibernateVm, ActionType.MigrateVm, ActionType.ChangeDisk,
                        ActionType.CancelMigrateVm, ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.ImageIllegal,
                EnumSet.of(ActionType.RunVm,
                        ActionType.RunVmOnce,
                        ActionType.StopVm,
                        ActionType.ShutdownVm,
                        ActionType.HibernateVm,
                        ActionType.MigrateVm,
                        ActionType.AddVmTemplate,
                        ActionType.ExportVm,
                        ActionType.ImportVm,
                        ActionType.ChangeDisk,
                        ActionType.AddVmInterface,
                        ActionType.UpdateVmInterface,
                        ActionType.CreateSnapshotForVm,
                        ActionType.RemoveVmInterface,
                        ActionType.CancelMigrateVm,
                        ActionType.ExtendImageSize,
                        ActionType.RebootVm,
                        ActionType.ResetVm,
                        ActionType.CloneVm));
        vmMatrix.put(
                VMStatus.ImageLocked,
                EnumSet.of(ActionType.RunVm, ActionType.CloneVm,
                        ActionType.RunVmOnce, ActionType.StopVm, ActionType.ShutdownVm,
                        ActionType.HibernateVm, ActionType.MigrateVm, ActionType.RemoveVm,
                        ActionType.AddVmTemplate, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk, ActionType.CreateSnapshotForVm,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm, ActionType.ExtendImageSize,
                        ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.NotResponding,
                EnumSet.of(ActionType.RunVm, ActionType.CloneVm,
                        ActionType.RunVmOnce, ActionType.HibernateVm, ActionType.MigrateVm,
                        ActionType.RemoveVm, ActionType.AddVmTemplate, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk, ActionType.CreateSnapshotForVm,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm, ActionType.ExtendImageSize,
                        ActionType.RebootVm, ActionType.ResetVm));

        vmMatrix.put(
                VMStatus.Unassigned,
                EnumSet.of(ActionType.RunVm, ActionType.CloneVm,
                        ActionType.RunVmOnce, ActionType.StopVm, ActionType.ShutdownVm,
                        ActionType.HibernateVm, ActionType.MigrateVm, ActionType.RemoveVm,
                        ActionType.AddVmTemplate, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk, ActionType.CreateSnapshotForVm,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm, ActionType.ExtendImageSize,
                        ActionType.RebootVm, ActionType.ResetVm));
        vmMatrix.put(
                VMStatus.Unknown,
                EnumSet.of(ActionType.RunVm, ActionType.CloneVm,
                        ActionType.RunVmOnce, ActionType.StopVm, ActionType.ShutdownVm,
                        ActionType.HibernateVm, ActionType.MigrateVm, ActionType.RemoveVm,
                        ActionType.AddVmTemplate, ActionType.ExportVm,
                        ActionType.ImportVm, ActionType.ChangeDisk, ActionType.CreateSnapshotForVm,
                        ActionType.AddVmInterface, ActionType.UpdateVmInterface,
                        ActionType.RemoveVmInterface, ActionType.CancelMigrateVm, ActionType.ExtendImageSize,
                        ActionType.RebootVm, ActionType.ResetVm));
        _matrix.put(VM.class, vmMatrix);
        _matrix.put(VmWithStatusForExclusiveLock.class, vmMatrix);

        Map<Enum<?>, Set<ActionType>> vmTemplateMatrix = new HashMap<>();
        vmTemplateMatrix.put(
                VmTemplateStatus.Locked,
                EnumSet.of(ActionType.RemoveVmTemplate,
                        ActionType.ExportVmTemplate,
                        ActionType.ImportVmTemplate,
                        ActionType.UpdateVmTemplate));
        vmTemplateMatrix.put(
                VmTemplateStatus.Illegal,
                EnumSet.of(ActionType.ExportVmTemplate, ActionType.ImportVmTemplate));
        _matrix.put(VmTemplate.class, vmTemplateMatrix);

        Map<Enum<?>, Set<ActionType>> storageDomainMatrix = new HashMap<>();
        storageDomainMatrix.put(
                StorageDomainStatus.Active,
                EnumSet.of(ActionType.DetachStorageDomainFromPool, ActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Inactive,
                EnumSet.of(ActionType.DetachStorageDomainFromPool));
        storageDomainMatrix.put(
                StorageDomainStatus.Locked,
                EnumSet.of(ActionType.DetachStorageDomainFromPool,
                        ActionType.DeactivateStorageDomainWithOvfUpdate, ActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Unattached,
                EnumSet.of(ActionType.DetachStorageDomainFromPool,
                        ActionType.DeactivateStorageDomainWithOvfUpdate, ActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Uninitialized,
                EnumSet.of(ActionType.DetachStorageDomainFromPool,
                        ActionType.DeactivateStorageDomainWithOvfUpdate, ActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Unknown,
                EnumSet.of(ActionType.DetachStorageDomainFromPool, ActionType.DeactivateStorageDomainWithOvfUpdate));
        storageDomainMatrix.put(
                StorageDomainStatus.Maintenance,
                EnumSet.of(ActionType.DeactivateStorageDomainWithOvfUpdate));
        storageDomainMatrix.put(
                StorageDomainStatus.PreparingForMaintenance,
                EnumSet.of(ActionType.DetachStorageDomainFromPool, ActionType.DeactivateStorageDomainWithOvfUpdate));
        storageDomainMatrix.put(
                StorageDomainStatus.Detaching,
                EnumSet.of(ActionType.DetachStorageDomainFromPool,
                        ActionType.DeactivateStorageDomainWithOvfUpdate, ActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Activating,
                EnumSet.of(ActionType.DetachStorageDomainFromPool, ActionType.ActivateStorageDomain));
        _matrix.put(StorageDomain.class, storageDomainMatrix);
    }

    private static boolean canExecute(BusinessEntityWithStatus<?, ?> entity, Class type, ActionType action) {
        if (!KubevirtSupportedActions.isActionSupported(entity, action)) {
            return false;
        }

        Set<ActionType> disallowedActions = _matrix.get(type).get(entity.getStatus());
        return disallowedActions == null || !disallowedActions.contains(action);
    }

    public static boolean canExecute(List<? extends BusinessEntityWithStatus<?, ?>> entities,
            Class type,
            ActionType action) {

        if (entities == null) {
            return false;
        }

        if (_matrix.containsKey(type)) {
            for (BusinessEntityWithStatus<?, ?> entity : entities) {
                if (entity.getClass() == type && !canExecute(entity, type, action)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean canExecute(List<? extends Managed> entities,
            ActionType action,
            Class type) {

        if (entities == null) {
            return false;
        }

        for (Managed entity : entities) {
            if (entity.getClass() == type && !KubevirtSupportedActions.isActionSupported(entity, action)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method determines per list of entities whether the selected action can run at least on one item from the
     * entities list
     */
    public static boolean canExecutePartially(List<? extends BusinessEntityWithStatus<?, ?>> entities,
                                     Class type,
                                     ActionType action) {
        if (_matrix.containsKey(type)) {
            for (BusinessEntityWithStatus<?, ?> entity : entities) {
                if (entity.getClass() == type && canExecute(entity, type, action)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

}
