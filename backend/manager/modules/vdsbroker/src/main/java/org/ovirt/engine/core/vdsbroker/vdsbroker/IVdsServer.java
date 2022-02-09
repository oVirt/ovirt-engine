package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.security.cert.Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.CloseableHttpClient;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicket;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHookContentInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHooksListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHostsPubKeyReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterLocalLogicalVolumeListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterLocalPhysicalVolumeListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServersListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServicesReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTaskInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTasksListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVDOVolumeListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepConfigList;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatus;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatusDetail;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGlobalOptionsInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeOptionsInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeProfileInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeSnapshotConfigReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeSnapshotCreateReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeSnapshotInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeStatusReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeTaskReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesHealInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.OneStorageDeviceReturn;
import org.ovirt.engine.core.vdsbroker.gluster.StorageDeviceListReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfo;
import org.ovirt.engine.core.vdsbroker.irsbroker.UUIDListReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmBackupInfo;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointIds;
import org.ovirt.vdsm.jsonrpc.client.BrokerCommandCallback;

@SuppressWarnings("rawtypes")
public interface IVdsServer {
    void close();

    List<Certificate> getPeerCertificates();

    CloseableHttpClient getHttpClient();

    OneVmReturn create(Map createInfo);

    StatusOnlyReturn copyData(String jobId, Map src, Map dst, boolean copyBitmaps);

    StatusOnlyReturn updateVolume(String jobId, Map<?, ?> volumeInfo, Map<?, ?> volumeAttributes);

    StatusOnlyReturn moveDomainDevice(String jobId, Map<String, Object> moveParams);

    StatusOnlyReturn reduceDomain(String jobId, Map<String, Object> reduceParams);

    StatusOnlyReturn mergeSubchain(String jobId, Map<String, Object> subchainInfo, boolean mergeBitmaps);

    StatusOnlyReturn destroy(String vmId);

    StatusOnlyReturn shutdown(String vmId, String timeout, String message);

    StatusOnlyReturn shutdown(String vmId, String timeout, String message, boolean reboot);

    StatusOnlyReturn reset(String vmId);

    StatusOnlyReturn setDestroyOnReboot(String vmId);

    StatusOnlyReturn hibernate(String vmId, String hiberVolHandle);

    OneVmReturn resume(String vmId);

    VMListReturn list();

    VMListReturn fullList(List<String> vmIds);

    VDSInfoReturn getCapabilities();

    void getCapabilities(BrokerCommandCallback callback);

    VDSInfoReturn getHardwareInfo();

    void getHardwareInfo(BrokerCommandCallback callback);

    VDSInfoReturn getVdsStats();

    void getVdsStats(BrokerCommandCallback callback);

    StatusOnlyReturn setMOMPolicyParameters(Map<String, Object> key_value_store);

    StatusOnlyReturn setHaMaintenanceMode(String mode, boolean enabled);

    StatusOnlyReturn add_image_ticket(ImageTicket ticket);

    StatusOnlyReturn remove_image_ticket(String ticketId);

    StatusOnlyReturn extend_image_ticket(String ticketId, long timeout);

    ImageTicketInformationReturn getImageTicket(String ticketId);

    StatusOnlyReturn desktopLogin(String vmId, String domain, String user, String password);

    StatusOnlyReturn desktopLogoff(String vmId, String force);

    VMInfoListReturn getVmStats(String vmId);

    VMInfoListReturn getAllVmStats();

    VmExternalDataReturn getVmExternalData(String vmId, VmExternalDataKind kind, boolean forceUpdate);

    HostDevListReturn hostDevListByCaps();

    StatusOnlyReturn migrate(Map<String, Object> migrationInfo);

    MigrateStatusReturn migrateStatus(String vmId);

    StatusOnlyReturn migrateCancel(String vmId);

    PrepareImageReturn prepareImage(String spID, String sdID, String imageID, String volumeID, boolean allowIllegal);

    StatusReturn teardownImage(String spId, String sdId, String imgGroupId, String imgId);

    StatusReturn verifyUntrustedVolume(String spId, String sdId, String imgGroupId, String imgId);

    OneVmReturn changeDisk(String vmId, String imageLocation);

