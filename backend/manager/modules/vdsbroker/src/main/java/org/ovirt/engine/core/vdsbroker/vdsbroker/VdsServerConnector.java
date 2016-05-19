package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

public interface VdsServerConnector {
    public Map<String, Object> create(Map createInfo);

    public Map<String, Object> createVolumeContainer(String jobId, Map<String, Object> volumeInfo);

    public Map<String, Object> copyData(Map srcImage, Map dstImage, boolean collapse);

    public Map<String, Object> allocateVolume(String spUUID, String sdUUID, String imgGUID, String volUUID, String size);

    public Map<String, Object> destroy(String vmId);

    public Map<String, Object> shutdown(String vmId, String timeout, String message);

    public Map<String, Object> shutdown(String vmId, String timeout, String message, boolean reboot);

    public Map<String, Object> pause(String vmId);

    public Map<String, Object> hibernate(String vmId, String hiberVolHandle);

    public Map<String, Object> cont(String vmId);

    public Map<String, Object> list();

    public Map<String, Object> list(String isFull, String[] vmIds);

    public Map<String, Object> getVdsCapabilities();

    public Map<String, Object> getVdsHardwareInfo();

    public Map<String, Object> getVdsStats();

    public Map<String, Object> desktopLogin(String vmId, String domain, String user, String password);

    public Map<String, Object> desktopLogoff(String vmId, String force);

    public Map<String, Object> getVmStats(String vmId);

    public Map<String, Object> setMOMPolicyParameters(Map<String, Object> key_value);

    public Map<String, Object> setHaMaintenanceMode(String mode, boolean enabled);

    public Map<String, Object> add_image_ticket(String ticketId, String[] ops, long timeout,
            long size, String url);

    public Map<String, Object> remove_image_ticket(String ticketId);

    public Map<String, Object> extend_image_ticket(String ticketId, long timeout);

    public Map<String, Object> get_image_transfer_session_stats(String ticketId);

    public Map<String, Object> getAllVmStats();

    public Map<String, Object> hostdevListByCaps();

    public Map<String, Object> migrate(Map<String, Object> migrationInfo);

    public Map<String, Object> migrateStatus(String vmId);

    public Map<String, Object> migrateCancel(String vmId);

    public Map<String, Object> changeCD(String vmId, String imageLocation);

    public Map<String, Object> changeCD(String vmId, Map<String, Object> driveSpec);

    public Map<String, Object> changeFloppy(String vmId, String imageLocation);

    public Map<String, Object> monitorCommand(String vmId, String monitorCommand);

    public Map<String, Object> addNetwork(String bridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    public Map<String, Object> delNetwork(String bridge, String vlan, String bond, String[] nics);

    public Map<String, Object> editNetwork(String oldBridge, String newBridge, String vlan, String bond, String[] nics,
            Map<String, String> options);

    public Map<String, Object> setupNetworks(Map networks, Map bonding, Map options);

    @FutureCall(delegeteTo="setupNetworks")
    public FutureTask<Map<String, Object>> futureSetupNetworks(Map networks, Map bonding, Map options);

    public Map<String, Object> setSafeNetworkConfig();

    public Map<String, Object> fenceNode(String ip, String port, String type, String user, String password,
            String action, String secured, String options);

    public Map<String, Object> fenceNode(String ip, String port, String type, String user, String password,
            String action, String secured, String options, Map<String, Object> fencingPolicy);

    public Map<String, Object> connectStorageServer(int serverType, String spUUID, Map<String, String>[] args);

    public Map<String, Object> disconnectStorageServer(int serverType, String spUUID, Map<String, String>[] args);

    public Map<String, Object> createStorageDomain(int domainType, String sdUUID, String domainName, String arg,
            int storageType, String storageFormatType);

    public Map<String, Object> formatStorageDomain(String sdUUID);

    public Map<String, Object> connectStoragePool(String spUUID, int hostSpmId, String SCSIKey, String masterdomainId,
            int masterVersion);

    public Map<String, Object> connectStoragePool(String spUUID, int hostSpmId, String SCSIKey, String masterdomainId,
                                                  int masterVersion, Map<String, String> storageDomains);

    public Map<String, Object> disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey);

    public Map<String, Object> createStoragePool(int poolType, String spUUID, String poolName, String msdUUID,
            String[] domList, int masterVersion, String lockPolicy, int lockRenewalIntervalSec, int leaseTimeSec,
            int ioOpTimeoutSec, int leaseRetries);

    public Map<String, Object> reconstructMaster(String spUUID, String poolName, String masterDom,
            Map<String, String> domDict, int masterVersion, String lockPolicy, int lockRenewalIntervalSec,
            int leaseTimeSec, int ioOpTimeoutSec, int leaseRetries, int hostSpmId);

    public Map<String, Object> getStorageDomainStats(String sdUUID);

    public Map<String, Object> getStorageDomainInfo(String sdUUID);

    public Map<String, Object> getStorageDomainsList(String spUUID, int domainType, String poolType, String path);

    public Map<String, Object> getIsoList(String spUUID);

    public Map<String, Object> createVG(String sdUUID, String[] deviceList, boolean force);

