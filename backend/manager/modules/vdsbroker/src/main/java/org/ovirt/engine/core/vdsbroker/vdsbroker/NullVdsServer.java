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

public class NullVdsServer implements IVdsServer {
    @Override public void close() {
    }

    @Override public List<Certificate> getPeerCertificates() {
        return null;
    }

    @Override public CloseableHttpClient getHttpClient() {
        return null;
    }

    @Override public OneVmReturn create(Map createInfo) {
        return null;
    }

    @Override public StatusOnlyReturn copyData(String jobId, Map src, Map dst, boolean copyBitmaps) {
        return null;
    }

    @Override public StatusOnlyReturn updateVolume(String jobId, Map<?, ?> volumeInfo, Map<?, ?> volumeAttributes) {
        return null;
    }

    @Override public StatusOnlyReturn moveDomainDevice(String jobId, Map<String, Object> moveParams) {
        return null;
    }

    @Override public StatusOnlyReturn reduceDomain(String jobId, Map<String, Object> reduceParams) {
        return null;
    }

    @Override public StatusOnlyReturn mergeSubchain(String jobId,
            Map<String, Object> subchainInfo,
            boolean mergeBitmaps) {
        return null;
    }

    @Override public StatusOnlyReturn destroy(String vmId) {
        return null;
    }

    @Override public StatusOnlyReturn shutdown(String vmId, String timeout, String message) {
        return null;
    }

    @Override public StatusOnlyReturn shutdown(String vmId, String timeout, String message, boolean reboot) {
        return null;
    }

    @Override public StatusOnlyReturn reset(String vmId) {
        return null;
    }

    @Override public StatusOnlyReturn setDestroyOnReboot(String vmId) {
        return null;
    }

    @Override public StatusOnlyReturn hibernate(String vmId, String hiberVolHandle) {
        return null;
    }

    @Override public OneVmReturn resume(String vmId) {
        return null;
    }

    @Override public VMListReturn list() {
        return null;
    }

    @Override public VMListReturn fullList(List<String> vmIds) {
        return null;
    }

    @Override public VDSInfoReturn getCapabilities() {
        return null;
    }

    @Override public void getCapabilities(BrokerCommandCallback callback) {

    }

    @Override public VDSInfoReturn getHardwareInfo() {
        return null;
    }

    @Override public void getHardwareInfo(BrokerCommandCallback callback) {

    }

    @Override public VDSInfoReturn getVdsStats() {
        return null;
    }

    @Override public void getVdsStats(BrokerCommandCallback callback) {

    }

    @Override public StatusOnlyReturn setMOMPolicyParameters(Map<String, Object> key_value_store) {
        return null;
    }

    @Override public StatusOnlyReturn setHaMaintenanceMode(String mode, boolean enabled) {
        return null;
    }

    @Override public StatusOnlyReturn add_image_ticket(ImageTicket ticket) {
        return null;
    }

    @Override public StatusOnlyReturn remove_image_ticket(String ticketId) {
        return null;
    }

    @Override public StatusOnlyReturn extend_image_ticket(String ticketId, long timeout) {
        return null;
    }

    @Override public ImageTicketInformationReturn getImageTicket(String ticketId) {
        return null;
    }

    @Override public StatusOnlyReturn desktopLogin(String vmId, String domain, String user, String password) {
        return null;
    }

    @Override public StatusOnlyReturn desktopLogoff(String vmId, String force) {
        return null;
    }

    @Override public VMInfoListReturn getVmStats(String vmId) {
        return null;
    }

    @Override public VMInfoListReturn getAllVmStats() {
        return null;
    }

    @Override public VmExternalDataReturn getVmExternalData(String vmId, VmExternalDataKind kind, boolean forceUpdate) {
        return null;
    }

    @Override public HostDevListReturn hostDevListByCaps() {
        return null;
    }

    @Override public StatusOnlyReturn migrate(Map<String, Object> migrationInfo) {
        return null;
    }

    @Override public MigrateStatusReturn migrateStatus(String vmId) {
        return null;
    }

    @Override public StatusOnlyReturn migrateCancel(String vmId) {
        return null;
    }

