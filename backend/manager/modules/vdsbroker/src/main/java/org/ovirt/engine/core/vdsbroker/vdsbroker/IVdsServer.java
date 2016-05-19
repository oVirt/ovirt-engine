package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.security.cert.Certificate;
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
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesHealInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.OneStorageDeviceReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.StorageDeviceListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfoReturnForXmlRpc;


public interface IVdsServer {
    void close();

    List<Certificate> getPeerCertificates();

    HttpClient getHttpClient();

    OneVmReturnForXmlRpc create(Map createInfo);

    StatusOnlyReturnForXmlRpc createVolumeContainer(String jobId, Map<String, Object> createVolumeInfo);

    StatusOnlyReturnForXmlRpc copyData(Map src, Map dst, boolean collapse);

    StatusOnlyReturnForXmlRpc allocateVolume(String spUUID, String sdUUID, String imgGUID, String volUUID, String size);

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

    StatusOnlyReturnForXmlRpc add_image_ticket(String ticketId, String[] ops, long timeout,
                                               long size, String url);

    StatusOnlyReturnForXmlRpc remove_image_ticket(String ticketId);

    StatusOnlyReturnForXmlRpc extend_image_ticket(String ticketId, long timeout);

    OneMapReturnForXmlRpc get_image_transfer_session_stats(String ticketId);

    StatusOnlyReturnForXmlRpc desktopLogin(String vmId, String domain, String user, String password);

    StatusOnlyReturnForXmlRpc desktopLogoff(String vmId, String force);

    VMInfoListReturnForXmlRpc getVmStats(String vmId);

    VMInfoListReturnForXmlRpc getAllVmStats();

    HostDevListReturnForXmlRpc hostDevListByCaps();

    StatusOnlyReturnForXmlRpc migrate(Map<String, Object> migrationInfo);

    MigrateStatusReturnForXmlRpc migrateStatus(String vmId);

    StatusOnlyReturnForXmlRpc migrateCancel(String vmId);

    PrepareImageReturnForXmlRpc prepareImage(String spID, String sdID, String imageID, String volumeID, boolean allowIllegal);

    StatusReturnForXmlRpc teardownImage(String spId, String sdId, String imgGroupId, String imgId);

    StatusReturnForXmlRpc verifyUntrustedVolume(String spId, String sdId, String imgGroupId, String imgId);

    OneVmReturnForXmlRpc changeDisk(String vmId, String imageLocation);

    OneVmReturnForXmlRpc changeDisk(String vmId, Map<String, Object> driveSpec);

    OneVmReturnForXmlRpc changeFloppy(String vmId, String imageLocation);

    StatusOnlyReturnForXmlRpc monitorCommand(String vmId, String monitorCommand);

    StatusOnlyReturnForXmlRpc addNetwork(String bridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    StatusOnlyReturnForXmlRpc delNetwork(String bridge, String vlan, String bond, String[] nics);

    StatusOnlyReturnForXmlRpc editNetwork(String oldBridge, String newBridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    Future<Map<String, Object>> setupNetworks(Map networks,
            Map bonding,
            Map options,
            boolean isPolicyReset);

    StatusOnlyReturnForXmlRpc setSafeNetworkConfig();

    FenceStatusReturnForXmlRpc fenceNode(String ip, String port, String type, String user, String password,
             String action, String secured, String options,  Map<String, Object> fencingPolicy);

    ServerConnectionStatusReturnForXmlRpc connectStorageServer(int serverType, String spUUID, Map<String, String>[] args);

    ServerConnectionStatusReturnForXmlRpc disconnectStorageServer(int serverType, String spUUID,
            Map<String, String>[] args);

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

    OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList, boolean force);

    OneVGReturnForXmlRpc getVGInfo(String vgUUID);

    LUNListReturnForXmlRpc getDeviceList(int storageType, String[] devicesList, boolean checkStatus);

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

    HostJobsReturnForXmlRpc getHostJobs(String jobType, List<String> jobIds);

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

    StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen);

    AlignmentScanReturnForXmlRpc getDiskAlignment(String vmId, Map<String, String> driveSpecs);

