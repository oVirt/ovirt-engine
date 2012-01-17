package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public interface IVdsEventListener {
    void VdsNotResponding(VDS vds); // BLL

    void VdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, boolean saveToDb,
            Guid domainId); // BLL

    void VdsMovedToMaintanance(Guid vdsId); // BLL

    void StorageDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL

    void MasterDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL

    /**
     * Temoporary patch. Vitaly todo: fix it
     *
     * @param vmId
     */
    void ProcessOnVmStop(Guid vmId);

    void VdsUpEvent(Guid vdsId);

    void ProcessOnClientIpChange(VDS vds, Guid vmId);

    void ProcessOnCpuFlagsChange(Guid vdsId);

    void ProcessOnVmPoweringUp(Guid vds_id, Guid vmid, String display_ip, int display_port);

    void Rerun(Guid vmId);

    void RunningSucceded(Guid vmId);

    void RemoveAsyncRunningCommand(Guid vmId);

    // void VdsNetworkConfigurationChanged(VDS vds);

    void StoragePoolUpEvent(storage_pool storagePool, boolean isNewSpm);


    void StoragePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error);

    void StoragePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error, TransactionScopeOption transactionScopeOption);

    void StoragePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status);

    void RunFailedAutoStartVM(Guid vmId);

    boolean RestartVds(Guid vdsId);
}
