package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.uutils.crypto.CertificateChain;
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
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VdsServerWrapper implements IVdsServer {

    private static final Logger logger = LoggerFactory.getLogger(VdsServerWrapper.class);
    private final VdsServerConnector vdsServer;
    private final HttpClient httpClient;

    public VdsServerWrapper(VdsServerConnector innerImplementor, HttpClient httpClient) {
        this.vdsServer = innerImplementor;
        this.httpClient = httpClient;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public OneVmReturnForXmlRpc create(Map createInfo) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.create(createInfo);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc createVolumeContainer(String jobId, Map<String, Object> createVolumeInfo) {
        Map<String, Object> xmlRpcReturnValue = vdsServer.createVolumeContainer(jobId, createVolumeInfo);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public StatusOnlyReturnForXmlRpc copyData(Map src, Map dst, boolean collapse) {
        Map<String, Object> xmlRpcReturnValue = vdsServer.copyData(src, dst, collapse);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public StatusOnlyReturnForXmlRpc allocateVolume(String spUUID, String sdUUID, String imgGUID, String volUUID, String size) {
        Map<String, Object> xmlRpcReturnValue = vdsServer.allocateVolume(sdUUID, spUUID, imgGUID, volUUID, size);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public StatusOnlyReturnForXmlRpc destroy(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.destroy(vmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.shutdown(vmId, timeout, message);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message, boolean reboot) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.shutdown(vmId, timeout, message, reboot);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public OneVmReturnForXmlRpc pause(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.pause(vmId);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc hibernate(String vmId, String hiberVolHandle) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hibernate(vmId, hiberVolHandle);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneVmReturnForXmlRpc resume(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.cont(vmId);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public VMListReturnForXmlRpc list() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.list();
            VMListReturnForXmlRpc wrapper = new VMListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public VMListReturnForXmlRpc list(String mode, String[] vmIds) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.list(mode, vmIds);
            VMListReturnForXmlRpc wrapper = new VMListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public VDSInfoReturnForXmlRpc getCapabilities() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVdsCapabilities();
            VDSInfoReturnForXmlRpc wrapper = new VDSInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public VDSInfoReturnForXmlRpc getHardwareInfo() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVdsHardwareInfo();
            VDSInfoReturnForXmlRpc wrapper = new VDSInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public VDSInfoReturnForXmlRpc getVdsStats() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVdsStats();
            VDSInfoReturnForXmlRpc wrapper = new VDSInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc desktopLogin(String vmId, String domain, String user, String password) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.desktopLogin(vmId, domain, user, password);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc desktopLogoff(String vmId, String force) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.desktopLogoff(vmId, force);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public synchronized VMInfoListReturnForXmlRpc getVmStats(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVmStats(vmId);
            VMInfoListReturnForXmlRpc wrapper = new VMInfoListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public VMInfoListReturnForXmlRpc getAllVmStats() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getAllVmStats();
            VMInfoListReturnForXmlRpc wrapper = new VMInfoListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public HostDevListReturnForXmlRpc hostDevListByCaps() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hostdevListByCaps();
            return new HostDevListReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc migrate(Map<String, Object> migrationInfo) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrate(migrationInfo);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public MigrateStatusReturnForXmlRpc migrateStatus(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrateStatus(vmId);
            return new MigrateStatusReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc migrateCancel(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrateCancel(vmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneVmReturnForXmlRpc changeDisk(String vmId, String imageLocation) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.changeCD(vmId, imageLocation);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneVmReturnForXmlRpc changeDisk(String vmId, Map<String, Object> driveSpec) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.changeCD(vmId, driveSpec);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneVmReturnForXmlRpc changeFloppy(String vmId, String imageLocation) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.changeFloppy(vmId, imageLocation);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc monitorCommand(String vmId, String monitorCommand) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.monitorCommand(vmId, monitorCommand);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc addNetwork(String bridge, String vlan, String bond, String[] nics,
            Map<String, String> options) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.addNetwork(bridge, vlan, bond, nics, options);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc delNetwork(String bridge, String vlan, String bond, String[] nics) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.delNetwork(bridge, vlan, bond, nics);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc editNetwork(String oldBridge, String newBridge, String vlan, String bond,
            String[] nics, Map<String, String> options) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.editNetwork(oldBridge, newBridge, vlan, bond, nics,
                    options);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc setSafeNetworkConfig() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setSafeNetworkConfig();
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public FenceStatusReturnForXmlRpc fenceNode(String ip, String port, String type, String user, String password,
                                                String action, String secured, String options,  Map<String, Object> fencingPolicy) {
        try {
            Map<String, Object> xmlRpcReturnValue;
            if (fencingPolicy == null) {
                // if fencing policy is null, fence proxy does not support fencing policy parameter
                xmlRpcReturnValue = vdsServer.fenceNode(ip, port, type, user, password, action,
                        secured, options);
            } else {
                xmlRpcReturnValue = vdsServer.fenceNode(ip, port, type, user, password, action,
                        secured, options, fencingPolicy);
            }
            FenceStatusReturnForXmlRpc wrapper = new FenceStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public ServerConnectionStatusReturnForXmlRpc connectStorageServer(int serverType, String spUUID,
            Map<String, String>[] args) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.connectStorageServer(serverType, spUUID, args);
            ServerConnectionStatusReturnForXmlRpc wrapper =
                    new ServerConnectionStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public ServerConnectionStatusReturnForXmlRpc disconnectStorageServer(int serverType, String spUUID,
            Map<String, String>[] args) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.disconnectStorageServer(serverType, spUUID, args);
            ServerConnectionStatusReturnForXmlRpc wrapper =
                    new ServerConnectionStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc createStorageDomain(int domainType, String sdUUID, String domainName, String arg,
            int storageType, String storageFormatType) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.createStorageDomain(domainType, sdUUID, domainName, arg,
                    storageType, storageFormatType);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc formatStorageDomain(String sdUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.formatStorageDomain(sdUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc connectStoragePool(String spUUID, int hostSpmId, String SCSIKey,
            String masterdomainId, int masterVersion, Map<String, String> storageDomains) {
        try {
            Map<String, Object> xmlRpcReturnValue;
            if (storageDomains == null) {
                xmlRpcReturnValue = vdsServer.connectStoragePool(spUUID, hostSpmId, SCSIKey,
                        masterdomainId, masterVersion);
            } else {
                xmlRpcReturnValue = vdsServer.connectStoragePool(spUUID, hostSpmId, SCSIKey,
                        masterdomainId, masterVersion, storageDomains);
            }
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.disconnectStoragePool(spUUID, hostSpmId, SCSIKey);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc createStoragePool(int poolType, String spUUID, String poolName, String msdUUID,
            String[] domList, int masterVersion, String lockPolicy, int lockRenewalIntervalSec, int leaseTimeSec,
            int ioOpTimeoutSec, int leaseRetries) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.createStoragePool(poolType, spUUID, poolName, msdUUID,
                    domList, masterVersion, lockPolicy, lockRenewalIntervalSec, leaseTimeSec, ioOpTimeoutSec,
                    leaseRetries);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc reconstructMaster(String spUUID, String poolName, String masterDom,
            Map<String, String> domDict, int masterVersion, String lockPolicy, int lockRenewalIntervalSec,
            int leaseTimeSec, int ioOpTimeoutSec, int leaseRetries, int hostSpmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.reconstructMaster(spUUID, poolName, masterDom,
                    domDict, masterVersion, lockPolicy, lockRenewalIntervalSec, leaseTimeSec, ioOpTimeoutSec,
                    leaseRetries, hostSpmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneStorageDomainStatsReturnForXmlRpc getStorageDomainStats(String sdUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageDomainStats(sdUUID);
            OneStorageDomainStatsReturnForXmlRpc wrapper = new OneStorageDomainStatsReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneStorageDomainInfoReturnForXmlRpc getStorageDomainInfo(String sdUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageDomainInfo(sdUUID);
            OneStorageDomainInfoReturnForXmlRpc wrapper = new OneStorageDomainInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StorageDomainListReturnForXmlRpc getStorageDomainsList(String sdUUID, int domainType, String poolType,
            String path) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageDomainsList(sdUUID, domainType, poolType, path);
            StorageDomainListReturnForXmlRpc wrapper = new StorageDomainListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList, boolean force) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.createVG(sdUUID, deviceList, force);
            OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneVGReturnForXmlRpc getVGInfo(String vgUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVGInfo(vgUUID);
            OneVGReturnForXmlRpc wrapper = new OneVGReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public LUNListReturnForXmlRpc getDeviceList(int storageType, String[] devicesList, boolean checkStatus) {
        try {
            String[] idsList = devicesList == null ? new String[] {} : devicesList;
            Map<String, Object> xmlRpcReturnValue = vdsServer.getDeviceList(storageType, idsList, checkStatus);
            LUNListReturnForXmlRpc wrapper = new LUNListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public DevicesVisibilityMapReturnForXmlRpc getDevicesVisibility(String[] devicesList) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getDevicesVisibility(devicesList);
            DevicesVisibilityMapReturnForXmlRpc wrapper = new DevicesVisibilityMapReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public IQNListReturnForXmlRpc discoverSendTargets(Map<String, String> args) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.discoverSendTargets(args);
            IQNListReturnForXmlRpc wrapper = new IQNListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public OneUuidReturnForXmlRpc spmStart(String spUUID, int prevID, String prevLVER, int recoveryMode,
            String SCSIFencing, int maxHostId, String storagePoolFormatType) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.spmStart(spUUID, prevID, prevLVER, recoveryMode,
                    SCSIFencing, maxHostId, storagePoolFormatType);
            OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc spmStop(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.spmStop(spUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public SpmStatusReturnForXmlRpc spmStatus(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getSpmStatus(spUUID);
            SpmStatusReturnForXmlRpc wrapper = new SpmStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public HostJobsReturnForXmlRpc getHostJobs(String jobType, List<String> jobIds) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getHostJobs(jobType, jobIds);
            HostJobsReturnForXmlRpc wrapper = new HostJobsReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public TaskStatusReturnForXmlRpc getTaskStatus(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getTaskStatus(taskUUID);
            TaskStatusReturnForXmlRpc wrapper = new TaskStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public TaskStatusListReturnForXmlRpc getAllTasksStatuses() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getAllTasksStatuses();
            TaskStatusListReturnForXmlRpc wrapper = new TaskStatusListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public TaskInfoListReturnForXmlRpc getAllTasksInfo() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getAllTasksInfo();
            TaskInfoListReturnForXmlRpc wrapper = new TaskInfoListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc stopTask(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.stopTask(taskUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc clearTask(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.clearTask(taskUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc revertTask(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.revertTask(taskUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc hotplugDisk(Map info) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hotplugDisk(info);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotunplugDisk(Map info) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hotunplugDisk(info);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotPlugNic(Map info) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hotplugNic(info);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotUnplugNic(Map info) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hotunplugNic(info);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public Future<Map<String, Object>> setupNetworks(Map networks,
            Map bonds,
            Map options,
            boolean isPolicyReset) {
        return vdsServer.futureSetupNetworks(networks, bonds, options);
    }

    @Override
    public FutureTask<Map<String, Object>> poll() {
        return vdsServer.futurePing();
    }

    @Override
    public FutureTask<Map<String, Object>> timeBoundPoll(long timeout, TimeUnit unit) {
        return poll();
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.snapshot(vmId, disks);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.snapshot(vmId, disks, memory);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.snapshot(vmId, disks, memory, frozen);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public AlignmentScanReturnForXmlRpc getDiskAlignment(String vmId, Map<String, String> driveSpecs) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getDiskAlignment(vmId, driveSpecs);
            AlignmentScanReturnForXmlRpc wrapper = new AlignmentScanReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public ImageSizeReturnForXmlRpc diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.diskSizeExtend(vmId, diskParams, newSize);
            ImageSizeReturnForXmlRpc wrapper = new ImageSizeReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc merge(String vmId, Map<String, String> drive,
            String baseVolUUID, String topVolUUID, String bandwidth, String jobUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.merge(vmId, drive,
                    baseVolUUID, topVolUUID, bandwidth, jobUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public OneUuidReturnForXmlRpc glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force) {
        try {
            return new OneUuidReturnForXmlRpc(vdsServer.glusterVolumeCreate(volumeName,
                    brickList,
                    replicaCount,
                    stripeCount,
                    transportList,
                    force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSet(String volumeName, String key, String value) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeSet(volumeName, key, value));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeStart(String volumeName, Boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeStart(volumeName, force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeStop(String volumeName, Boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeStop(volumeName, force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeReset(String volumeName, String volumeOption, Boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeReset(volumeName, volumeOption, force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeDelete(String volumeName) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeDelete(volumeName));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeOptionsInfoReturnForXmlRpc glusterVolumeSetOptionsList() {
        try {
            return new GlusterVolumeOptionsInfoReturnForXmlRpc(vdsServer.glusterVolumeSetOptionsList());
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterTaskInfoReturnForXmlRpc glusterVolumeRemoveBricksStart(String volumeName,
            String[] brickDirectories,
            int replicaCount,
            Boolean forceRemove) {
        try {
            if (forceRemove) {
                return new GlusterTaskInfoReturnForXmlRpc(vdsServer.glusterVolumeRemoveBrickForce(volumeName,
                        brickDirectories,
                        replicaCount));
            } else {
                return new GlusterTaskInfoReturnForXmlRpc(vdsServer.glusterVolumeRemoveBrickStart(volumeName,
                        brickDirectories,
                        replicaCount));
            }
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBricksStop(String volumeName,
            String[] brickDirectories,
            int replicaCount) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeRemoveBrickStop(volumeName,
                    brickDirectories,
                    replicaCount);
            GlusterVolumeTaskReturnForXmlRpc wrapper = new GlusterVolumeTaskReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeRemoveBricksCommit(String volumeName,
            String[] brickDirectories,
            int replicaCount) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeRemoveBrickCommit(volumeName,
                    brickDirectories,
                    replicaCount));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount,
            boolean force) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.glusterVolumeBrickAdd(volumeName, bricks, replicaCount, stripeCount, force);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterTaskInfoReturnForXmlRpc glusterVolumeRebalanceStart(String volumeName, Boolean fixLayoutOnly, Boolean force) {
        try {
            return new GlusterTaskInfoReturnForXmlRpc(vdsServer.glusterVolumeRebalanceStart(volumeName, fixLayoutOnly, force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public BooleanReturnForXmlRpc glusterVolumeEmptyCheck(String volumeName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeEmptyCheck(volumeName);
            BooleanReturnForXmlRpc wrapper = new BooleanReturnForXmlRpc(xmlRpcReturnValue, "volumeEmptyCheck");
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterHostsPubKeyReturnForXmlRpc glusterGeoRepKeysGet() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterGeoRepKeysGet();
            GlusterHostsPubKeyReturnForXmlRpc wrapper = new GlusterHostsPubKeyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String userName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterGeoRepKeysUpdate(userName, geoRepPubKeys);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterGeoRepMountBrokerSetup(String remoteVolumeName, String userName, String remoteGroupName, Boolean partial) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterGeoRepMountBrokerSetup(userName, remoteGroupName, remoteVolumeName, partial);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionCreate(String volumeName, String remoteHost, String remoteVolumeName, String userName, Boolean force) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeGeoRepSessionCreate(volumeName, remoteHost, remoteVolumeName, userName, force);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionResume(String volumeName,
            String slaveHostName,
            String slaveVolumeName,
            String userName,
            boolean force) {
        try{
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.glusterVolumeGeoRepSessionResume(volumeName,
                            slaveHostName,
                            slaveVolumeName,
                            userName,
                            force);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }
    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStop(String volumeName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeRebalanceStop(volumeName);
            GlusterVolumeTaskReturnForXmlRpc wrapper = new GlusterVolumeTaskReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHostRemove(String hostName, Boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterHostRemove(hostName, force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeReplaceBrickCommitForce(String volumeName, String existingBrickDir,
            String newBrickDir) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeReplaceBrickCommitForce(volumeName, existingBrickDir,
                    newBrickDir));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHostAdd(String hostName) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.glusterHostAdd(hostName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterServersListReturnForXmlRpc glusterServersList() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHostsList();
            GlusterServersListReturnForXmlRpc wrapper = new GlusterServersListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.diskReplicateStart(vmUUID, srcDisk, dstDisk);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.diskReplicateFinish(vmUUID, srcDisk, dstDisk);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeProfileStart(String volumeName) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeProfileStart(volumeName));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeProfileStop(String volumeName) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeProfileStop(volumeName));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionPause(String masterVolumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName,
            boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeGeoRepSessionPause(masterVolumeName,
                    slaveHost,
                    slaveVolumeName,
                    userName,
                    force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStart(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeGeoRepSessionStart(volumeName,
                    remoteHost,
                    remoteVolumeName,
                    userName,
                    force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeStatusReturnForXmlRpc glusterVolumeStatus(Guid clusterId,
            String volumeName, String brickName, String volumeStatusOption) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.glusterVolumeStatus(volumeName, brickName, volumeStatusOption);
            GlusterVolumeStatusReturnForXmlRpc wrapper =
                    new GlusterVolumeStatusReturnForXmlRpc(clusterId, xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumesListReturnForXmlRpc glusterVolumesList(Guid clusterId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumesList();
            GlusterVolumesListReturnForXmlRpc wrapper =
                    new GlusterVolumesListReturnForXmlRpc(clusterId, xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumesListReturnForXmlRpc glusterVolumeInfo(Guid clusterId, String volumeName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumesList(volumeName);
            GlusterVolumesListReturnForXmlRpc wrapper =
                    new GlusterVolumesListReturnForXmlRpc(clusterId, xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumesHealInfoReturnForXmlRpc glusterVolumeHealInfo(String volumeName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeHealInfo(volumeName);
            GlusterVolumesHealInfoReturnForXmlRpc wrapper =
                    new GlusterVolumesHealInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeProfileInfoReturnForXmlRpc glusterVolumeProfileInfo(Guid clusterId, String volumeName, boolean nfs) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeProfileInfo(volumeName, nfs);
            GlusterVolumeProfileInfoReturnForXmlRpc wrapper =
                    new GlusterVolumeProfileInfoReturnForXmlRpc(clusterId, xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc vmUpdateDevice(String vmId, Map device) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.vmUpdateDevice(vmId, device);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookEnable(String glusterCommand, String stage, String hookName) {
        try {

            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHookEnable(glusterCommand, stage, hookName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookDisable(String glusterCommand, String stage, String hookName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHookDisable(glusterCommand, stage, hookName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterHooksListReturnForXmlRpc glusterHooksList() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHooksList();
            GlusterHooksListReturnForXmlRpc wrapper = new GlusterHooksListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public OneUuidReturnForXmlRpc glusterHostUUIDGet() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHostUUIDGet();
            OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
             return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterHookContentInfoReturnForXmlRpc glusterHookRead(String glusterCommand, String stage, String hookName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHookRead(glusterCommand, stage, hookName);
            GlusterHookContentInfoReturnForXmlRpc wrapper =
                    new GlusterHookContentInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterServicesReturnForXmlRpc glusterServicesList(Guid serverId, String[] serviceNames) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterServicesGet(serviceNames);
            GlusterServicesReturnForXmlRpc wrapper = new GlusterServicesReturnForXmlRpc(serverId, xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookUpdate(String glusterCommand, String stage, String hookName, String content, String checksum) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHookUpdate(glusterCommand, stage, hookName, content, checksum);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

      @Override
    public StatusOnlyReturnForXmlRpc glusterHookAdd(String glusterCommand, String stage, String hookName,
            String content, String checksum, Boolean enabled) {
          try {
              Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHookAdd(glusterCommand, stage, hookName, content, checksum, enabled);
              StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
              return wrapper;
          } catch (UndeclaredThrowableException ute) {
              throw new XmlRpcRunTimeException(ute);
          }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookRemove(String glusterCommand, String stage, String hookName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterHookRemove(glusterCommand, stage, hookName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterServicesReturnForXmlRpc glusterServicesAction(Guid serverId, String [] serviceList, String actionType) {
        try {
          Map<String, Object> xmlRpcReturnValue = vdsServer.glusterServicesAction(serviceList, actionType);
          GlusterServicesReturnForXmlRpc wrapper = new GlusterServicesReturnForXmlRpc(serverId, xmlRpcReturnValue);
          return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStatus(String volumeName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeRebalanceStatus(volumeName);
            GlusterVolumeTaskReturnForXmlRpc wrapper = new GlusterVolumeTaskReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList() {
        try {
            Map<String, Object> response = vdsServer.glusterVolumeGeoRepSessionList();
            return new GlusterVolumeGeoRepStatusForXmlRpc(response);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName) {
        try {
            Map<String, Object> response;
            response = vdsServer.glusterVolumeGeoRepSessionList(volumeName);
            return new GlusterVolumeGeoRepStatusForXmlRpc(response);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        try {
            Map<String, Object> response;
            response = vdsServer.glusterVolumeGeoRepSessionList(volumeName, slaveHost, slaveVolumeName, userName);
            return new GlusterVolumeGeoRepStatusForXmlRpc(response);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepStatusDetailForXmlRpc glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        try {
            Map<String, Object> response =
                    vdsServer.glusterVolumeGeoRepSessionStatus(volumeName, slaveHost, slaveVolumeName, userName);
            return new GlusterVolumeGeoRepStatusDetailForXmlRpc(response);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeRemoveBrickStatus(volumeName, bricksList);
            GlusterVolumeTaskReturnForXmlRpc wrapper = new GlusterVolumeTaskReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionDelete(String volumeName, String remoteHost,
            String remoteVolumeName, String userName) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeGeoRepSessionDelete(volumeName,
                    remoteHost, remoteVolumeName, userName));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStop(String volumeName, String remoteHost,
            String remoteVolumeName, String userName, Boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeGeoRepSessionStop(volumeName,
                    remoteHost, remoteVolumeName, userName, force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigSet(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String configValue,
            String userName) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeGeoRepConfigSet(volumeName,
                    slaveHost,
                    slaveVolumeName,
                    configKey,
                    configValue,
                    userName));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigReset(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String userName) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeGeoRepConfigReset(volumeName,
                    slaveHost,
                    slaveVolumeName,
                    configKey,
                    userName));
        } catch(UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepConfigListXmlRpc glusterVolumeGeoRepConfigList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        try {
            return new GlusterVolumeGeoRepConfigListXmlRpc(vdsServer.glusterVolumeGeoRepConfigList(volumeName,
                    slaveHost,
                    slaveVolumeName,
                    userName));
        } catch(UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc setNumberOfCpus(String vmId, String numberOfCpus) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.setNumberOfCpus(vmId, numberOfCpus));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotplugMemory(Map info) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.hotplugMemory(info));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc updateVmPolicy(Map info) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.updateVmPolicy(info));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc setMOMPolicyParameters(Map<String, Object> key_value_store) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setMOMPolicyParameters(key_value_store);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc setHaMaintenanceMode(String mode, boolean enabled) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setHaMaintenanceMode(mode, enabled);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc add_image_ticket(String ticketId,
            String[] ops, long timeout, long size, String url) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.add_image_ticket(ticketId,
                    ops, timeout, size, url);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc remove_image_ticket(String ticketId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.remove_image_ticket(ticketId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc extend_image_ticket(String ticketId, long timeout) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.extend_image_ticket(ticketId, timeout);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public OneMapReturnForXmlRpc get_image_transfer_session_stats(String ticketId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.get_image_transfer_session_stats(ticketId);
            OneMapReturnForXmlRpc wrapper = new OneMapReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterTasksListReturnForXmlRpc glusterTasksList() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterTasksList();
            GlusterTasksListReturnForXmlRpc wrapper = new GlusterTasksListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID) {
        Map<String, Object> xmlRpcReturnValue = vdsServer.getStoragePoolInfo(spUUID);
        StoragePoolInfoReturnForXmlRpc wrapper = new StoragePoolInfoReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public void close() {
        XmlRpcUtils.shutDownConnection(this.httpClient);
    }

    @Override
    public List<Certificate> getPeerCertificates() {
        try {
            Pair<String, URL> connectionUrl =
                    XmlRpcUtils.getConnectionUrl(httpClient.getHostConfiguration().getHost(),
                            httpClient.getHostConfiguration().getPort(),
                            null,
                            Config.<Boolean>getValue(ConfigValues.EncryptHostCommunication));
            return CertificateChain.getSSLPeerCertificates(connectionUrl.getSecond());
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to get peer certification for host '{}': {}",
                    httpClient.getHostConfiguration().getHost(),
                    e.getMessage());
            logger.debug("Exception", e);
            return null;
        }
    }

    @Override
    public PrepareImageReturnForXmlRpc prepareImage(String spID, String sdID, String imageID,
            String volumeID, boolean allowIllegal) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.prepareImage(sdID, spID, imageID, volumeID, allowIllegal);
            PrepareImageReturnForXmlRpc wrapper = new PrepareImageReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusReturnForXmlRpc teardownImage(String sdID, String spID, String imageID, String volumeID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.teardownImage(sdID, spID, imageID, volumeID);
            StatusReturnForXmlRpc wrapper = new StatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusReturnForXmlRpc verifyUntrustedVolume(String spID, String sdID, String imageID, String volumeID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.verifyUntrustedVolume(sdID, spID, imageID, volumeID);
            StatusReturnForXmlRpc wrapper = new StatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public VMListReturnForXmlRpc getExternalVmList(String uri, String username, String password) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getExternalVMs(uri, username, password);
            VMListReturnForXmlRpc wrapper = new VMListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeSnapshotInfoReturnForXmlRpc glusterVolumeSnapshotList(Guid clusterId,
            String volumeName) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.glusterVolumeSnapshotList(volumeName == null ? "" : volumeName);
            GlusterVolumeSnapshotInfoReturnForXmlRpc wrapper =
                    new GlusterVolumeSnapshotInfoReturnForXmlRpc(clusterId, xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeSnapshotConfigReturnForXmlRpc glusterSnapshotConfigList(Guid clusterId) {
        try {
            Map<String, Object> xmlRpcReturnValue;
            xmlRpcReturnValue = vdsServer.glusterSnapshotConfigList();
            GlusterVolumeSnapshotConfigReturnForXmlRpc wrapper =
                    new GlusterVolumeSnapshotConfigReturnForXmlRpc(clusterId, xmlRpcReturnValue);
            return wrapper;
        }  catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeSnapshotCreateReturnForXmlRpc glusterVolumeSnapshotCreate(String volumeName,
            String snapshotName,
            String description,
            boolean force) {
        try {
            return new GlusterVolumeSnapshotCreateReturnForXmlRpc(vdsServer.glusterVolumeSnapshotCreate(volumeName,
                    snapshotName,
                    description,
                    force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotDelete(String snapshotName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterSnapshotDelete(snapshotName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSnapshotDeleteAll(String volumeName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterVolumeSnapshotDeleteAll(volumeName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotActivate(String snapshotName, boolean force) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterSnapshotActivate(snapshotName, force);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotDeactivate(String snapshotName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterSnapshotDeactivate(snapshotName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotRestore(String snapshotName) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterSnapshotRestore(snapshotName);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc hostdevChangeNumvfs(String deviceName, int numOfVfs) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hostdevChangeNumvfs(deviceName, numOfVfs);
            return new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSnapshotConfigSet(String volumeName, String cfgName, String cfgValue) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeSnapshotConfigSet(volumeName, cfgName, cfgValue));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotConfigSet(String cfgName, String cfgValue) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterSnapshotConfigSet(cfgName, cfgValue));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StorageDeviceListReturnForXmlRpc glusterStorageDeviceList() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterStorageDevicesList();
            StorageDeviceListReturnForXmlRpc wrapper = new StorageDeviceListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public OneStorageDeviceReturnForXmlRpc glusterCreateBrick(String lvName,
            String mountPoint,
            Map<String, Object> raidParams,
            String fsType,
            String[] storageDevices) {
        try {
            return new OneStorageDeviceReturnForXmlRpc(vdsServer.glusterCreateBrick(lvName,
                    mountPoint,
                    storageDevices,
                    fsType,
                    raidParams));
        } catch (UndeclaredThrowableException exp) {
            throw new XmlRpcRunTimeException(exp);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc convertVmFromExternalSystem(
            String url,
            String user,
            String password,
            Map<String, Object> vm,
            String jobUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.convertExternalVm(
                    url, user, password, vm, jobUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc convertVmFromOva(String ovaPath, Map<String, Object> vm, String jobUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.convertExternalVmFromOva(ovaPath, vm, jobUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public OvfReturnForXmlRpc getConvertedVm(String jobUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getConvertedVm(jobUUID);
            return new OvfReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc deleteV2VJob(String jobUUID) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.deleteV2VJob(jobUUID));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc abortV2VJob(String jobUUID) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.abortV2VJob(jobUUID));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotScheduleOverride(boolean force) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterSnapshotScheduleOverride(force));
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotScheduleReset() {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterSnapshotScheduleReset());
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.registerSecrets(libvirtSecrets, clearUnusedSecrets);
            return new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc unregisterSecrets(String[] libvirtSecretsUuids) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.unregisterSecrets(libvirtSecretsUuids);
            return new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc freeze(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.freeze(vmId);
            return new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc thaw(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.thaw(vmId);
            return new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc isolateVolume(String sdUUID, String srcImageID, String dstImageID, String volumeID) {
        Map<String, Object> xmlRpcReturnValue = vdsServer.isolateVolume(sdUUID, srcImageID,
                dstImageID, volumeID);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public StatusOnlyReturnForXmlRpc wipeVolume(String sdUUID, String imgUUID, String volUUID) {
        Map<String, Object> xmlRpcReturnValue = vdsServer.wipeVolume(sdUUID, imgUUID,
                volUUID);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public OneVmReturnForXmlRpc getExternalVmFromOva(String ovaPath) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getExternalVmFromOva(ovaPath);
            return new OneVmReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        try {
            Map<String, Object> xmpRpcReturnValue = vdsServer.refreshVolume(sdUUID, spUUID, imgUUID, volUUID);
            return new StatusOnlyReturnForXmlRpc(xmpRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public VolumeInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        try {
            Map<String, Object> xmpRpcReturnValue = vdsServer.getVolumeInfo(sdUUID, spUUID, imgUUID, volUUID);
            return new VolumeInfoReturnForXmlRpc(xmpRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterStopProcesses() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.glusterProcessesStop();
            return new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }
}
