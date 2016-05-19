package org.ovirt.engine.core.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;

public final class VdcActionUtils {

    private static final Map<Class<?>, Map<Enum<?>, Set<VdcActionType>>> _matrix =
            new HashMap<>();

    static {
        // this matrix contains the actions that CANNOT run per status
        // ("black list")
        Map<Enum<?>, Set<VdcActionType>> vdsMatrix = new HashMap<>();
        vdsMatrix.put(
                VDSStatus.Maintenance,
                EnumSet.of(VdcActionType.MaintenanceVds, VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds));
        vdsMatrix.put(
                VDSStatus.Up,
                EnumSet.of(VdcActionType.ActivateVds, VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.StartVds, VdcActionType.StopVds));
        vdsMatrix.put(
                VDSStatus.Error,
                EnumSet.of(VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Installing,
                EnumSet.of(VdcActionType.RemoveVds, VdcActionType.ActivateVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.MaintenanceVds, VdcActionType.StartVds,
                        VdcActionType.StopVds, VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.NonResponsive,
                EnumSet.of(VdcActionType.RemoveVds, VdcActionType.ActivateVds,
                        VdcActionType.ApproveVds, VdcActionType.RefreshHostCapabilities, VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.PreparingForMaintenance,
                EnumSet.of(VdcActionType.RemoveVds, VdcActionType.MaintenanceVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.RefreshHostCapabilities, VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Reboot,
                EnumSet.of(VdcActionType.ActivateVds, VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds,
                        VdcActionType.MaintenanceVds,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Unassigned,
                EnumSet.of(VdcActionType.ActivateVds,
                        VdcActionType.RemoveVds,
                        VdcActionType.MaintenanceVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Initializing,
                EnumSet.of(VdcActionType.ActivateVds, VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms, VdcActionType.ApproveVds,
                        VdcActionType.MaintenanceVds, VdcActionType.RefreshHostCapabilities, VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.NonOperational,
                EnumSet.of(VdcActionType.RemoveVds,
                        VdcActionType.ApproveVds));
        vdsMatrix.put(
                VDSStatus.PendingApproval,
                EnumSet.of(VdcActionType.UpdateVds,
                        VdcActionType.ActivateVds, VdcActionType.MaintenanceVds,
                        VdcActionType.AttachVdsToTag,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.InstallingOS,
                EnumSet.of(VdcActionType.UpdateVds,
                        VdcActionType.ActivateVds,
                        VdcActionType.MaintenanceVds,
                        VdcActionType.AttachVdsToTag,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.ApproveVds,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.InstallFailed,
                EnumSet.of(VdcActionType.ApproveVds, VdcActionType.RefreshHostCapabilities));
        vdsMatrix.put(
                VDSStatus.Connecting,
                EnumSet.of(VdcActionType.MaintenanceVds, VdcActionType.RemoveVds,
                        VdcActionType.ActivateVds,
                        VdcActionType.ApproveVds,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));
        vdsMatrix.put(
                VDSStatus.Down,
                EnumSet.of(VdcActionType.ActivateVds,
                        VdcActionType.ApproveVds,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));

        vdsMatrix.put(
                VDSStatus.Kdumping,
                EnumSet.of(
                        VdcActionType.ActivateVds,
                        VdcActionType.ApproveVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.MaintenanceVds,
                        VdcActionType.RemoveVds,
                        VdcActionType.RefreshHostCapabilities,
                        VdcActionType.UpgradeHost));

        _matrix.put(VDS.class, vdsMatrix);

        Map<Enum<?>, Set<VdcActionType>> vmMatrix = new HashMap<>();
        vmMatrix.put(
                VMStatus.WaitForLaunch,
                EnumSet.of(VdcActionType.HibernateVm, VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm,
                        VdcActionType.ExtendImageSize, VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.Up,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm, VdcActionType.CloneVm,
                        VdcActionType.ExportVm, VdcActionType.ImportVm,
                        VdcActionType.CancelMigrateVm));
        vmMatrix.put(
                VMStatus.PoweringDown,
                EnumSet.of(VdcActionType.HibernateVm, VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce,
                        VdcActionType.AddVmTemplate, VdcActionType.RemoveVm, VdcActionType.MigrateVm,
                        VdcActionType.ExportVm, VdcActionType.ImportVm,
                        VdcActionType.ChangeDisk, VdcActionType.AddVmInterface,
                        VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm,
                        VdcActionType.ExtendImageSize, VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.PoweringUp,
                EnumSet.of(VdcActionType.HibernateVm, VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm,
                        VdcActionType.ExtendImageSize));
        vmMatrix.put(
                VMStatus.RebootInProgress,
                EnumSet.of(VdcActionType.HibernateVm, VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm,
                        VdcActionType.ExtendImageSize, VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.MigratingFrom,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CreateAllSnapshotsFromVm,
                        VdcActionType.ExtendImageSize, VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.Suspended,
                EnumSet.of(VdcActionType.HibernateVm, VdcActionType.AddVmTemplate, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.MigrateVm, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk, VdcActionType.RemoveVm,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm, VdcActionType.RebootVm,
                        VdcActionType.CreateAllSnapshotsFromVm));
        vmMatrix.put(
                VMStatus.Paused,
                EnumSet.of(VdcActionType.RemoveVm, VdcActionType.HibernateVm, VdcActionType.CloneVm,
                        VdcActionType.AddVmTemplate, VdcActionType.RunVmOnce, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ExtendImageSize,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm, VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.SavingState,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm,
                        VdcActionType.ExtendImageSize, VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.RestoringState,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm, VdcActionType.ExtendImageSize,
                        VdcActionType.RebootVm));

        vmMatrix.put(
                VMStatus.Down,
                EnumSet.of(VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.ChangeDisk,
                        VdcActionType.CancelMigrateVm, VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.ImageIllegal,
                EnumSet.of(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce,
                        VdcActionType.StopVm,
                        VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm,
                        VdcActionType.MigrateVm,
                        VdcActionType.AddVmTemplate,
                        VdcActionType.ExportVm,
                        VdcActionType.ImportVm,
                        VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface,
                        VdcActionType.UpdateVmInterface,
                        VdcActionType.CreateAllSnapshotsFromVm,
                        VdcActionType.RemoveVmInterface,
                        VdcActionType.CancelMigrateVm,
                        VdcActionType.ExtendImageSize,
                        VdcActionType.RebootVm,
                        VdcActionType.CloneVm));
        vmMatrix.put(
                VMStatus.ImageLocked,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk, VdcActionType.CreateAllSnapshotsFromVm,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm, VdcActionType.ExtendImageSize,
                        VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.NotResponding,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.HibernateVm, VdcActionType.MigrateVm,
                        VdcActionType.RemoveVm, VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm, VdcActionType.ExtendImageSize,
                        VdcActionType.RebootVm));

        vmMatrix.put(
                VMStatus.Unassigned,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk, VdcActionType.CreateAllSnapshotsFromVm,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm, VdcActionType.ExtendImageSize,
                        VdcActionType.RebootVm));
        vmMatrix.put(
                VMStatus.Unknown,
                EnumSet.of(VdcActionType.RunVm, VdcActionType.CloneVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk, VdcActionType.CreateAllSnapshotsFromVm,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm, VdcActionType.ExtendImageSize,
                        VdcActionType.RebootVm));
        _matrix.put(VM.class, vmMatrix);

        Map<Enum<?>, Set<VdcActionType>> vmTemplateMatrix = new HashMap<>();
        vmTemplateMatrix.put(
                VmTemplateStatus.Locked,
                EnumSet.of(VdcActionType.RemoveVmTemplate,
                        VdcActionType.ExportVmTemplate,
                        VdcActionType.ImportVmTemplate));
        vmTemplateMatrix.put(
                VmTemplateStatus.Illegal,
                EnumSet.of(VdcActionType.ExportVmTemplate, VdcActionType.ImportVmTemplate));
        _matrix.put(VmTemplate.class, vmTemplateMatrix);

        Map<Enum<?>, Set<VdcActionType>> storageDomainMatrix = new HashMap<>();
        storageDomainMatrix.put(
                StorageDomainStatus.Active,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool, VdcActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Inactive,
                EnumSet.of(VdcActionType.DeactivateStorageDomainWithOvfUpdate));
        storageDomainMatrix.put(
                StorageDomainStatus.Locked,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomainWithOvfUpdate, VdcActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Unattached,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomainWithOvfUpdate, VdcActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Uninitialized,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomainWithOvfUpdate, VdcActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Unknown,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool, VdcActionType.DeactivateStorageDomainWithOvfUpdate));
        storageDomainMatrix.put(
                StorageDomainStatus.Maintenance,
                EnumSet.of(VdcActionType.DeactivateStorageDomainWithOvfUpdate));
        storageDomainMatrix.put(
                StorageDomainStatus.PreparingForMaintenance,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool, VdcActionType.DeactivateStorageDomainWithOvfUpdate));
        storageDomainMatrix.put(
                StorageDomainStatus.Detaching,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomainWithOvfUpdate, VdcActionType.ActivateStorageDomain));
        storageDomainMatrix.put(
                StorageDomainStatus.Activating,
                EnumSet.of(VdcActionType.DetachStorageDomainFromPool, VdcActionType.ActivateStorageDomain));
        _matrix.put(StorageDomain.class, storageDomainMatrix);
    }

    public static boolean canExecute(List<? extends BusinessEntityWithStatus<?, ?>> entities,
            Class type,
            VdcActionType action) {

        if (entities == null) {
            return false;
        }

        if (_matrix.containsKey(type)) {
            for (BusinessEntityWithStatus<?, ?> a : entities) {
                if (a.getClass() == type && _matrix.get(type).containsKey(a.getStatus())
                        && _matrix.get(type).get(a.getStatus()).contains(action)) {
                    return false;
                }
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
                                     VdcActionType action) {
        if (_matrix.containsKey(type)) {
            for (BusinessEntityWithStatus<?, ?> a : entities) {
                if (a.getClass() == type &&
                    (!_matrix.get(type).containsKey(a.getStatus())
                        || !_matrix.get(type).get(a.getStatus()).contains(action))) {
                    return true;
                }
            }
        }
        else {
            return true;
        }
        return false;
    }
}