    @Override public PrepareImageReturn prepareImage(String spID,
            String sdID,
            String imageID,
            String volumeID,
            boolean allowIllegal) {
        return null;
    }

    @Override public StatusReturn teardownImage(String spId, String sdId, String imgGroupId, String imgId) {
        return null;
    }

    @Override public StatusReturn verifyUntrustedVolume(String spId, String sdId, String imgGroupId, String imgId) {
        return null;
    }

    @Override public OneVmReturn changeDisk(String vmId, String imageLocation) {
        return null;
    }

    @Override public OneVmReturn changeDisk(String vmId, Map<String, Object> driveSpec) {
        return null;
    }

    @Override public StatusOnlyReturn addNetwork(String bridge,
            String vlan,
            String bond,
            String[] nics,
            Map<String, String> options) {
        return null;
    }

    @Override public StatusOnlyReturn delNetwork(String bridge, String vlan, String bond, String[] nics) {
        return null;
    }

    @Override public StatusOnlyReturn editNetwork(String oldBridge,
            String newBridge,
            String vlan,
            String bond,
            String[] nics,
            Map<String, String> options) {
        return null;
    }

    @Override
    public Future<Map<String, Object>> setupNetworks(Map networks, Map bonding, Map options, boolean isPolicyReset) {
        return null;
    }

    @Override public StatusOnlyReturn setSafeNetworkConfig() {
        return null;
    }

    @Override public FenceStatusReturn fenceNode(String ip,
            String port,
            String type,
            String user,
            String password,
            String action,
            String secured,
            String options,
            Map<String, Object> fencingPolicy) {
        return null;
    }

    @Override public ServerConnectionStatusReturn connectStorageServer(int serverType,
            String spUUID,
            Map<String, String>[] args) {
        return null;
    }

    @Override public ServerConnectionStatusReturn disconnectStorageServer(int serverType,
            String spUUID,
            Map<String, String>[] args) {
        return null;
    }

    @Override public StatusOnlyReturn createStorageDomain(int domainType,
            String sdUUID,
            String domainName,
            String arg,
            int storageType,
            String storageFormatType,
            Integer blockSize,
            int maxHosts) {
        return null;
    }

    @Override public StatusOnlyReturn formatStorageDomain(String sdUUID) {
        return null;
    }

    @Override public StatusOnlyReturn connectStoragePool(String spUUID,
            int hostSpmId,
            String SCSIKey,
            String masterdomainId,
            int masterVersion,
            Map<String, String> storageDomains) {
        return null;
    }

