package org.ovirt.engine.core.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.NotImplementedException;

public final class VdcActionUtils {

    private static java.util.Map<java.lang.Class<?>, java.util.Map<Enum<?>, java.util.HashSet<VdcActionType>>>
            _matrix =
            new java.util.HashMap<java.lang.Class<?>, java.util.Map<Enum<?>, java.util.HashSet<VdcActionType>>>();

    static {
        // this matrix contains the actions that CANNOT run per status
        // ("black list")
        java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>> vdsMatrix =
                new java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>>();
        vdsMatrix.put(
                VDSStatus.Maintenance,
                new HashSet<VdcActionType>(Arrays
                        .asList(VdcActionType.MaintananceVds, VdcActionType.ClearNonResponsiveVdsVms,
                                VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.Up,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.ActivateVds, VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.StartVds, VdcActionType.StopVds)));
        vdsMatrix.put(
                VDSStatus.Error,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms, VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.Installing,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RemoveVds, VdcActionType.ActivateVds,
                        VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.MaintananceVds, VdcActionType.StartVds,
                        VdcActionType.StopVds)));
        vdsMatrix.put(
                VDSStatus.NonResponsive,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RemoveVds, VdcActionType.ActivateVds,
                        VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.PreparingForMaintenance,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RemoveVds, VdcActionType.MaintananceVds,
                        VdcActionType.ClearNonResponsiveVdsVms,

                        VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.Reboot,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.ActivateVds, VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms,

                        VdcActionType.ApproveVds, VdcActionType.MaintananceVds)));
        vdsMatrix.put(
                VDSStatus.Unassigned,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.ActivateVds, VdcActionType
                        .RemoveVds, VdcActionType.MaintananceVds,
                        VdcActionType.ClearNonResponsiveVdsVms, VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.Initializing,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.ActivateVds, VdcActionType.RemoveVds,
                        VdcActionType.ClearNonResponsiveVdsVms, VdcActionType.ApproveVds,
                        VdcActionType.MaintananceVds)));
        vdsMatrix.put(
                VDSStatus.NonOperational,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RemoveVds, VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.PendingApproval,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.UpdateVds,
                        VdcActionType.ActivateVds, VdcActionType.MaintananceVds,
                        VdcActionType.AttachVdsToTag, VdcActionType.ClearNonResponsiveVdsVms)));
        vdsMatrix.put(
                VDSStatus.InstallFailed,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.Connecting,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.MaintananceVds, VdcActionType.RemoveVds,
                        VdcActionType.ActivateVds, VdcActionType.ApproveVds)));
        vdsMatrix.put(
                VDSStatus.Down,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.ActivateVds, VdcActionType
                        .ApproveVds)));
        _matrix.put(VDS.class, vdsMatrix);

        HashMap<Enum<?>, HashSet<VdcActionType>> vmMatrix =
                new HashMap<Enum<?>, HashSet<VdcActionType>>();
        vmMatrix.put(
                VMStatus.WaitForLaunch,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.HibernateVm, VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.Up,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm,
                        VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.PoweringDown,
                new HashSet<VdcActionType>(Arrays
                        .asList(VdcActionType.HibernateVm, VdcActionType.RunVm,
                                VdcActionType.RunVmOnce,
                                VdcActionType.AddVmTemplate, VdcActionType.RemoveVm, VdcActionType.MigrateVm,
                                VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm,
                                VdcActionType.ChangeDisk, VdcActionType.AddVmInterface,
                                VdcActionType.UpdateVmInterface,
                                VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.PoweringUp,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.HibernateVm, VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.RebootInProgress,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.HibernateVm, VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.MigratingFrom,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.ExportVm,
                        VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface)));
        vmMatrix.put(
                VMStatus.PoweredDown,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.HibernateVm,
                        VdcActionType.RunVm, VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate,
                        VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));

        vmMatrix.put(
                VMStatus.Suspended,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.HibernateVm, VdcActionType.AddVmTemplate,
                        VdcActionType.RunVmOnce, VdcActionType.MigrateVm, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk, VdcActionType.RemoveVm,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.Paused,
                new HashSet<VdcActionType>(Arrays
                        .asList(VdcActionType.RemoveVm, VdcActionType.HibernateVm,
                                VdcActionType.AddVmTemplate, VdcActionType.RunVmOnce, VdcActionType.ExportVm,
                                VdcActionType.MoveVm, VdcActionType.ImportVm,
                                VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                                VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.SavingState,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.RestoringState,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));

        vmMatrix.put(
                VMStatus.Down,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.ChangeDisk,
                        VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.ImageIllegal,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.AddVmTemplate,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.ImageLocked,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.NotResponding,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.HibernateVm, VdcActionType.MigrateVm,
                        VdcActionType.RemoveVm, VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));

        vmMatrix.put(
                VMStatus.Unassigned,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        vmMatrix.put(
                VMStatus.Unknown,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                        VdcActionType.RemoveVmInterface, VdcActionType.CancelMigrateVm)));
        _matrix.put(VM.class, vmMatrix);

        HashMap<Enum<?>, HashSet<VdcActionType>> vmTemplateMatrix =
                new HashMap<Enum<?>, HashSet<VdcActionType>>();
        vmTemplateMatrix.put(
                VmTemplateStatus.Locked,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.RemoveVmTemplate,
                        VdcActionType.ExportVmTemplate,
                        VdcActionType.MoveOrCopyTemplate, VdcActionType.ImportVmTemplate)));
        vmTemplateMatrix.put(
                VmTemplateStatus.Illegal,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.ExportVmTemplate,
                        VdcActionType.MoveOrCopyTemplate, VdcActionType.ImportVmTemplate)));
        _matrix.put(VmTemplate.class, vmTemplateMatrix);

        HashMap<Enum<?>, HashSet<VdcActionType>> storageDomainMatrix =
                new HashMap<Enum<?>, HashSet<VdcActionType>>();
        storageDomainMatrix.put(
                StorageDomainStatus.Active,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.ActivateStorageDomain)));
        storageDomainMatrix.put(
                StorageDomainStatus.InActive,
                new HashSet<VdcActionType>(Arrays
                        .asList(VdcActionType.DeactivateStorageDomain)));
        storageDomainMatrix.put(
                StorageDomainStatus.Locked,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomain, VdcActionType.ActivateStorageDomain)));
        storageDomainMatrix.put(
                StorageDomainStatus.Unattached,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomain, VdcActionType.ActivateStorageDomain)));
        storageDomainMatrix.put(
                StorageDomainStatus.Uninitialized,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomain, VdcActionType.ActivateStorageDomain)));
        storageDomainMatrix.put(
                StorageDomainStatus.Unknown,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.DetachStorageDomainFromPool,
                        VdcActionType.DeactivateStorageDomain)));
        storageDomainMatrix.put(
                StorageDomainStatus.Maintenance,
                new HashSet<VdcActionType>(Arrays.asList(VdcActionType.DeactivateStorageDomain)));
        _matrix.put(storage_domains.class, storageDomainMatrix);
    }

    public static boolean CanExecute(List<?> entities, Class<?> type, VdcActionType action) {
        if (_matrix.containsKey(type)) {
            for (Object a : entities) {
                if (a.getClass() == type && _matrix.get(type).containsKey(GetStatusProperty(a))
                        && _matrix.get(type).get(GetStatusProperty(a)).contains(action)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Enum<?> GetStatusProperty(Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity.getClass().getName().endsWith("VDS")) {
            return (entity instanceof VDS ?
                    ((VDS) entity).getstatus() :
                    null);
        } else if (entity.getClass().getName().endsWith("VM")) {
            return (entity instanceof VM ?
                    ((VM) entity).getStatus() :
                    null);
        } else if (entity.getClass().getName().endsWith("VmTemplate")) {
            return (entity instanceof VmTemplate ?
                    ((VmTemplate) entity).getstatus() :
                    null);

        } else if (entity instanceof storage_domains) {
            StorageDomainStatus status = ((storage_domains) entity).getstatus();
            return status != null ? status : StorageDomainStatus.Uninitialized;
        }

        throw new NotImplementedException();
    }

}
