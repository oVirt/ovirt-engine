package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHookContentInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHooksListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServersListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServicesReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeOptionsInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeProfileInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.IsoListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;


public interface IVdsServer {
    OneVmReturnForXmlRpc create(Map createInfo);

    StatusOnlyReturnForXmlRpc destroy(String vmId);

    StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message);

    OneVmReturnForXmlRpc pause(String vmId);

    StatusOnlyReturnForXmlRpc hibernate(String vmId, String hiberVolHandle);

    OneVmReturnForXmlRpc reset(String vmId);

    OneVmReturnForXmlRpc resume(String vmId);

    VMListReturnForXmlRpc list();

    VMListReturnForXmlRpc list(String isFull, String[] vmIds);

    VDSInfoReturnForXmlRpc getCapabilities();

    VDSInfoReturnForXmlRpc getHardwareInfo();

    VDSInfoReturnForXmlRpc getVdsStats();

    StatusOnlyReturnForXmlRpc desktopLogin(String vmId, String domain, String user, String password);

    StatusOnlyReturnForXmlRpc desktopLogoff(String vmId, String force);

    VMInfoListReturnForXmlRpc getVmStats(String vmId);

    VMInfoListReturnForXmlRpc getAllVmStats();

    StatusOnlyReturnForXmlRpc migrate(Map<String, String> migrationInfo);

    StatusOnlyReturnForXmlRpc migrateStatus(String vmId);

    StatusOnlyReturnForXmlRpc migrateCancel(String vmId);

    OneVmReturnForXmlRpc changeDisk(String vmId, String imageLocation);

    OneVmReturnForXmlRpc changeFloppy(String vmId, String imageLocation);

    @Deprecated
    StatusOnlyReturnForXmlRpc heartBeat();

    StatusOnlyReturnForXmlRpc monitorCommand(String vmId, String monitorCommand);

    StatusOnlyReturnForXmlRpc sendHcCmdToDesktop(String vmId, String hcCommand);

    StatusOnlyReturnForXmlRpc setVmTicket(String vmId, String otp64, String sec);

    StatusOnlyReturnForXmlRpc setVmTicket(String vmId, String otp64, String sec, String connectionAction, Map<String, String> params);

    StatusOnlyReturnForXmlRpc startSpice(String vdsIp, int port, String ticket);

    StatusOnlyReturnForXmlRpc addNetwork(String bridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    StatusOnlyReturnForXmlRpc delNetwork(String bridge, String vlan, String bond, String[] nics);

    StatusOnlyReturnForXmlRpc editNetwork(String oldBridge, String newBridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    Future<Map<String, Object>> setupNetworks(Map networks,
            Map bonding,
            Map options);

    StatusOnlyReturnForXmlRpc setSafeNetworkConfig();

    FenceStatusReturnForXmlRpc fenceNode(String ip, String port, String type, String user, String password,
            String action, String secured, String options);

    ServerConnectionStatusReturnForXmlRpc connectStorageServer(int serverType, String spUUID, Map<String, String>[] args);

    ServerConnectionStatusReturnForXmlRpc disconnectStorageServer(int serverType, String spUUID,
            Map<String, String>[] args);

    ServerConnectionListReturnForXmlRpc getStorageConnectionsList(String spUUID);

    StatusOnlyReturnForXmlRpc validateStorageDomain(String sdUUID);

    StatusOnlyReturnForXmlRpc createStorageDomain(int domainType, String sdUUID, String domainName, String arg,
            int storageType, String storageFormatType);

    StatusOnlyReturnForXmlRpc formatStorageDomain(String sdUUID);

    StatusOnlyReturnForXmlRpc connectStoragePool(String spUUID, int hostSpmId, String SCSIKey, String masterdomainId,
            int masterVersion);

    StatusOnlyReturnForXmlRpc disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    StatusOnlyReturnForXmlRpc createStoragePool(int poolType, String spUUID, String poolName, String msdUUID,
            String[] domList, int masterVersion, String lockPolicy, int lockRenewalIntervalSec, int leaseTimeSec,
            int ioOpTimeoutSec, int leaseRetries);

    StatusOnlyReturnForXmlRpc reconstructMaster(String spUUID, String poolName, String masterDom,
            Map<String, String> domDict, int masterVersion, String lockPolicy, int lockRenewalIntervalSec,
            int leaseTimeSec, int ioOpTimeoutSec, int leaseRetries, int hostSpmId);

    OneStorageDomainStatsReturnForXmlRpc getStorageDomainStats(String sdUUID);

    OneStorageDomainInfoReturnForXmlRpc getStorageDomainInfo(String sdUUID);

    StorageDomainListReturnForXmlRpc getStorageDomainsList(String spUUID, int domainType, int poolType, String path);

    IsoListReturnForXmlRpc getIsoList(String spUUID);

    OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList);

    OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList, boolean force);

    VGListReturnForXmlRpc getVGList();

    OneVGReturnForXmlRpc getVGInfo(String vgUUID);

    LUNListReturnForXmlRpc getDeviceList(int storageType);

    OneLUNReturnForXmlRpc getDeviceInfo(String devGUID);

    DevicesVisibilityMapReturnForXmlRpc getDevicesVisibility(String[] devicesList);

    IQNListReturnForXmlRpc discoverSendTargets(Map<String, String> args);

    OneUuidReturnForXmlRpc spmStart(String spUUID,
            int prevID,
            String prevLVER,
            int recoveryMode,
            String SCSIFencing,
            int maxHostId,
            String storagePoolFormatType);

    StatusOnlyReturnForXmlRpc spmStop(String spUUID);

    SpmStatusReturnForXmlRpc spmStatus(String spUUID);

    StatusOnlyReturnForXmlRpc fenceSpmStorage(String spUUID, int prevID, String prevLVER);

    StatusOnlyReturnForXmlRpc refreshStoragePool(String spUUID, String msdUUID, int masterVersion);

    TaskStatusReturnForXmlRpc getTaskStatus(String taskUUID);

    TaskStatusListReturnForXmlRpc getAllTasksStatuses();

    TaskInfoReturnForXmlRpc getTaskInfo(String taskUUID);

    TaskInfoListReturnForXmlRpc getAllTasksInfo();

    StatusOnlyReturnForXmlRpc stopTask(String taskUUID);

    StatusOnlyReturnForXmlRpc clearTask(String taskUUID);

    StatusOnlyReturnForXmlRpc revertTask(String taskUUID);

    StatusOnlyReturnForXmlRpc hotplugDisk(Map info);

    StatusOnlyReturnForXmlRpc hotunplugDisk(Map info);

    StatusOnlyReturnForXmlRpc hotPlugNic(Map info);

    StatusOnlyReturnForXmlRpc hotUnplugNic(Map info);

    StatusOnlyReturnForXmlRpc vmUpdateDevice(String vmId, Map device);

    FutureTask<Map<String, Object>> poll();

    StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] snapParams);

    // Gluster vdsm Commands
    OneUuidReturnForXmlRpc glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList);

    StatusOnlyReturnForXmlRpc glusterVolumeSet(String volumeName, String key, String value);

    StatusOnlyReturnForXmlRpc glusterVolumeStart(String volumeName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeStop(String volumeName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeDelete(String volumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeReset(String volumeName, String volumeOption, Boolean force);

    GlusterVolumeOptionsInfoReturnForXmlRpc glusterVolumeSetOptionsList();

    StatusOnlyReturnForXmlRpc glusterVolumeRemoveBrickForce(String volumeName, String[] brickList, int replicCount);

    StatusOnlyReturnForXmlRpc glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount);

    StatusOnlyReturnForXmlRpc glusterVolumeRebalanceStart(String volumeName, Boolean fixLayoutOnly, Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeReplaceBrickStart(String volumeName,
            String existingBrickDir,
            String newBrickDir);

    StatusOnlyReturnForXmlRpc glusterHostRemove(String hostName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterHostAdd(String hostName);

    GlusterServersListReturnForXmlRpc glusterServersList();

    StatusOnlyReturnForXmlRpc diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk);

    StatusOnlyReturnForXmlRpc diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk);

    StatusOnlyReturnForXmlRpc glusterVolumeProfileStart(String volumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeProfileStop(String volumeName);

    GlusterVolumeStatusReturnForXmlRpc glusterVolumeStatus(Guid clusterId,
            String volumeName,
            String brickName,
            String volumeStatusOption);

    GlusterVolumesListReturnForXmlRpc glusterVolumesList(Guid clusterId);

    GlusterVolumeProfileInfoReturnForXmlRpc glusterVolumeProfileInfo(Guid clusterId, String volumeName);

    StatusOnlyReturnForXmlRpc glusterHookEnable(String glusterCommand, String stage, String hookName);

    StatusOnlyReturnForXmlRpc glusterHookDisable(String glusterCommand, String stage, String hookName);

    GlusterHooksListReturnForXmlRpc glusterHooksList();

    OneUuidReturnForXmlRpc glusterHostUUIDGet();

    GlusterServicesReturnForXmlRpc glusterServicesList(Guid serverId, String[] serviceNames);

    GlusterHookContentInfoReturnForXmlRpc glusterHookRead(String glusterCommand, String stage, String hookName);
}
