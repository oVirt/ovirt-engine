package org.ovirt.engine.core.vdsbroker.vdsbroker.kubevirt;

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
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.OneStorageDeviceReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.StorageDeviceListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.AlignmentScanReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BooleanReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DevicesVisibilityMapReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FenceStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostDevListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IQNListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ImageSizeReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.LUNListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MigrateStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneStorageDomainInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneStorageDomainStatsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneVGReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneVmReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OvfReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ServerConnectionStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SpmStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StorageDomainListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskInfoListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskStatusListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VGListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMInfoListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VolumeInfoReturnForXmlRpc;

public class KubevirtVdsServer implements IVdsServer {

    private final Guid vdsId;

    public KubevirtVdsServer(Guid vdsId) {
        this.vdsId = vdsId;
    }

    @Override
    public void close() {

    }

    @Override
    public List<Certificate> getPeerCertificates() {
        return null;
    }

    @Override
    public HttpClient getHttpClient() {
        return null;
    }

    @Override
    public OneVmReturnForXmlRpc create(Map createInfo) {
        // controller pod
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc createVolumeContainer(String sdUUID,
            String imgGUID,
            String size,
            int volFormat,
            int diskType,
            String volUUID,
            String descr,
            String srcImgGUID,
            String srcVolUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc copyData(Map src, Map dst, boolean collapse) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc allocateVolume(String spUUID,
            String sdUUID,
            String imgGUID,
            String volUUID,
            String size) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc destroy(String vmId) {
        // controller pod
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message) {
        // controller pod
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message, boolean reboot) {
        // compute pod
        return null;
    }

    @Override
    public OneVmReturnForXmlRpc pause(String vmId) {
        // compute pod
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc hibernate(String vmId, String hiberVolHandle) {
        return null;
    }

    @Override
    public OneVmReturnForXmlRpc resume(String vmId) {
        return null;
    }

    @Override
    public VMListReturnForXmlRpc list() {
        //vAdvisor spec endpoint
        return null;
    }

    @Override
    public VMListReturnForXmlRpc list(String isFull, String[] vmIds) {
        //vAdvisor spec endpoint
        return null;
    }

    @Override
    public VDSInfoReturnForXmlRpc getCapabilities() {
        //hardcoded for now
        return null;
    }

    @Override
    public VDSInfoReturnForXmlRpc getHardwareInfo() {
        // cAdvisor
        return null;
    }

    @Override
    public VDSInfoReturnForXmlRpc getVdsStats() {
        // cAdvisor
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc setMOMPolicyParameters(Map<String, Object> key_value_store) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc setHaMaintenanceMode(String mode, boolean enabled) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc desktopLogin(String vmId, String domain, String user, String password) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc desktopLogoff(String vmId, String force) {
        return null;
    }

    @Override
    public VMInfoListReturnForXmlRpc getVmStats(String vmId) {
        // prometheus
        return null;
    }

    @Override
    public VMInfoListReturnForXmlRpc getAllVmStats() {
        // prometheus
        return null;
    }

    @Override
    public HostDevListReturnForXmlRpc hostDevListByCaps() {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc migrate(Map<String, String> migrationInfo) {
        return null;
    }

    @Override
    public MigrateStatusReturnForXmlRpc migrateStatus(String vmId) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc migrateCancel(String vmId) {
        return null;
    }

    @Override
    public OneVmReturnForXmlRpc changeDisk(String vmId, String imageLocation) {
        return null;
    }

    @Override
    public OneVmReturnForXmlRpc changeFloppy(String vmId, String imageLocation) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc monitorCommand(String vmId, String monitorCommand) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc setVmTicket(String vmId, String otp64, String sec) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc setVmTicket(String vmId,
            String otp64,
            String sec,
            String connectionAction,
            Map<String, String> params) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc addNetwork(String bridge,
            String vlan,
            String bond,
            String[] nics,
            Map<String, String> options) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc delNetwork(String bridge, String vlan, String bond, String[] nics) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc editNetwork(String oldBridge,
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

    @Override
    public StatusOnlyReturnForXmlRpc setSafeNetworkConfig() {
        return null;
    }

    @Override
    public FenceStatusReturnForXmlRpc fenceNode(String ip,
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

    @Override
    public ServerConnectionStatusReturnForXmlRpc connectStorageServer(int serverType,
            String spUUID,
            Map<String, String>[] args) {
        return null;
    }

    @Override
    public ServerConnectionStatusReturnForXmlRpc disconnectStorageServer(int serverType,
            String spUUID,
            Map<String, String>[] args) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc createStorageDomain(int domainType,
            String sdUUID,
            String domainName,
            String arg,
            int storageType,
            String storageFormatType) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc formatStorageDomain(String sdUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc connectStoragePool(String spUUID,
            int hostSpmId,
            String SCSIKey,
            String masterdomainId,
            int masterVersion,
            Map<String, String> storageDomains) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc createStoragePool(int poolType,
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

    @Override
    public StatusOnlyReturnForXmlRpc reconstructMaster(String spUUID,
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

    @Override
    public OneStorageDomainStatsReturnForXmlRpc getStorageDomainStats(String sdUUID) {
        return null;
    }

    @Override
    public OneStorageDomainInfoReturnForXmlRpc getStorageDomainInfo(String sdUUID) {
        return null;
    }

    @Override
    public StorageDomainListReturnForXmlRpc getStorageDomainsList(String spUUID,
            int domainType,
            String poolType,
            String path) {
        return null;
    }

    @Override
    public FileStatsReturnForXmlRpc getIsoList(String spUUID) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList, boolean force) {
        return null;
    }

    @Override
    public VGListReturnForXmlRpc getVGList() {
        return null;
    }

    @Override
    public OneVGReturnForXmlRpc getVGInfo(String vgUUID) {
        return null;
    }

    @Override
    public LUNListReturnForXmlRpc getDeviceList(int storageType) {
        return null;
    }

    @Override
    public LUNListReturnForXmlRpc getDeviceList(int storageType, String[] devicesList, boolean checkStatus) {
        return null;
    }

    @Override
    public DevicesVisibilityMapReturnForXmlRpc getDevicesVisibility(String[] devicesList) {
        return null;
    }

    @Override
    public IQNListReturnForXmlRpc discoverSendTargets(Map<String, String> args) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc spmStart(String spUUID,
            int prevID,
            String prevLVER,
            int recoveryMode,
            String SCSIFencing,
            int maxHostId,
            String storagePoolFormatType) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc spmStop(String spUUID) {
        return null;
    }

    @Override
    public SpmStatusReturnForXmlRpc spmStatus(String spUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc refreshStoragePool(String spUUID, String msdUUID, int masterVersion) {
        return null;
    }

    @Override
    public TaskStatusReturnForXmlRpc getTaskStatus(String taskUUID) {
        return null;
    }

    @Override
    public TaskStatusListReturnForXmlRpc getAllTasksStatuses() {
        return null;
    }

    @Override
    public TaskInfoListReturnForXmlRpc getAllTasksInfo() {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc stopTask(String taskUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc clearTask(String taskUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc revertTask(String taskUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotplugDisk(Map info) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotunplugDisk(Map info) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotPlugNic(Map info) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotUnplugNic(Map info) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc vmUpdateDevice(String vmId, Map device) {
        return null;
    }

    @Override
    public FutureTask<Map<String, Object>> poll() {
        return null;
    }

    @Override
    public FutureTask<Map<String, Object>> timeBoundPoll(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen) {
        return null;
    }

    @Override
    public AlignmentScanReturnForXmlRpc getDiskAlignment(String vmId, Map<String, String> driveSpecs) {
        return null;
    }

    @Override
    public ImageSizeReturnForXmlRpc diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc merge(String vmId,
            Map<String, String> drive,
            String baseVolUUID,
            String topVolUUID,
            String bandwidth,
            String jobUUID) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList) {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSet(String volumeName, String key, String value) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeStart(String volumeName, Boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeStop(String volumeName, Boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeDelete(String volumeName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeReset(String volumeName, String volumeOption, Boolean force) {
        return null;
    }

    @Override
    public GlusterVolumeOptionsInfoReturnForXmlRpc glusterVolumeSetOptionsList() {
        return null;
    }

    @Override
    public GlusterTaskInfoReturnForXmlRpc glusterVolumeRemoveBricksStart(String volumeName,
            String[] brickList,
            int replicaCount,
            Boolean forceRemove) {
        return null;
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBricksStop(String volumeName,
            String[] brickList,
            int replicaCount) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeRemoveBricksCommit(String volumeName,
            String[] brickList,
            int replicaCount) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount,
            boolean force) {
        return null;
    }

    @Override
    public GlusterTaskInfoReturnForXmlRpc glusterVolumeRebalanceStart(String volumeName,
            Boolean fixLayoutOnly,
            Boolean force) {
        return null;
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStop(String volumeName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeReplaceBrickStart(String volumeName,
            String existingBrickDir,
            String newBrickDir) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHostRemove(String hostName, Boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHostAdd(String hostName) {
        return null;
    }

    @Override
    public GlusterServersListReturnForXmlRpc glusterServersList() {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeProfileStart(String volumeName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeProfileStop(String volumeName) {
        return null;
    }

    @Override
    public GlusterVolumeStatusReturnForXmlRpc glusterVolumeStatus(Guid clusterId,
            String volumeName,
            String brickName,
            String volumeStatusOption) {
        return null;
    }

    @Override
    public GlusterVolumesListReturnForXmlRpc glusterVolumesList(Guid clusterId) {
        return null;
    }

    @Override
    public GlusterVolumeProfileInfoReturnForXmlRpc glusterVolumeProfileInfo(Guid clusterId,
            String volumeName,
            boolean nfs) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookEnable(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookDisable(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override
    public GlusterHooksListReturnForXmlRpc glusterHooksList() {
        return null;
    }

    @Override
    public OneUuidReturnForXmlRpc glusterHostUUIDGet() {
        return null;
    }

    @Override
    public GlusterServicesReturnForXmlRpc glusterServicesList(Guid serverId, String[] serviceNames) {
        return null;
    }

    @Override
    public GlusterHookContentInfoReturnForXmlRpc glusterHookRead(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookUpdate(String glusterCommand,
            String stage,
            String hookName,
            String content,
            String checksum) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookAdd(String glusterCommand,
            String stage,
            String hookName,
            String content,
            String checksum,
            Boolean enabled) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookRemove(String glusterCommand, String stage, String hookName) {
        return null;
    }

    @Override
    public GlusterServicesReturnForXmlRpc glusterServicesAction(Guid serverId,
            String[] serviceList,
            String actionType) {
        return null;
    }

    @Override
    public StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID) {
        return null;
    }

    @Override
    public GlusterTasksListReturnForXmlRpc glusterTasksList() {
        return null;
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStatus(String volumeName) {
        return null;
    }

    @Override
    public BooleanReturnForXmlRpc glusterVolumeEmptyCheck(String volumeName) {
        return null;
    }

    @Override
    public GlusterHostsPubKeyReturnForXmlRpc glusterGeoRepKeysGet() {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String userName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterGeoRepMountBrokerSetup(String remoteVolumeName,
            String userName,
            String remoteGroupName,
            Boolean partial) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionCreate(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionDelete(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStop(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        return null;
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList() {
        return null;
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName) {
        return null;
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        return null;
    }

    @Override
    public GlusterVolumeGeoRepStatusDetailForXmlRpc glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStart(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionPause(String masterVolumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName,
            boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigSet(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String configValue,
            String userName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigReset(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String userName) {
        return null;
    }

    @Override
    public GlusterVolumeGeoRepConfigListXmlRpc glusterVolumeGeoRepConfigList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        return null;
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc setNumberOfCpus(String vmId, String numberOfCpus) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotplugMemory(Map info) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc updateVmPolicy(Map info) {
        return null;
    }

    @Override
    public VMListReturnForXmlRpc getExternalVmList(String uri, String username, String password) {
        return null;
    }

    @Override
    public OneVmReturnForXmlRpc getExternalVmFromOva(String ovaPath) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionResume(String volumeName,
            String slaveHostName,
            String slaveVolumeName,
            String userName,
            boolean force) {
        return null;
    }

    @Override
    public GlusterVolumeSnapshotInfoReturnForXmlRpc glusterVolumeSnapshotList(Guid clusterId, String volumeName) {
        return null;
    }

    @Override
    public GlusterVolumeSnapshotConfigReturnForXmlRpc glusterSnapshotConfigList(Guid clusterId) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotDelete(String snapshotName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSnapshotDeleteAll(String volumeName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotActivate(String snapshotName, boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotDeactivate(String snapshotName) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotRestore(String snapshotName) {
        return null;
    }

    @Override
    public GlusterVolumeSnapshotCreateReturnForXmlRpc glusterVolumeSnapshotCreate(String volumeName,
            String snapshotName,
            String description,
            boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSnapshotConfigSet(String volumeName,
            String cfgName,
            String cfgValue) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotConfigSet(String cfgName, String cfgValue) {
        return null;
    }

    @Override
    public OneStorageDeviceReturnForXmlRpc glusterCreateBrick(String lvName,
            String mountPoint,
            Map<String, Object> raidParams,
            String fsType,
            String[] storageDevices) {
        return null;
    }

    @Override
    public StorageDeviceListReturnForXmlRpc glusterStorageDeviceList() {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc hostdevChangeNumvfs(String deviceName, int numOfVfs) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc convertVmFromExternalSystem(String url,
            String user,
            String password,
            Map<String, Object> vm,
            String jobUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc convertVmFromOva(String ovaPath, Map<String, Object> vm, String jobUUID) {
        return null;
    }

    @Override
    public OvfReturnForXmlRpc getConvertedVm(String jobUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc deleteV2VJob(String jobUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc abortV2VJob(String jobUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotScheduleOverride(boolean force) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotScheduleReset() {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc unregisterSecrets(String[] libvirtSecretsUuids) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc freeze(String vmId) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc thaw(String vmId) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc isolateVolume(String sdUUID,
            String srcImageID,
            String dstImageID,
            String volumeID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc wipeVolume(String sdUUID, String imgUUID, String volUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        return null;
    }

    @Override
    public VolumeInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        return null;
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterStopProcesses() {
        return null;
    }
}