    public Map<String, Object> getVGInfo(String vgUUID);

    public Map<String, Object> getDeviceList(int storageType, String[] devicesList, boolean checkStatus);

    public Map<String, Object> getDevicesVisibility(String[] devicesList);

    public Map<String, Object> discoverSendTargets(Map<String, String> args);

    public Map<String, Object> spmStart(String spUUID,
            int prevID,
            String prevLVER,
            int recoveryMode,
            String SCSIFencing,
            int maxHostId,
            String storagePoolFormatType);

    public Map<String, Object> spmStop(String spUUID);

    public Map<String, Object> getSpmStatus(String spUUID);

    public Map<String, Object> getHostJobs(String job_type, List<String> jobIds);

    public Map<String, Object> getTaskStatus(String taskUUID);

    public Map<String, Object> getAllTasksStatuses();

    public Map<String, Object> getAllTasksInfo();

    public Map<String, Object> stopTask(String taskUUID);

    public Map<String, Object> clearTask(String taskUUID);

    public Map<String, Object> revertTask(String taskUUID);

    Map<String, Object> hotplugDisk(Map<String, Object> info);

    Map<String, Object> hotunplugDisk(Map<String, Object> info);

    public Map<String, Object> hotplugNic(Map<String, Object> innerMap);

    public Map<String, Object> hotunplugNic(Map<String, Object> innerMap);

    public Map<String, Object> vmUpdateDevice(String vmId, Map<String, Object> device);

    Map<String, Object> snapshot(String vmId, Map<String, String>[] disks);

    Map<String, Object> snapshot(String vmId, Map<String, String>[] disks, String memory);

