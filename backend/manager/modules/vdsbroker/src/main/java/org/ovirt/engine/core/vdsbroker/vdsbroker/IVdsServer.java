package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHookContentInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHooksListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHostsPubKeyReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServersListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServicesReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTaskInfoReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTasksListReturn;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepConfigList;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatus;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatusDetail;
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

@SuppressWarnings("rawtypes")
public interface IVdsServer {
    void close();

    List<Certificate> getPeerCertificates();

    HttpClient getHttpClient();

    OneVmReturn create(Map createInfo);

    StatusOnlyReturn createVolumeContainer(String jobId, Map<String, Object> createVolumeInfo);

    StatusOnlyReturn copyData(String jobId, Map src, Map dst);

    StatusOnlyReturn updateVolume(String jobId, Map<?, ?> volumeInfo, Map<?, ?> volumeAttributes);

    StatusOnlyReturn allocateVolume(String spUUID, String sdUUID, String imgGUID, String volUUID, String size);

    StatusOnlyReturn moveDomainDevice(String jobId, Map<String, Object> moveParams);

    StatusOnlyReturn reduceDomain(String jobId, Map<String, Object> reduceParams);

    StatusOnlyReturn mergeSubchain(String jobId, Map<String, Object> subchainInfo);

    StatusOnlyReturn destroy(String vmId);

    StatusOnlyReturn shutdown(String vmId, String timeout, String message);

    StatusOnlyReturn shutdown(String vmId, String timeout, String message, boolean reboot);

    StatusOnlyReturn setDestroyOnReboot(String vmId);

    OneVmReturn pause(String vmId);

    StatusOnlyReturn hibernate(String vmId, String hiberVolHandle);

    OneVmReturn resume(String vmId);

    VMListReturn list();

    VMListReturn fullList(List<String> vmIds);

    VDSInfoReturn getCapabilities();

    VDSInfoReturn getHardwareInfo();

    VDSInfoReturn getVdsStats();

    StatusOnlyReturn setMOMPolicyParameters(Map<String, Object> key_value_store);

    StatusOnlyReturn setHaMaintenanceMode(String mode, boolean enabled);

    StatusOnlyReturn add_image_ticket(String ticketId, String[] ops, long timeout,
                                               long size, String url, String filename);

    StatusOnlyReturn remove_image_ticket(String ticketId);

    StatusOnlyReturn extend_image_ticket(String ticketId, long timeout);

    ImageTicketInformationReturn getImageTicket(String ticketId);

    StatusOnlyReturn desktopLogin(String vmId, String domain, String user, String password);

    StatusOnlyReturn desktopLogoff(String vmId, String force);

    VMInfoListReturn getVmStats(String vmId);

    VMInfoListReturn getAllVmStats();

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
            int storageType, String storageFormatType);

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

    FutureTask<Map<String, Object>> poll();

    FutureTask<Map<String, Object>> timeBoundPoll(long timeout, TimeUnit unit);

    StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks);

    StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory);

    StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen);

    AlignmentScanReturn getDiskAlignment(String vmId, Map<String, String> driveSpecs);

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

    StatusOnlyReturn isolateVolume(String sdUUID, String srcImageID, String dstImageID, String volumeID);

    StatusOnlyReturn wipeVolume(String sdUUID, String imgUUID, String volUUID);

    StatusOnlyReturn refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID);

    VolumeInfoReturn getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID);

    QemuImageInfoReturn getQemuImageInfo(String sdUUID, String spUUID, String imgUUID, String volUUID);

    StatusOnlyReturn glusterStopProcesses();

    StatusOnlyReturn sparsifyVolume(String jobId, Map<String, Object> volumeAddress);

    StatusOnlyReturn amendVolume(String jobId, Map<String, Object> volInfo, Map<String, Object> volAttr);

    StatusOnlyReturn sealDisks(String templateId, String jobId, String storagePoolId, List<Map<String, Object>> images);

    DomainXmlListReturn dumpxmls(List<String> vmIds);

    StatusOnlyReturn hotplugLease(Guid vmId, Guid storageDomainId);

    StatusOnlyReturn hotunplugLease(Guid vmId, Guid storageDomainId);

    LldpReturn getLldp(String[] interfaces);
}
