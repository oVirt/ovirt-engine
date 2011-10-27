package org.ovirt.engine.core.common;

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
    private static java.util.Map<java.lang.Class<?>, java.util.Map<Enum<?>, java.util.HashSet<VdcActionType>>> _matrix =
            new java.util.HashMap<java.lang.Class<?>, java.util.Map<Enum<?>, java.util.HashSet<VdcActionType>>>();

    static {
        // this matrix contains the actions that CANNOT run per status
        // ("black list")
        java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>> vdsMatrix =
                new java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>>();
        vdsMatrix.put(
                VDSStatus.Maintenance,
                new java.util.HashSet<VdcActionType>(java.util.Arrays
                        .asList(new VdcActionType[] { VdcActionType.MaintananceVds,
                                VdcActionType.ClearNonResponsiveVdsVms, VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.Up,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.ActivateVds, VdcActionType.RemoveVds, VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.StartVds, VdcActionType.StopVds })));
        vdsMatrix.put(
                VDSStatus.Error,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.RemoveVds, VdcActionType.ClearNonResponsiveVdsVms, VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.Installing,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.RemoveVds, VdcActionType.ActivateVds, VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.MaintananceVds, VdcActionType.StartVds,
                        VdcActionType.StopVds })));
        vdsMatrix.put(
                VDSStatus.NonResponsive,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.RemoveVds, VdcActionType.ActivateVds, VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.PreparingForMaintenance,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.RemoveVds, VdcActionType.MaintananceVds, VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.Reboot,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.ActivateVds, VdcActionType.RemoveVds, VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.MaintananceVds })));
        vdsMatrix.put(
                VDSStatus.Unassigned,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.ActivateVds, VdcActionType.RemoveVds, VdcActionType.MaintananceVds,
                        VdcActionType.ClearNonResponsiveVdsVms, VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.Initializing,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.ActivateVds, VdcActionType.RemoveVds, VdcActionType.ClearNonResponsiveVdsVms,
                        VdcActionType.ApproveVds, VdcActionType.MaintananceVds })));
        vdsMatrix.put(
                VDSStatus.NonOperational,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.RemoveVds, VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.PendingApproval,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.UpdateVds, VdcActionType.ActivateVds, VdcActionType.MaintananceVds,
                        VdcActionType.AttachVdsToTag, VdcActionType.ClearNonResponsiveVdsVms })));
        vdsMatrix.put(
                VDSStatus.InstallFailed,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.Problematic,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.MaintananceVds, VdcActionType.RemoveVds, VdcActionType.ActivateVds,
                        VdcActionType.ApproveVds })));
        vdsMatrix.put(
                VDSStatus.Down,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.ActivateVds, VdcActionType.ApproveVds })));
        _matrix.put(VDS.class, vdsMatrix);

        java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>> vmMatrix =
                new java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>>();
        vmMatrix.put(
                VMStatus.WaitForLaunch,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.HibernateVm, VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.Up,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.PoweringDown,
                new java.util.HashSet<VdcActionType>(java.util.Arrays
                        .asList(new VdcActionType[] { VdcActionType.HibernateVm, VdcActionType.RunVm,
                                VdcActionType.RunVmOnce,
                                VdcActionType.AddVmTemplate, VdcActionType.RemoveVm, VdcActionType.MigrateVm,
                                VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                                VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                                VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.PoweringUp,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.HibernateVm, VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.RebootInProgress,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.HibernateVm, VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.MigratingFrom,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.ExportVm,
                        VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.PoweredDown,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.HibernateVm, VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.AddVmTemplate, VdcActionType.RemoveVm,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));

        vmMatrix.put(
                VMStatus.Suspended,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.HibernateVm, VdcActionType.AddVmTemplate, VdcActionType.RunVmOnce,
                        VdcActionType.MigrateVm, VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.RemoveVm,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.Paused,
                new java.util.HashSet<VdcActionType>(java.util.Arrays
                        .asList(new VdcActionType[] { VdcActionType.RemoveVm, VdcActionType.HibernateVm,
                                VdcActionType.AddVmTemplate, VdcActionType.RunVmOnce, VdcActionType.ExportVm,
                                VdcActionType.MoveVm, VdcActionType.ImportVm,
                                VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface,
                                VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.SavingState,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.RestoringState,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));

        vmMatrix.put(
                VMStatus.Down,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.StopVm, VdcActionType.ShutdownVm, VdcActionType.HibernateVm,
                        VdcActionType.MigrateVm, VdcActionType.ChangeDisk })));
        vmMatrix.put(
                VMStatus.ImageIllegal,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.AddVmTemplate,
                        VdcActionType.ExportVm, VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.ImageLocked,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.NotResponding,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.HibernateVm, VdcActionType.MigrateVm,
                        VdcActionType.RemoveVm, VdcActionType.AddVmTemplate, VdcActionType.ExportVm,
                        VdcActionType.MoveVm, VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));

        vmMatrix.put(
                VMStatus.Unassigned,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        vmMatrix.put(
                VMStatus.Unknown,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] { VdcActionType.RunVm,
                        VdcActionType.RunVmOnce, VdcActionType.StopVm, VdcActionType.ShutdownVm,
                        VdcActionType.HibernateVm, VdcActionType.MigrateVm, VdcActionType.RemoveVm,
                        VdcActionType.AddVmTemplate, VdcActionType.ExportVm, VdcActionType.MoveVm,
                        VdcActionType.ImportVm, VdcActionType.ChangeDisk,
                        VdcActionType.AddVmInterface, VdcActionType.UpdateVmInterface, VdcActionType.RemoveVmInterface })));
        _matrix.put(VM.class, vmMatrix);

        java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>> vmTemplateMatrix =
                new java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>>();
        vmTemplateMatrix.put(
                VmTemplateStatus.Locked,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.RemoveVmTemplate, VdcActionType.ExportVmTemplate,
                        VdcActionType.MoveOrCopyTemplate, VdcActionType.ImportVmTemplate })));
        vmTemplateMatrix.put(
                VmTemplateStatus.Illegal,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.ExportVmTemplate, VdcActionType.MoveOrCopyTemplate,
                        VdcActionType.ImportVmTemplate })));
        _matrix.put(VmTemplate.class, vmTemplateMatrix);

        java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>> storageDomainMatrix =
                new java.util.HashMap<Enum<?>, java.util.HashSet<VdcActionType>>();
        storageDomainMatrix.put(
                StorageDomainStatus.Active,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.DetachStorageDomainFromPool, VdcActionType.ActivateStorageDomain })));
        storageDomainMatrix.put(
                StorageDomainStatus.InActive,
                new java.util.HashSet<VdcActionType>(java.util.Arrays
                        .asList(new VdcActionType[] { VdcActionType.DeactivateStorageDomain })));
        storageDomainMatrix.put(
                StorageDomainStatus.Locked,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.DetachStorageDomainFromPool, VdcActionType.DeactivateStorageDomain,
                        VdcActionType.ActivateStorageDomain })));
        storageDomainMatrix.put(
                StorageDomainStatus.Unattached,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.DetachStorageDomainFromPool, VdcActionType.DeactivateStorageDomain,
                        VdcActionType.ActivateStorageDomain })));
        storageDomainMatrix.put(
                StorageDomainStatus.Uninitialized,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.DetachStorageDomainFromPool, VdcActionType.DeactivateStorageDomain,
                        VdcActionType.ActivateStorageDomain })));
        storageDomainMatrix.put(
                StorageDomainStatus.Unknown,
                new java.util.HashSet<VdcActionType>(java.util.Arrays.asList(new VdcActionType[] {
                        VdcActionType.DetachStorageDomainFromPool, VdcActionType.DeactivateStorageDomain })));
        _matrix.put(storage_domains.class, storageDomainMatrix);
        // var userMatrix = new Dictionary<Enum<?>, HashSet<VdcActionType>>();
        // _matrix.Add(SearchType.DBUser, userMatrix);
    }

    public static boolean CanExecute(java.util.List<?> entities, java.lang.Class<?> type, VdcActionType action) {
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
        // C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (entity.GetType().Name)
        if (entity == null) {
            return null;
        }

        // ORIGINAL LINE: case "VDS":
        if (entity.getClass().getName().endsWith("VDS")) {
            return (entity instanceof VDS ?
                    ((VDS) entity).getstatus() :
                    null);
        }

        // ORIGINAL LINE: case "VM":
        else if (entity.getClass().getName().endsWith("VM")) {
            return (entity instanceof VM ?
                    ((VM) entity).getstatus() :
                    null);
        }

        // ORIGINAL LINE: case "VmTemplate":
        else if (entity.getClass().getName().endsWith("VmTemplate")) {
            return (entity instanceof VmTemplate ?
                    ((VmTemplate) entity).getstatus() :
                    null);

        }
        // ORIGINAL LINE: case "storage_domains":
        else if (entity instanceof storage_domains) {
            StorageDomainStatus status = ((storage_domains) entity).getstatus();
            return status != null ? status : StorageDomainStatus.Uninitialized;
            // case DBUser:
            // return (entity as DbUser).status;
        }

        throw new NotImplementedException();
    }

}