    Map<String, Object> snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen);

    public Map<String, Object> getDiskAlignment(String vmId, Map<String, String> driveSpecs);

    public Map<String, Object> diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize);

    public Map<String, Object> merge(String vmId, Map<String, String> drive,
            String baseVolUUID, String topVolUUID, String bandwidth, String jobUUID);

    // Gluster vdsm commands
    public Map<String, Object> glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force);

    public Map<String, Object> glusterVolumeSet(String volumeName, String key, String value);

    public Map<String, Object> glusterVolumeStart(String volumeName, Boolean force);

    public Map<String, Object> glusterVolumeStop(String volumeName, Boolean force);

    public Map<String, Object> glusterVolumeDelete(String volumeName);

    public Map<String, Object> glusterVolumeReset(String volumeName, String volumeOption, Boolean force);

    public Map<String, Object> glusterVolumeSetOptionsList();

    public Map<String, Object> glusterVolumeRemoveBrickForce(String volumeName,
            String[] brickDirectories,
            int replicaCount);

    public Map<String, Object> glusterVolumeRemoveBrickStart(String volumeName,
            String[] brickDirectories,
            int replicaCount);

    public Map<String, Object> glusterVolumeRemoveBrickStop(String volumeName,
            String[] brickDirectories,
            int replicaCount);

    public Map<String, Object> glusterVolumeRemoveBrickCommit(String volumeName,
            String[] brickDirectories,
            int replicaCount);

    public Map<String, Object> glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount,
            boolean force);

    public Map<String, Object> glusterVolumeRebalanceStart(String volumeName, Boolean fixLayoutOnly, Boolean force);

    public Map<String, Object> glusterVolumeRebalanceStop(String volumeName);

    public Map<String, Object> replaceGlusterVolumeBrickStart(String volumeName, String existingBrickDir, String newBrickDir);

    public Map<String, Object> glusterHostRemove(String hostName, Boolean force);

    public Map<String, Object> glusterVolumeReplaceBrickCommitForce(String volumeName, String existingBrickDir, String newBrickDir);

    public Map<String, Object> glusterHostAdd(String hostName);

    public Map<String, Object> glusterHostsList();

    public Map<String, Object> glusterVolumesList();

    public Map<String, Object> glusterVolumesList(String volumeName);

    public Map<String, Object> ping();

    @FutureCall(delegeteTo = "ping")
    public FutureTask<Map<String, Object>> futurePing();

    public Map<String, Object> diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk);

    public Map<String, Object> diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk);

    public Map<String, Object> glusterVolumeProfileStart(String volumeName);

    public Map<String, Object> glusterVolumeProfileStop(String volumeName);

    public Map<String, Object> glusterVolumeStatus(String volumeName, String brickName, String volumeStatusOption);

    public Map<String, Object> glusterVolumeProfileInfo(String volumeName, boolean nfs);

    public Map<String, Object> glusterHookEnable(String glusterCommand, String stage, String hookName);

    public Map<String, Object> glusterHookDisable(String glusterCommand, String level, String hookName);

    public Map<String, Object> glusterHooksList();

    public Map<String, Object> glusterHostUUIDGet();

    public Map<String, Object> glusterServicesGet(String[] serviceNames);

    public Map<String, Object> glusterHookRead(String glusterCommand, String stage, String hookName);

    public Map<String, Object> glusterHookUpdate(String glusterCommand, String stage, String hookName, String content, String checksum);

    public Map<String, Object> glusterHookAdd(String glusterCommand, String stage, String hookName, String content, String checksum, Boolean enabled);

    public Map<String, Object> glusterHookRemove(String glusterCommand, String stage, String hookName);

    public Map<String, Object> glusterServicesAction(String[] serviceNames, String action);

    public Map<String, Object> getStoragePoolInfo(String spUUID);

    public Map<String, Object> glusterTasksList();

    public Map<String, Object> glusterVolumeRebalanceStatus(String volumeName);

    public Map<String, Object> glusterVolumeEmptyCheck(String volumeName);

    public Map<String, Object> glusterGeoRepKeysGet();

    public Map<String, Object> glusterGeoRepKeysUpdate(String userName, List<String> geoRepPubKeys);

    public Map<String, Object> glusterGeoRepMountBrokerSetup(String userName,
            String remoteGroupName,
            String remoteVolumeName,
            Boolean partial);

    public Map<String, Object> glusterVolumeGeoRepSessionCreate(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    public Map<String, Object> glusterVolumeGeoRepSessionResume(String volumeName,
            String slaveHostName,
            String slaveVolumeName,
            String userName,
            boolean force);

    public Map<String, Object> glusterVolumeGeoRepSessionStart(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    public Map<String, Object> glusterVolumeGeoRepSessionPause(String masterVolumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName,
            boolean force);

    public Map<String, Object> glusterVolumeGeoRepSessionList();

    public Map<String, Object> glusterVolumeGeoRepSessionList(String volumeName);

    public Map<String, Object> glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    public Map<String, Object> glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    public Map<String, Object> glusterVolumeGeoRepConfigReset(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String userName);

    public Map<String, Object> glusterVolumeGeoRepConfigList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName);

    public Map<String, Object> glusterVolumeGeoRepSessionDelete(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName);

    public Map<String, Object> glusterVolumeGeoRepSessionStop(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force);

    public Map<String, Object> glusterVolumeGeoRepConfigSet(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String configValue,
            String userName);

    public Map<String, Object> glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList);

    public Map<String, Object> setNumberOfCpus(String vmId, String numberOfCpus);

    public Map<String, Object> hotplugMemory(Map<String, Object> info);

    public Map<String, Object> updateVmPolicy(Map info);

    public Map<String, Object> getExternalVMs(String uri, String username, String password);

    public Map<String, Object> glusterVolumeSnapshotList(String volumeName);

    public Map<String, Object> glusterSnapshotConfigList();

    public Map<String, Object> glusterSnapshotDelete(String snapshotName);

    public Map<String, Object> glusterVolumeSnapshotDeleteAll(String volumeName);

    public Map<String, Object> glusterSnapshotActivate(String snapshotName, boolean force);

    public Map<String, Object> glusterSnapshotDeactivate(String snapshotName);

    public Map<String, Object> glusterSnapshotRestore(String snapshotName);

    public Map<String, Object> glusterVolumeSnapshotCreate(String volumeName,
            String snapshotName,
            String description,
            boolean force);

    public Map<String, Object> glusterVolumeSnapshotConfigSet(String volumeName, String cfgName, String cfgValue);

    public Map<String, Object> glusterSnapshotConfigSet(String cfgName, String cfgValue);

    public Map<String, Object> glusterStorageDevicesList();

    public Map<String, Object> glusterCreateBrick(String brickName,
            String mountPoint,
            String[] devices,
            String fsType,
            Map<String, Object> raidParams);

    public Map<String, Object> hostdevChangeNumvfs(String deviceName, int numOfVfs);

    public Map<String, Object> convertExternalVm(String url, String user, String password, Map vm, String jobId);

    public Map<String, Object> convertExternalVmFromOva(String ovaPath, Map vm, String jobId);

    public Map<String, Object> getConvertedVm(String jobUUID);

    public Map<String, Object> deleteV2VJob(String jobUUID);

    public Map<String, Object> abortV2VJob(String jobUUID);

    public Map<String, Object> glusterSnapshotScheduleOverride(boolean force);

    public Map<String, Object> glusterSnapshotScheduleReset();

    public Map<String, Object> registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets);

    public Map<String, Object> unregisterSecrets(String[] libvirtSecretsUuids);

    public Map<String, Object> freeze(String vmId);

    public Map<String, Object> thaw(String vmId);

    public Map<String, Object> isolateVolume(String sdUUID, String srcImgUUID, String dstImgUUID, String volumeID);

    public Map<String, Object> wipeVolume(String sdUUID, String imgUUID, String volUUID);

    public Map<String, Object> getExternalVmFromOva(String ovaPath);

    public Map<String, Object> refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID);

    public Map<String, Object> getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID);

    public Map<String, Object> glusterProcessesStop();

    public Map<String, Object> prepareImage(String sdID, String spID, String imageID,
            String volumeID, boolean allowIllegal);

    public Map<String, Object> teardownImage(String sdID, String spID, String imageID, String volumeID);

    public Map<String, Object> verifyUntrustedVolume(String sdID, String spID, String imageID, String volumeID);

    public Map<String, Object> glusterVolumeHealInfo(String volumeName);
}