    OneVmReturn changeDisk(String vmId, Map<String, Object> driveSpec);

    StatusOnlyReturn addNetwork(String bridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    StatusOnlyReturn delNetwork(String bridge, String vlan, String bond, String[] nics);

    StatusOnlyReturn editNetwork(String oldBridge, String newBridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    Future<Map<String, Object>> setupNetworks(Map networks,
            Map bonding,
            Map options,
            boolean isPolicyReset);

    StatusOnlyReturn setSafeNetworkConfig();

    FenceStatusReturn fenceNode(String ip, String port, String type, String user, String password,
             String action, String secured, String options,  Map<String, Object> fencingPolicy);

    ServerConnectionStatusReturn connectStorageServer(int serverType, String spUUID, Map<String, String>[] args);

    ServerConnectionStatusReturn disconnectStorageServer(int serverType, String spUUID,
            Map<String, String>[] args);

    StatusOnlyReturn createStorageDomain(int domainType, String sdUUID, String domainName, String arg,
            int storageType, String storageFormatType, Integer blockSize, int maxHosts);

    StatusOnlyReturn formatStorageDomain(String sdUUID);

    StatusOnlyReturn connectStoragePool(String spUUID, int hostSpmId, String SCSIKey, String masterdomainId,
                                                 int masterVersion, Map<String, String> storageDomains);

    StatusOnlyReturn disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    // The poolType parameter is ignored by VDSM
    StatusOnlyReturn createStoragePool(int poolType, String spUUID, String poolName, String msdUUID,
            String[] domList, int masterVersion, String lockPolicy, int lockRenewalIntervalSec, int leaseTimeSec,
            int ioOpTimeoutSec, int leaseRetries);

    StatusOnlyReturn reconstructMaster(String spUUID, String poolName, String masterDom,
            Map<String, String> domDict, int masterVersion, String lockPolicy, int lockRenewalIntervalSec,
            int leaseTimeSec, int ioOpTimeoutSec, int leaseRetries, int hostSpmId);

    OneStorageDomainStatsReturn getStorageDomainStats(String sdUUID);

    OneStorageDomainInfoReturn getStorageDomainInfo(String sdUUID);

    StorageDomainListReturn getStorageDomainsList(String spUUID, int domainType, String poolType, String path);

    OneUuidReturn createVG(String sdUUID, String[] deviceList, boolean force);

    OneVGReturn getVGInfo(String vgUUID);

    LUNListReturn getDeviceList(int storageType, String[] devicesList, boolean checkStatus);

    DevicesVisibilityMapReturn getDevicesVisibility(String[] devicesList);

    IQNListReturn discoverSendTargets(Map<String, String> args);

    OneUuidReturn spmStart(String spUUID,
            int prevID,
            String prevLVER,
            int recoveryMode,
            String SCSIFencing,
            int maxHostId,
            String storagePoolFormatType);

    StatusOnlyReturn spmStop(String spUUID);

    SpmStatusReturn spmStatus(String spUUID);

    HostJobsReturn getHostJobs(String jobType, List<String> jobIds);

    TaskStatusReturn getTaskStatus(String taskUUID);

    TaskStatusListReturn getAllTasksStatuses();

    TaskInfoListReturn getAllTasksInfo();

    StatusOnlyReturn stopTask(String taskUUID);

    StatusOnlyReturn clearTask(String taskUUID);

    StatusOnlyReturn revertTask(String taskUUID);

    StatusOnlyReturn cleanStorageDomainMetaData(String sdUUID, String spUUID);

    StatusOnlyReturn hotplugDisk(Map info);

    StatusOnlyReturn hotunplugDisk(Map info);

    VmInfoReturn hotPlugNic(Map info);

    StatusOnlyReturn hotUnplugNic(Map info);

    StatusOnlyReturn vmUpdateDevice(String vmId, Map device);

    /**
     * @since engine 4.2.1
     * @since cluster compatibility version >= 4.2
     */
    @Deprecated
    FutureTask<Map<String, Object>> poll();

    /**
     * @since engine 4.2.1
     * @since cluster compatibility version >= 4.2
     */
    @Deprecated
    FutureTask<Map<String, Object>> timeBoundPoll(long timeout, TimeUnit unit);

    /**
     * poll2 performs Host.ping2. It is available for cluster compatibility >= 4.2 and replaces
     * poll() which performs Host.ping. poll2 is used by the engine to ascertain that vdsm is
     * reachable.
     */
    FutureTask<Map<String, Object>> timeBoundPoll2(long timeout, TimeUnit unit);

    /**
     * PollConfirmConnectivity performs Host.confirmConnectivity. It is a new verb for cluster
     * compatibility >= 4.2
     * PollConfirmConnectivity is used by vdsm to ascertain that the engine is reachable.
     */
    FutureTask<Map<String, Object>> timeBoundPollConfirmConnectivity(long timeout, TimeUnit unit);

    StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String jobUUID, int timeout);

    StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory, String jobUUID, int timeout);

    StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen, String jobUUID, int timeout);

    ImageSizeReturn diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize);

    StatusOnlyReturn merge(String vmId, Map<String, String> drive,
            String baseVolUUID, String topVolUUID, String bandwidth, String jobUUID);

    // Gluster vdsm Commands
    OneUuidReturn glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force,
            boolean arbiter);

    StatusOnlyReturn glusterVolumeSet(String volumeName, String key, String value);

    StatusOnlyReturn glusterVolumeStart(String volumeName, Boolean force);

    StatusOnlyReturn glusterVolumeStop(String volumeName, Boolean force);

    StatusOnlyReturn glusterVolumeDelete(String volumeName);

    StatusOnlyReturn glusterVolumeReset(String volumeName, String volumeOption, Boolean force);

    GlusterVolumeOptionsInfoReturn glusterVolumeSetOptionsList();

    GlusterVolumeGlobalOptionsInfoReturn glusterVolumeGlobalOptionsGet();

    GlusterTaskInfoReturn glusterVolumeRemoveBricksStart(String volumeName,
            String[] brickList,
            int replicaCount,
            Boolean forceRemove);

    GlusterVolumeTaskReturn glusterVolumeRemoveBricksStop(String volumeName,
            String[] brickList,
            int replicaCount);

    StatusOnlyReturn glusterVolumeRemoveBricksCommit(String volumeName,
            String[] brickList,
            int replicaCount);

    StatusOnlyReturn glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount,
            boolean force);

    GlusterTaskInfoReturn glusterVolumeRebalanceStart(String volumeName, Boolean fixLayoutOnly, Boolean force);

    GlusterVolumeTaskReturn glusterVolumeRebalanceStop(String volumeName);

    StatusOnlyReturn glusterVolumeReplaceBrickCommitForce(String volumeName,
            String existingBrickDir,
            String newBrickDir);

    StatusOnlyReturn glusterHostRemove(String hostName, Boolean force);

    StatusOnlyReturn glusterHostAdd(String hostName);

    GlusterServersListReturn glusterServersList();

    StatusOnlyReturn diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk);

    StatusOnlyReturn diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk);

    StatusOnlyReturn glusterVolumeProfileStart(String volumeName);

    StatusOnlyReturn glusterVolumeProfileStop(String volumeName);

    GlusterVolumeStatusReturn glusterVolumeStatus(Guid clusterId,
            String volumeName,
            String brickName,
            String volumeStatusOption);

    GlusterLocalLogicalVolumeListReturn glusterLogicalVolumeList();

    GlusterLocalPhysicalVolumeListReturn glusterPhysicalVolumeList();

    GlusterVDOVolumeListReturn glusterVDOVolumeList();

    GlusterVolumesListReturn glusterVolumesList(Guid clusterId);

    GlusterVolumesListReturn glusterVolumeInfo(Guid clusterId, String volumeName);

    GlusterVolumesHealInfoReturn glusterVolumeHealInfo(String volumeName);

    GlusterVolumeProfileInfoReturn glusterVolumeProfileInfo(Guid clusterId, String volumeName, boolean nfs);

    StatusOnlyReturn glusterHookEnable(String glusterCommand, String stage, String hookName);

    StatusOnlyReturn glusterHookDisable(String glusterCommand, String stage, String hookName);

    GlusterHooksListReturn glusterHooksList();

    OneUuidReturn glusterHostUUIDGet();

    GlusterServicesReturn glusterServicesList(Guid serverId, String[] serviceNames);

    GlusterHookContentInfoReturn glusterHookRead(String glusterCommand, String stage, String hookName);

    StatusOnlyReturn glusterHookUpdate(String glusterCommand, String stage, String hookName, String content, String checksum);

    StatusOnlyReturn glusterHookAdd(String glusterCommand, String stage, String hookName, String content, String checksum, Boolean enabled);

    StatusOnlyReturn glusterHookRemove(String glusterCommand, String stage, String hookName);

    GlusterServicesReturn glusterServicesAction(Guid serverId, String [] serviceList, String actionType);

    StoragePoolInfo getStoragePoolInfo(String spUUID);

    GlusterTasksListReturn glusterTasksList();

    GlusterVolumeTaskReturn glusterVolumeRebalanceStatus(String volumeName);

    BooleanReturn glusterVolumeEmptyCheck(String volumeName);

    GlusterHostsPubKeyReturn glusterGeoRepKeysGet();

    StatusOnlyReturn glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String userName);

    StatusOnlyReturn glusterGeoRepMountBrokerSetup(String remoteVolumeName,
            String userName,
            String remoteGroupName, Boolean partial);

    StatusOnlyReturn glusterVolumeGeoRepSessionCreate(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    StatusOnlyReturn glusterVolumeGeoRepSessionDelete(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName);

    StatusOnlyReturn glusterVolumeGeoRepSessionStop(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList();

    GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList(String volumeName);

    GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    GlusterVolumeGeoRepStatusDetail glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    StatusOnlyReturn glusterVolumeGeoRepSessionStart(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    StatusOnlyReturn glusterVolumeGeoRepSessionPause(String masterVolumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName,
            boolean force);

    StatusOnlyReturn glusterVolumeGeoRepConfigSet(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String configValue,
            String userName);

    StatusOnlyReturn glusterVolumeGeoRepConfigReset(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String userName);

    GlusterVolumeGeoRepConfigList glusterVolumeGeoRepConfigList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    GlusterVolumeTaskReturn glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList);

    StatusOnlyReturn setNumberOfCpus(String vmId, String numberOfCpus);

    StatusOnlyReturn hotplugMemory(Map info);

    StatusOnlyReturn hotUnplugMemory(Map<String, Object> params);

    StatusOnlyReturn updateVmPolicy(Map info);

    VMListReturn getExternalVmList(String uri, String username, String password, List<String> vmsNames);

    VMNamesListReturn getExternalVmNamesList(String uri, String username, String password);

    OneVmReturn getExternalVmFromOva(String ovaPath);

    StatusOnlyReturn glusterVolumeGeoRepSessionResume(String volumeName,
            String slaveHostName,
            String slaveVolumeName,
            String userName,
            boolean force);

    GlusterVolumeSnapshotInfoReturn glusterVolumeSnapshotList(Guid clusterId, String volumeName);

    GlusterVolumeSnapshotConfigReturn glusterSnapshotConfigList(Guid clusterId);

    StatusOnlyReturn glusterSnapshotDelete(String snapshotName);

    StatusOnlyReturn glusterVolumeSnapshotDeleteAll(String volumeName);

    StatusOnlyReturn glusterSnapshotActivate(String snapshotName, boolean force);

    StatusOnlyReturn glusterSnapshotDeactivate(String snapshotName);

    StatusOnlyReturn glusterSnapshotRestore(String snapshotName);

    GlusterVolumeSnapshotCreateReturn glusterVolumeSnapshotCreate(String volumeName,
            String snapshotName,
            String description,
            boolean force);

    StatusOnlyReturn glusterVolumeSnapshotConfigSet(String volumeName, String cfgName, String cfgValue);

    StatusOnlyReturn glusterSnapshotConfigSet(String cfgName, String cfgValue);

    OneStorageDeviceReturn glusterCreateBrick(String lvName,
            String mountPoint,
            Map<String, Object> raidParams,
            String fsType,
            String[] storageDevices);

    StorageDeviceListReturn glusterStorageDeviceList();

    StatusOnlyReturn glusterWebhookAdd(String url, String bearerToken);

    StatusOnlyReturn glusterWebhookSync();

    StatusOnlyReturn glusterWebhookDelete(String url);

    StatusOnlyReturn glusterWebhookUpdate(String url, String bearerToken);

    StatusOnlyReturn hostdevChangeNumvfs(String deviceName, int numOfVfs);

    StatusOnlyReturn convertVmFromExternalSystem(String url, String user, String password, Map<String, Object> vm, String jobUUID);

    StatusOnlyReturn convertVmFromOva(String ovaPath, Map<String, Object> vm, String jobUUID);

    OvfReturn getConvertedVm(String jobUUID);

    StatusOnlyReturn deleteV2VJob(String jobUUID);

    StatusOnlyReturn abortV2VJob(String jobUUID);

    StatusOnlyReturn glusterSnapshotScheduleOverride(boolean force);

    StatusOnlyReturn glusterSnapshotScheduleReset();

    StatusOnlyReturn registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets);

    StatusOnlyReturn unregisterSecrets(String[] libvirtSecretsUuids);

    StatusOnlyReturn freeze(String vmId);

    StatusOnlyReturn thaw(String vmId);

    VmBackupInfo startVmBackup(String vmId, Map<String, Object> backupConfig);

    StatusOnlyReturn stopVmBackup(String vmId, String backupId);

    VmBackupInfo vmBackupInfo(String vmId, String backupId, String checkpointId);

    VmCheckpointIds redefineVmCheckpoints(String vmId, Collection<Map<String, Object>> checkpoints);

    VmCheckpointIds deleteVmCheckpoints(String vmId, String[] checkpointIds);

    UUIDListReturn listVmCheckpoints(String vmId);

    StatusOnlyReturn addBitmap(String jobId, Map<String, Object> volInfo, String bitmapName);

    StatusOnlyReturn removeBitmap(String jobId, Map<String, Object> volInfo, String bitmapName);

    StatusOnlyReturn clearBitmaps(String jobId, Map<String, Object> volInfo);

    NbdServerURLReturn startNbdServer(String serverId, Map<String, Object> nbdServerConfig);

    StatusOnlyReturn stopNbdServer(String serverId);

    StatusOnlyReturn isolateVolume(String sdUUID, String srcImageID, String dstImageID, String volumeID);

    StatusOnlyReturn wipeVolume(String sdUUID, String imgUUID, String volUUID);

    StatusOnlyReturn refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID);

    VolumeInfoReturn getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID);

    MeasureReturn measureVolume(String sdUUID,
            String spUUID,
            String imgUUID,
            String volUUID,
            int dstVolFormat,
            boolean withBacking);

    QemuImageInfoReturn getQemuImageInfo(String sdUUID, String spUUID, String imgUUID, String volUUID);

    StatusOnlyReturn glusterStopProcesses();

    StatusOnlyReturn sparsifyVolume(String jobId, Map<String, Object> volumeAddress);

    StatusOnlyReturn amendVolume(String jobId, Map<String, Object> volInfo, Map<String, Object> volAttr);

    StatusOnlyReturn sealDisks(String vmId, String jobId, String storagePoolId, List<Map<String, Object>> images);

    DomainXmlListReturn dumpxmls(List<String> vmIds);

    StatusOnlyReturn hotplugLease(Guid vmId, Guid storageDomainId);

    StatusOnlyReturn hotunplugLease(Guid vmId, Guid storageDomainId);

    LldpReturn getLldp(String[] interfaces);

    StatusOnlyReturn glusterVolumeResetBrickStart(String volumeName, String existingBrickDir);

    StatusOnlyReturn glusterVolumeResetBrickCommitForce(String volumeName,
            String existingBrickDir);

    DeviceInfoReturn attachManagedBlockStorageVolume(Guid volumeId, Map<String, Object> connectionInfo);

    StatusOnlyReturn detachManagedBlockStorageVolume(Guid volumeId);

    VDSInfoReturn getLeaseStatus(String leaseUUID, String sdUUID);

    StatusOnlyReturn fenceLeaseJob(String leaseUUID, String sdUUID, Map<String, Object> leaseMetadata);

    ScreenshotInfoReturn createScreenshot(String vmId);

}
