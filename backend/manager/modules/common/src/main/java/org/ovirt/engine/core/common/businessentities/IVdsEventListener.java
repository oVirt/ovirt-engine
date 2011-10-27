package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//[ServiceContract, ServiceKnownType(typeof(VM)), ServiceKnownType(typeof(AuditLog)), ServiceKnownType(typeof(VDS)), ServiceKnownType(typeof(VmTemplate)), ServiceKnownType(typeof(AdUser)), ServiceKnownType(typeof(ad_groups)), ServiceKnownType(typeof(DbUser)), ServiceKnownType(typeof(vm_pools)), ServiceKnownType(typeof(VDSGroup)), ServiceKnownType(typeof(storage_pool)), ServiceKnownType(typeof(storage_domains)), ServiceKnownType(typeof(SessionState))]
public interface IVdsEventListener {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void VdsNotResponding(VDS vds); // BLL
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:

    void VdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, boolean saveToDb,
            Guid domainId); // BLL
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:

    void VdsMovedToMaintanance(Guid vdsId); // BLL
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:

    void StorageDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:

    void MasterDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL

    /**
     * Temoporary patch. Vitaly todo: fix it
     *
     * @param vmId
     */
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void ProcessOnVmStop(Guid vmId);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void VdsUpEvent(Guid vdsId);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void ProcessOnClientIpChange(VDS vds, Guid vmId);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void ProcessOnCpuFlagsChange(Guid vdsId);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void ProcessOnVmPoweringUp(Guid vds_id, Guid vmid, String display_ip, int display_port);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void Rerun(Guid vmId);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void RunningSucceded(Guid vmId);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void RemoveAsyncRunningCommand(Guid vmId);

    // [OperationContract(IsOneWay = true)]
    // void VdsNetworkConfigurationChanged(VDS vds);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void StoragePoolUpEvent(storage_pool storagePool, boolean isNewSpm);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:

    void StoragePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error);

    void StoragePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error, TransactionScopeOption transactionScopeOption);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void StoragePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsOneWay = true)]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void RunFailedAutoStartVM(Guid vmId);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    boolean RestartVds(Guid vdsId);
}