    ImageSizeReturnForXmlRpc diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize);

    StatusOnlyReturnForXmlRpc merge(String vmId, Map<String, String> drive,
            String baseVolUUID, String topVolUUID, String bandwidth, String jobUUID);

    // Gluster vdsm Commands
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
            int stripeCount,
            boolean force);

    GlusterTaskInfoReturnForXmlRpc glusterVolumeRebalanceStart(String volumeName, Boolean fixLayoutOnly, Boolean force);

    GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStop(String volumeName);

    StatusOnlyReturnForXmlRpc glusterVolumeReplaceBrickCommitForce(String volumeName,
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

    GlusterVolumesListReturnForXmlRpc glusterVolumeInfo(Guid clusterId, String volumeName);

    GlusterVolumesHealInfoReturnForXmlRpc glusterVolumeHealInfo(String volumeName);

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

    BooleanReturnForXmlRpc glusterVolumeEmptyCheck(String volumeName);

    GlusterHostsPubKeyReturnForXmlRpc glusterGeoRepKeysGet();

    StatusOnlyReturnForXmlRpc glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String userName);

    StatusOnlyReturnForXmlRpc glusterGeoRepMountBrokerSetup(String remoteVolumeName,
            String userName,
            String remoteGroupName, Boolean partial);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionCreate(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionDelete(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStop(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList();

    GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName);

    GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    GlusterVolumeGeoRepStatusDetailForXmlRpc glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStart(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionPause(String masterVolumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName,
            boolean force);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigSet(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String configValue,
            String userName);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigReset(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String userName);

    GlusterVolumeGeoRepConfigListXmlRpc glusterVolumeGeoRepConfigList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList);

    StatusOnlyReturnForXmlRpc setNumberOfCpus(String vmId, String numberOfCpus);

    StatusOnlyReturnForXmlRpc hotplugMemory(Map info);

    StatusOnlyReturnForXmlRpc updateVmPolicy(Map info);

    VMListReturnForXmlRpc getExternalVmList(String uri, String username, String password);

    OneVmReturnForXmlRpc getExternalVmFromOva(String ovaPath);

    StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionResume(String volumeName,
            String slaveHostName,
            String slaveVolumeName,
            String userName,
            boolean force);

    GlusterVolumeSnapshotInfoReturnForXmlRpc glusterVolumeSnapshotList(Guid clusterId, String volumeName);

    GlusterVolumeSnapshotConfigReturnForXmlRpc glusterSnapshotConfigList(Guid clusterId);

    StatusOnlyReturnForXmlRpc glusterSnapshotDelete(String snapshotName);

    StatusOnlyReturnForXmlRpc glusterVolumeSnapshotDeleteAll(String volumeName);

    StatusOnlyReturnForXmlRpc glusterSnapshotActivate(String snapshotName, boolean force);

    StatusOnlyReturnForXmlRpc glusterSnapshotDeactivate(String snapshotName);

    StatusOnlyReturnForXmlRpc glusterSnapshotRestore(String snapshotName);

    GlusterVolumeSnapshotCreateReturnForXmlRpc glusterVolumeSnapshotCreate(String volumeName,
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

    StatusOnlyReturnForXmlRpc hostdevChangeNumvfs(String deviceName, int numOfVfs);

    StatusOnlyReturnForXmlRpc convertVmFromExternalSystem(String url, String user, String password, Map<String, Object> vm, String jobUUID);

    StatusOnlyReturnForXmlRpc convertVmFromOva(String ovaPath, Map<String, Object> vm, String jobUUID);

    OvfReturnForXmlRpc getConvertedVm(String jobUUID);

    StatusOnlyReturnForXmlRpc deleteV2VJob(String jobUUID);

    StatusOnlyReturnForXmlRpc abortV2VJob(String jobUUID);

    StatusOnlyReturnForXmlRpc glusterSnapshotScheduleOverride(boolean force);

    StatusOnlyReturnForXmlRpc glusterSnapshotScheduleReset();

    StatusOnlyReturnForXmlRpc registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets);

    StatusOnlyReturnForXmlRpc unregisterSecrets(String[] libvirtSecretsUuids);

    StatusOnlyReturnForXmlRpc freeze(String vmId);

    StatusOnlyReturnForXmlRpc thaw(String vmId);

    StatusOnlyReturnForXmlRpc isolateVolume(String sdUUID, String srcImageID, String dstImageID, String volumeID);

    StatusOnlyReturnForXmlRpc wipeVolume(String sdUUID, String imgUUID, String volUUID);

    StatusOnlyReturnForXmlRpc refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID);

    VolumeInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID);

    StatusOnlyReturnForXmlRpc glusterStopProcesses();
}
