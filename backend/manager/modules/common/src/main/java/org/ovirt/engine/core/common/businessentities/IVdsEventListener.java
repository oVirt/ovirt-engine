package org.ovirt.engine.core.common.businessentities;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public interface IVdsEventListener {
    void vdsNotResponding(VDS vds, boolean executeSshSoftFencing, long lastUpdate); // BLL

    void vdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, Guid domainId); // BLL

    void vdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, Guid domainId,
            Map<String, String> customLogValues); // BLL

    void vdsMovedToMaintenance(VDS vds); // BLL

    EventResult storageDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL

    EventResult masterDomainNotOperational(Guid storageDomainId, Guid storagePoolId, boolean isReconstructToInactiveDomains, boolean canReconstructToCurrentMaster); // BLL

    void processOnVmStop(Collection<Guid> vmIds);

    boolean vdsUpEvent(VDS vds);

    boolean connectHostToDomainsInActiveOrUnknownStatus(VDS vds);

    void processOnClientIpChange(VDS vds, Guid vmId);

    void processOnCpuFlagsChange(Guid vdsId);

    void processOnVmPoweringUp(Guid vds_id, Guid vmid, String display_ip, int display_port);

    void handleVdsVersion(Guid vdsId);

    void rerun(Guid vmId);

    void runningSucceded(Guid vmId);

    void removeAsyncRunningCommand(Guid vmId);

    void storagePoolUpEvent(StoragePool storagePool);


    void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error);

    void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error, TransactionScopeOption transactionScopeOption);

    void storagePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status);

    void runFailedAutoStartVMs(List<Guid> vmIds);

    void addExternallyManagedVms(List<VmStatic> externalVmList);

    void handleVdsMaintenanceTimeout(VDS vds);

    /**
     * update host's scheduling related properties
     *
     * @param vds
     */
    void updateSchedulingStats(VDS vds); // BLL

    void syncLunsInfoForBlockStorageDomain(final Guid storageDomainId, final Guid vdsId);

    /**
     * Updates VMs QoS
     *
     * @param vmIds
     * @param vdsId
     */
    void updateSlaPolicies(List<Guid> vmIds, Guid vdsId);

    public void destroyVms(List<Pair<VM, Guid>> vmsToDestroy);
}