    @Override public StatusOnlyReturn disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        return null;
    }

    @Override public StatusOnlyReturn createStoragePool(int poolType,
            String spUUID,
            String poolName,
            String msdUUID,
            String[] domList,
            int masterVersion,
            String lockPolicy,
            int lockRenewalIntervalSec,
            int leaseTimeSec,
            int ioOpTimeoutSec,
            int leaseRetries) {
        return null;
    }

    @Override public StatusOnlyReturn reconstructMaster(String spUUID,
            String poolName,
            String masterDom,
            Map<String, String> domDict,
            int masterVersion,
            String lockPolicy,
            int lockRenewalIntervalSec,
            int leaseTimeSec,
            int ioOpTimeoutSec,
            int leaseRetries,
            int hostSpmId) {
        return null;
    }

    @Override public OneStorageDomainStatsReturn getStorageDomainStats(String sdUUID) {
        return null;
    }

    @Override public OneStorageDomainInfoReturn getStorageDomainInfo(String sdUUID) {
        return null;
    }

    @Override
    public StorageDomainListReturn getStorageDomainsList(String spUUID, int domainType, String poolType, String path) {
        return null;
    }

    @Override public OneUuidReturn createVG(String sdUUID, String[] deviceList, boolean force) {
        return null;
    }

    @Override public OneVGReturn getVGInfo(String vgUUID) {
        return null;
    }

    @Override public LUNListReturn getDeviceList(int storageType, String[] devicesList, boolean checkStatus) {
        return null;
    }

    @Override public DevicesVisibilityMapReturn getDevicesVisibility(String[] devicesList) {
        return null;
    }

    @Override public IQNListReturn discoverSendTargets(Map<String, String> args) {
        return null;
    }

    @Override public OneUuidReturn spmStart(String spUUID,
            int prevID,
            String prevLVER,
            int recoveryMode,
            String SCSIFencing,
            int maxHostId,
            String storagePoolFormatType) {
        return null;
    }

    @Override public StatusOnlyReturn spmStop(String spUUID) {
        return null;
    }

    @Override public SpmStatusReturn spmStatus(String spUUID) {
        return null;
    }

    @Override public HostJobsReturn getHostJobs(String jobType, List<String> jobIds) {
        return null;
    }

    @Override public TaskStatusReturn getTaskStatus(String taskUUID) {
        return null;
    }

    @Override public TaskStatusListReturn getAllTasksStatuses() {
        return null;
    }

    @Override public TaskInfoListReturn getAllTasksInfo() {
        return null;
    }

    @Override public StatusOnlyReturn stopTask(String taskUUID) {
        return null;
    }

    @Override public StatusOnlyReturn clearTask(String taskUUID) {
        return null;
    }

    @Override public StatusOnlyReturn revertTask(String taskUUID) {
        return null;
    }

    @Override public StatusOnlyReturn cleanStorageDomainMetaData(String sdUUID, String spUUID) {
        return null;
    }

    @Override public StatusOnlyReturn hotplugDisk(Map info) {
        return null;
    }

    @Override public StatusOnlyReturn hotunplugDisk(Map info) {
        return null;
    }

    @Override public VmInfoReturn hotPlugNic(Map info) {
        return null;
    }

    @Override public StatusOnlyReturn hotUnplugNic(Map info) {
        return null;
    }

    @Override public StatusOnlyReturn vmUpdateDevice(String vmId, Map device) {
        return null;
    }

    @Override public FutureTask<Map<String, Object>> poll() {
        return null;
    }

    @Override public FutureTask<Map<String, Object>> timeBoundPoll(long timeout, TimeUnit unit) {
        return null;
    }

    @Override public FutureTask<Map<String, Object>> timeBoundPoll2(long timeout, TimeUnit unit) {
        return null;
    }

    @Override public FutureTask<Map<String, Object>> timeBoundPollConfirmConnectivity(long timeout, TimeUnit unit) {
        return null;
    }

    @Override public StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String jobUUID, int timeout) {
        return null;
    }

    @Override public StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory, String jobUUID, int timeout) {
        return null;
    }

    @Override
    public StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen, String jobUUID, int timeout) {
        return null;
    }

    @Override public ImageSizeReturn diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize) {
        return null;
    }

    @Override public StatusOnlyReturn merge(String vmId,
            Map<String, String> drive,
            String baseVolUUID,
            String topVolUUID,
            String bandwidth,
            String jobUUID) {
        return null;
    }

    @Override public OneUuidReturn glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force,
            boolean arbiter) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeSet(String volumeName, String key, String value) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeStart(String volumeName, Boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeStop(String volumeName, Boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeDelete(String volumeName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeReset(String volumeName, String volumeOption, Boolean force) {
        return null;
    }

    @Override public GlusterVolumeOptionsInfoReturn glusterVolumeSetOptionsList() {
        return null;
    }

    @Override public GlusterTaskInfoReturn glusterVolumeRemoveBricksStart(String volumeName,
            String[] brickList,
            int replicaCount,
            Boolean forceRemove) {
        return null;
    }

    @Override public GlusterVolumeTaskReturn glusterVolumeRemoveBricksStop(String volumeName,
            String[] brickList,
            int replicaCount) {
        return null;
    }

    @Override
    public StatusOnlyReturn glusterVolumeRemoveBricksCommit(String volumeName, String[] brickList, int replicaCount) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount,
            boolean force) {
        return null;
    }

    @Override
    public GlusterTaskInfoReturn glusterVolumeRebalanceStart(String volumeName, Boolean fixLayoutOnly, Boolean force) {
        return null;
    }

    @Override public GlusterVolumeTaskReturn glusterVolumeRebalanceStop(String volumeName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeReplaceBrickCommitForce(String volumeName,
            String existingBrickDir,
            String newBrickDir) {
        return null;
    }

    @Override public StatusOnlyReturn glusterHostRemove(String hostName, Boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterHostAdd(String hostName) {
        return null;
    }

    @Override public GlusterServersListReturn glusterServersList() {
        return null;
    }

    @Override public StatusOnlyReturn diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk) {
        return null;
    }

    @Override public StatusOnlyReturn diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeProfileStart(String volumeName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeProfileStop(String volumeName) {
        return null;
    }

    @Override public GlusterVolumeStatusReturn glusterVolumeStatus(Guid clusterId,
            String volumeName,
            String brickName,
            String volumeStatusOption) {
        return null;
    }

    @Override public GlusterLocalLogicalVolumeListReturn glusterLogicalVolumeList() {
        return null;
    }

    @Override public GlusterLocalPhysicalVolumeListReturn glusterPhysicalVolumeList() {
        return null;
    }

    @Override public GlusterVDOVolumeListReturn glusterVDOVolumeList() {
        return null;
    }

    @Override public GlusterVolumesListReturn glusterVolumesList(Guid clusterId) {
        return null;
    }

    @Override public GlusterVolumesListReturn glusterVolumeInfo(Guid clusterId, String volumeName) {
        return null;
    }

    @Override public GlusterVolumesHealInfoReturn glusterVolumeHealInfo(String volumeName) {
        return null;
    }

    @Override
    public GlusterVolumeProfileInfoReturn glusterVolumeProfileInfo(Guid clusterId, String volumeName, boolean nfs) {
        return null;
    }

    @Override public StatusOnlyReturn glusterHookEnable(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterHookDisable(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override public GlusterHooksListReturn glusterHooksList() {
        return null;
    }

    @Override public OneUuidReturn glusterHostUUIDGet() {
        return null;
    }

    @Override public GlusterServicesReturn glusterServicesList(Guid serverId, String[] serviceNames) {
        return null;
    }

    @Override
    public GlusterHookContentInfoReturn glusterHookRead(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterHookUpdate(String glusterCommand,
            String stage,
            String hookName,
            String content,
            String checksum) {
        return null;
    }

    @Override public StatusOnlyReturn glusterHookAdd(String glusterCommand,
            String stage,
            String hookName,
            String content,
            String checksum,
            Boolean enabled) {
        return null;
    }

    @Override public StatusOnlyReturn glusterHookRemove(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override
    public GlusterServicesReturn glusterServicesAction(Guid serverId, String[] serviceList, String actionType) {
        return null;
    }

    @Override public StoragePoolInfo getStoragePoolInfo(String spUUID) {
        return null;
    }

    @Override public GlusterTasksListReturn glusterTasksList() {
        return null;
    }

    @Override public GlusterVolumeTaskReturn glusterVolumeRebalanceStatus(String volumeName) {
        return null;
    }

    @Override public BooleanReturn glusterVolumeEmptyCheck(String volumeName) {
        return null;
    }

    @Override public GlusterHostsPubKeyReturn glusterGeoRepKeysGet() {
        return null;
    }

    @Override public StatusOnlyReturn glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String userName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterGeoRepMountBrokerSetup(String remoteVolumeName,
            String userName,
            String remoteGroupName,
            Boolean partial) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepSessionCreate(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepSessionDelete(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepSessionStop(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        return null;
    }

    @Override public GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList() {
        return null;
    }

    @Override public GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList(String volumeName) {
        return null;
    }

    @Override public GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        return null;
    }

    @Override public GlusterVolumeGeoRepStatusDetail glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepSessionStart(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepSessionPause(String masterVolumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName,
            boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepConfigSet(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String configValue,
            String userName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepConfigReset(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String userName) {
        return null;
    }

    @Override public GlusterVolumeGeoRepConfigList glusterVolumeGeoRepConfigList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        return null;
    }

    @Override public GlusterVolumeTaskReturn glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList) {
        return null;
    }

    @Override public StatusOnlyReturn setNumberOfCpus(String vmId, String numberOfCpus) {
        return null;
    }

    @Override public StatusOnlyReturn hotplugMemory(Map info) {
        return null;
    }

    @Override public StatusOnlyReturn hotUnplugMemory(Map<String, Object> params) {
        return null;
    }

    @Override public StatusOnlyReturn updateVmPolicy(Map info) {
        return null;
    }

    @Override
    public VMListReturn getExternalVmList(String uri, String username, String password, List<String> vmsNames) {
        return null;
    }

    @Override public VMNamesListReturn getExternalVmNamesList(String uri, String username, String password) {
        return null;
    }

    @Override public OneVmReturn getExternalVmFromOva(String ovaPath) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeGeoRepSessionResume(String volumeName,
            String slaveHostName,
            String slaveVolumeName,
            String userName,
            boolean force) {
        return null;
    }

    @Override public GlusterVolumeSnapshotInfoReturn glusterVolumeSnapshotList(Guid clusterId, String volumeName) {
        return null;
    }

    @Override public GlusterVolumeSnapshotConfigReturn glusterSnapshotConfigList(Guid clusterId) {
        return null;
    }

    @Override public StatusOnlyReturn glusterSnapshotDelete(String snapshotName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeSnapshotDeleteAll(String volumeName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterSnapshotActivate(String snapshotName, boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterSnapshotDeactivate(String snapshotName) {
        return null;
    }

    @Override public StatusOnlyReturn glusterSnapshotRestore(String snapshotName) {
        return null;
    }

    @Override public GlusterVolumeSnapshotCreateReturn glusterVolumeSnapshotCreate(String volumeName,
            String snapshotName,
            String description,
            boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturn glusterVolumeSnapshotConfigSet(String volumeName, String cfgName, String cfgValue) {
        return null;
    }

    @Override public StatusOnlyReturn glusterSnapshotConfigSet(String cfgName, String cfgValue) {
        return null;
    }

    @Override public OneStorageDeviceReturn glusterCreateBrick(String lvName,
            String mountPoint,
            Map<String, Object> raidParams,
            String fsType,
            String[] storageDevices) {
        return null;
    }

    @Override public StorageDeviceListReturn glusterStorageDeviceList() {
        return null;
    }

    @Override public StatusOnlyReturn glusterWebhookAdd(String url, String bearerToken) {
        return null;
    }

    @Override public StatusOnlyReturn glusterWebhookSync() {
        return null;
    }

    @Override public StatusOnlyReturn glusterWebhookDelete(String url) {
        return null;
    }

    @Override public StatusOnlyReturn glusterWebhookUpdate(String url, String bearerToken) {
        return null;
    }

    @Override public StatusOnlyReturn hostdevChangeNumvfs(String deviceName, int numOfVfs) {
        return null;
    }

    @Override public StatusOnlyReturn convertVmFromExternalSystem(String url,
            String user,
            String password,
            Map<String, Object> vm,
            String jobUUID) {
        return null;
    }

    @Override public StatusOnlyReturn convertVmFromOva(String ovaPath, Map<String, Object> vm, String jobUUID) {
        return null;
    }

    @Override public OvfReturn getConvertedVm(String jobUUID) {
        return null;
    }

    @Override public StatusOnlyReturn deleteV2VJob(String jobUUID) {
        return null;
    }

    @Override public StatusOnlyReturn abortV2VJob(String jobUUID) {
        return null;
    }

    @Override public StatusOnlyReturn glusterSnapshotScheduleOverride(boolean force) {
        return null;
    }

    @Override public StatusOnlyReturn glusterSnapshotScheduleReset() {
        return null;
    }

    @Override
    public StatusOnlyReturn registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets) {
        return null;
    }

    @Override public StatusOnlyReturn unregisterSecrets(String[] libvirtSecretsUuids) {
        return null;
    }

    @Override public StatusOnlyReturn freeze(String vmId) {
        return null;
    }

    @Override public StatusOnlyReturn thaw(String vmId) {
        return null;
    }

    @Override public VmBackupInfo startVmBackup(String vmId, Map<String, Object> backupConfig) {
        return null;
    }

    @Override public StatusOnlyReturn stopVmBackup(String vmId, String backupId) {
        return null;
    }

    @Override public VmBackupInfo vmBackupInfo(String vmId, String backupId, String checkpointId) {
        return null;
    }

    @Override public VmCheckpointIds redefineVmCheckpoints(String vmId, Collection<Map<String, Object>> checkpoints) {
        return null;
    }

    @Override public VmCheckpointIds deleteVmCheckpoints(String vmId, String[] checkpointIds) {
        return null;
    }

    @Override public UUIDListReturn listVmCheckpoints(String vmId) {
        return null;
    }

    @Override
    public StatusOnlyReturn addBitmap(String jobId, Map<String, Object> volInfo, String bitmapName) {
        return null;
    }

    @Override
    public StatusOnlyReturn removeBitmap(String jobId, Map<String, Object> volInfo, String bitmapName) {
        return null;
    }

    @Override
    public StatusOnlyReturn clearBitmaps(String jobId, Map<String, Object> volInfo) {
        return null;
    }

    @Override public NbdServerURLReturn startNbdServer(String serverId, Map<String, Object> nbdServerConfig) {
        return null;
    }

    @Override public StatusOnlyReturn stopNbdServer(String serverId) {
        return null;
    }

    @Override
    public StatusOnlyReturn isolateVolume(String sdUUID, String srcImageID, String dstImageID, String volumeID) {
        return null;
    }

    @Override public StatusOnlyReturn wipeVolume(String sdUUID, String imgUUID, String volUUID) {
        return null;
    }

    @Override public StatusOnlyReturn refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        return null;
    }

    @Override public VolumeInfoReturn getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        return null;
    }

    @Override
    public MeasureReturn measureVolume(String sdUUID,
            String spUUID,
            String imgUUID,
            String volUUID,
            int dstVolFormat,
            boolean withBacking) {
        return null;
    }

    @Override
    public QemuImageInfoReturn getQemuImageInfo(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        return null;
    }

    @Override public StatusOnlyReturn glusterStopProcesses() {
        return null;
    }

    @Override public StatusOnlyReturn sparsifyVolume(String jobId, Map<String, Object> volumeAddress) {
        return null;
    }

    @Override
    public StatusOnlyReturn amendVolume(String jobId, Map<String, Object> volInfo, Map<String, Object> volAttr) {
        return null;
    }

    @Override public StatusOnlyReturn sealDisks(String vmId,
            String jobId,
            String storagePoolId,
            List<Map<String, Object>> images) {
        return null;
    }

    @Override public DomainXmlListReturn dumpxmls(List<String> vmIds) {
        return null;
    }

    @Override public StatusOnlyReturn hotplugLease(Guid vmId, Guid storageDomainId) {
        return null;
    }

    @Override public StatusOnlyReturn hotunplugLease(Guid vmId, Guid storageDomainId) {
        return null;
    }

    @Override public LldpReturn getLldp(String[] interfaces) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeResetBrickStart(String volumeName, String existingBrickDir) {
        return null;
    }

    @Override public StatusOnlyReturn glusterVolumeResetBrickCommitForce(String volumeName, String existingBrickDir) {
        return null;
    }

    @Override
    public DeviceInfoReturn attachManagedBlockStorageVolume(Guid volumeId, Map<String, Object> connectionInfo) {
        return null;
    }

    @Override public StatusOnlyReturn detachManagedBlockStorageVolume(Guid volumeId) {
        return null;
    }

    @Override public VDSInfoReturn getLeaseStatus(String leaseUUID, String sdUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturn fenceLeaseJob(String leaseUUID, String sdUUID, Map<String, Object> leaseMetadata) {
        return null;
    }

    @Override
    public GlusterVolumeGlobalOptionsInfoReturn glusterVolumeGlobalOptionsGet() {
        return null;
    }

    @Override
    public ScreenshotInfoReturn createScreenshot(String vmId) {
        return null;
    }
}
