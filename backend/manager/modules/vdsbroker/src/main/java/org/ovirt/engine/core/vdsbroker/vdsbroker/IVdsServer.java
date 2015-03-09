package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHookContentInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHooksListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHostsPubKeyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServersListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServicesReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTaskInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTasksListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepConfigListXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatusDetailForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeOptionsInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeProfileInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeSnapshotConfigReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeSnapshotCreateReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeSnapshotInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeTaskReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.OneStorageDeviceReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.StorageDeviceListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfoReturnForXmlRpc;


public interface IVdsServer {
    HttpClient getHttpClient();

    OneVmReturnForXmlRpc create(Map createInfo);

    StatusOnlyReturnForXmlRpc destroy(String vmId);

    StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message);

    StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message, boolean reboot);

    OneVmReturnForXmlRpc pause(String vmId);

    StatusOnlyReturnForXmlRpc hibernate(String vmId, String hiberVolHandle);

    OneVmReturnForXmlRpc resume(String vmId);

    VMListReturnForXmlRpc list();

    VMListReturnForXmlRpc list(String isFull, String[] vmIds);

    VDSInfoReturnForXmlRpc getCapabilities();

    VDSInfoReturnForXmlRpc getHardwareInfo();

    VDSInfoReturnForXmlRpc getVdsStats();

    StatusOnlyReturnForXmlRpc setMOMPolicyParameters(Map<String, Object> key_value_store);

    StatusOnlyReturnForXmlRpc setHaMaintenanceMode(String mode, boolean enabled);

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
             String action, String secured, String options,  Map<String, Object> fencingPolicy);

    ServerConnectionStatusReturnForXmlRpc connectStorageServer(int serverType, String spUUID, Map<String, String>[] args);

    ServerConnectionStatusReturnForXmlRpc disconnectStorageServer(int serverType, String spUUID,
            Map<String, String>[] args);

    ServerConnectionListReturnForXmlRpc getStorageConnectionsList(String spUUID);

    StatusOnlyReturnForXmlRpc createStorageDomain(int domainType, String sdUUID, String domainName, String arg,
            int storageType, String storageFormatType);

    StatusOnlyReturnForXmlRpc formatStorageDomain(String sdUUID);

    StatusOnlyReturnForXmlRpc connectStoragePool(String spUUID, int hostSpmId, String SCSIKey, String masterdomainId,
                                                 int masterVersion, Map<String, String> storageDomains);

    StatusOnlyReturnForXmlRpc disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    // The poolType parameter is ignored by VDSM
    StatusOnlyReturnForXmlRpc createStoragePool(int poolType, String spUUID, String poolName, String msdUUID,
            String[] domList, int masterVersion, String lockPolicy, int lockRenewalIntervalSec, int leaseTimeSec,
            int ioOpTimeoutSec, int leaseRetries);

    StatusOnlyReturnForXmlRpc reconstructMaster(String spUUID, String poolName, String masterDom,
            Map<String, String> domDict, int masterVersion, String lockPolicy, int lockRenewalIntervalSec,
            int leaseTimeSec, int ioOpTimeoutSec, int leaseRetries, int hostSpmId);

    OneStorageDomainStatsReturnForXmlRpc getStorageDomainStats(String sdUUID);

    OneStorageDomainInfoReturnForXmlRpc getStorageDomainInfo(String sdUUID);

    StorageDomainListReturnForXmlRpc getStorageDomainsList(String spUUID, int domainType, String poolType, String path);

    FileStatsReturnForXmlRpc getIsoList(String spUUID);

    OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList);

    OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList, boolean force);

    VGListReturnForXmlRpc getVGList();

    OneVGReturnForXmlRpc getVGInfo(String vgUUID);

    LUNListReturnForXmlRpc getDeviceList(int storageType);


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

    StatusOnlyReturnForXmlRpc refreshStoragePool(String spUUID, String msdUUID, int masterVersion);

    TaskStatusReturnForXmlRpc getTaskStatus(String taskUUID);

    TaskStatusListReturnForXmlRpc getAllTasksStatuses();

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

    FutureTask<Map<String, Object>> timeBoundPoll(long timeout, TimeUnit unit);

    StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks);

    StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory);

    AlignmentScanReturnForXmlRpc getDiskAlignment(String vmId, Map<String, String> driveSpecs);

    ImageSizeReturnForXmlRpc diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize);

    StatusOnlyReturnForXmlRpc merge(String vmId, Map<String, String> drive,
            String baseVolUUID, String topVolUUID, String bandwidth, String jobUUID);

    // Gluster vdsm Commands
    OneUuidReturnForXmlRpc glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList);

    OneUuidReturnForXmlRpc glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeSet(String volumeName, String key, String value);

    StatusOnlyReturnForXmlRpc glusterVolumeStart(String volumeName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeStop(String volumeName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeDelete(String volumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeReset(String volumeName, String volumeOption, Boolean force);

    GlusterVolumeOptionsInfoReturnForXmlRpc glusterVolumeSetOptionsList();

    GlusterTaskInfoReturnForXmlRpc glusterVolumeRemoveBricksStart(String volumeName,
            String[] brickList,
            int replicaCount,
            Boolean forceRemove);

    GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBricksStop(String volumeName,
            String[] brickList,
            int replicaCount);

    StatusOnlyReturnForXmlRpc glusterVolumeRemoveBricksCommit(String volumeName,
            String[] brickList,
            int replicaCount);

    StatusOnlyReturnForXmlRpc glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount);

    StatusOnlyReturnForXmlRpc glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount,
            boolean force);

    GlusterTaskInfoReturnForXmlRpc glusterVolumeRebalanceStart(String volumeName, Boolean fixLayoutOnly, Boolean force);

    GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStop(String volumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeReplaceBrickStart(String volumeName,
            String existingBrickDir,
            String newBrickDir);

    StatusOnlyReturnForXmlRpc glusterHostRemove(String hostName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterHostAdd(String hostName);

    GlusterServersListReturnForXmlRpc glusterServersList();

    GlusterHostsPubKeyReturnForXmlRpc glusterGeoRepKeysGet();

    StatusOnlyReturnForXmlRpc glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String remoteUserName);

    StatusOnlyReturnForXmlRpc glusterGeoRepMountBrokerSetup(String remoteVolumeName, String remoteUserName, String remoteGroupName);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionCreate(String volumeName, String remoteHost, String remoteVolumeName, String remoteUserName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionDelete(String volumeName, String remoteHost, String remoteVolumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStop(String volumeName, String remoteHost, String remoteVolumeName, Boolean force);

    StatusOnlyReturnForXmlRpc diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk);

    StatusOnlyReturnForXmlRpc diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk);

    StatusOnlyReturnForXmlRpc glusterVolumeProfileStart(String volumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeProfileStop(String volumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStart(String volumeName, String remoteHost, String remoteVolumeName, Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionPause(String masterVolumeName, String slaveHost, String slaveVolumeName, boolean force);

    GlusterVolumeStatusReturnForXmlRpc glusterVolumeStatus(Guid clusterId,
            String volumeName,
            String brickName,
            String volumeStatusOption);

    GlusterVolumesListReturnForXmlRpc glusterVolumesList(Guid clusterId);

    GlusterVolumeProfileInfoReturnForXmlRpc glusterVolumeProfileInfo(Guid clusterId, String volumeName, boolean nfs);

    StatusOnlyReturnForXmlRpc glusterHookEnable(String glusterCommand, String stage, String hookName);

    StatusOnlyReturnForXmlRpc glusterHookDisable(String glusterCommand, String stage, String hookName);

    GlusterHooksListReturnForXmlRpc glusterHooksList();

    OneUuidReturnForXmlRpc glusterHostUUIDGet();

    GlusterServicesReturnForXmlRpc glusterServicesList(Guid serverId, String[] serviceNames);

    GlusterHookContentInfoReturnForXmlRpc glusterHookRead(String glusterCommand, String stage, String hookName);

    StatusOnlyReturnForXmlRpc glusterHookUpdate(String glusterCommand, String stage, String hookName, String content, String checksum);

    StatusOnlyReturnForXmlRpc glusterHookAdd(String glusterCommand, String stage, String hookName, String content, String checksum, Boolean enabled);

    StatusOnlyReturnForXmlRpc glusterHookRemove(String glusterCommand, String stage, String hookName);

    GlusterServicesReturnForXmlRpc glusterServicesAction(Guid serverId, String [] serviceList, String actionType);

    StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID);

    GlusterTasksListReturnForXmlRpc glusterTasksList();

    GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStatus(String volumeName);

    GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList();

    GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName);

    GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName);

    GlusterVolumeGeoRepStatusDetailForXmlRpc glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigSet(String volumeName, String slaveHost, String slaveVolumeName, String configKey, String configValue);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigReset(String volumeName, String slaveHost, String slaveVolumeName, String configKey);

    GlusterVolumeGeoRepConfigListXmlRpc glusterVolumeGeoRepConfigList(String volumeName, String slaveHost, String slaveVolumeName);

    GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList);

    StatusOnlyReturnForXmlRpc setNumberOfCpus(String vmId, String numberOfCpus);

    StatusOnlyReturnForXmlRpc updateVmPolicy(Map info);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionResume(String volumeName, String slaveHostName, String slaveVolumeName, boolean force);

    GlusterVolumeSnapshotInfoReturnForXmlRpc glusterSnapshotInfo(Guid clusterId, String volumeName);

    GlusterVolumeSnapshotConfigReturnForXmlRpc glusterSnapshotConfigList(Guid clusterId);

    StatusOnlyReturnForXmlRpc glusterSnapshotDelete(String snapshotName);

    StatusOnlyReturnForXmlRpc glusterVolumeSnapshotDelete(String volumeName);

    StatusOnlyReturnForXmlRpc glusterSnapshotActivate(String snapshotName, boolean force);

    StatusOnlyReturnForXmlRpc glusterSnapshotDeactivate(String snapshotName);

    StatusOnlyReturnForXmlRpc glusterSnapshotRestore(String snapshotName);

    GlusterVolumeSnapshotCreateReturnForXmlRpc glusterSnapshotCreate(String volumeName,
            String snapshotName,
            String description,
            boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeSnapshotConfigSet(String volumeName, String cfgName, String cfgValue);

    StatusOnlyReturnForXmlRpc glusterSnapshotConfigSet(String cfgName, String cfgValue);

    OneStorageDeviceReturnForXmlRpc glusterCreateBrick(String lvName,
            String mountPoint,
            Map<String, Object> raidParams,
            String fsType,
            String[] storageDevices);

    StorageDeviceListReturnForXmlRpc glusterStorageDeviceList();
}
