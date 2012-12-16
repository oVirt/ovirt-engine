package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public interface IVdsEventListener {
    void vdsNotResponding(VDS vds); // BLL

    void vdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, boolean saveToDb,
            Guid domainId); // BLL

    void vdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, boolean saveToDb,
            Guid domainId,
            Map<String, String> customLogValues); // BLL

    void vdsMovedToMaintanance(VDS vds); // BLL

    void storageDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL

    void masterDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL

    /**
     * Temoporary patch. Vitaly todo: fix it
     *
     * @param vmId
     */
    void processOnVmStop(Guid vmId);

    boolean vdsUpEvent(Guid vdsId);

    void processOnClientIpChange(VDS vds, Guid vmId);

    void processOnCpuFlagsChange(Guid vdsId);

    void processOnVmPoweringUp(Guid vds_id, Guid vmid, String display_ip, int display_port);

    void handleVdsVersion(Guid vdsId);

    void rerun(Guid vmId);

    void runningSucceded(Guid vmId);

    void removeAsyncRunningCommand(Guid vmId);

    // void VdsNetworkConfigurationChanged(VDS vds);

    void storagePoolUpEvent(storage_pool storagePool, boolean isNewSpm);


    void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error);

    void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error, TransactionScopeOption transactionScopeOption);

    void storagePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status);

    void runFailedAutoStartVM(Guid vmId);

    boolean restartVds(Guid vdsId);
}
