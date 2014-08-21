package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHookContentInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterHooksListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServersListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterServicesReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTaskInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterTasksListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeGeoRepStatusDetailForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeOptionsInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeProfileInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumeTaskReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.gluster.GlusterVolumesListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VdsServerWrapper implements IVdsServer {

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
    public StatusOnlyReturnForXmlRpc migrate(Map<String, String> migrationInfo) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrate(migrationInfo);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc migrateStatus(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrateStatus(vmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
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
    public StatusOnlyReturnForXmlRpc heartBeat() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.heartBeat();
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
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
    public StatusOnlyReturnForXmlRpc setVmTicket(String vmId, String otp64, String sec) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setVmTicket(vmId, otp64, sec);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc setVmTicket(String vmId, String otp64, String sec, String connectionAction, Map<String, String> params) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setVmTicket(vmId, otp64, sec, connectionAction, params);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    @Override
    public StatusOnlyReturnForXmlRpc startSpice(String vdsIp, int port, String ticket) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.startSpice(vdsIp, port, ticket);
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
    public ServerConnectionListReturnForXmlRpc getStorageConnectionsList(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageConnectionsList(spUUID);
            ServerConnectionListReturnForXmlRpc wrapper = new ServerConnectionListReturnForXmlRpc(xmlRpcReturnValue);
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
    public FileStatsReturnForXmlRpc getIsoList(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getIsoList(spUUID);
            FileStatsReturnForXmlRpc wrapper = new FileStatsReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.createVG(sdUUID, deviceList);
            OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
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
    public VGListReturnForXmlRpc getVGList() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVGList();
            VGListReturnForXmlRpc wrapper = new VGListReturnForXmlRpc(xmlRpcReturnValue);
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
    public LUNListReturnForXmlRpc getDeviceList(int storageType) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getDeviceList(storageType);
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
    public StatusOnlyReturnForXmlRpc refreshStoragePool(String spUUID, String msdUUID, int masterVersion) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.refreshStoragePool(spUUID, msdUUID, masterVersion);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
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
            Map options) {
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
            String[] transportList) {
        try {
            return new OneUuidReturnForXmlRpc(vdsServer.glusterVolumeCreate(volumeName,
                    brickList,
                    replicaCount,
                    stripeCount,
                    transportList));
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
            int stripeCount) {
        try {
            Map<String, Object> xmlRpcReturnValue =
                    vdsServer.glusterVolumeBrickAdd(volumeName, bricks, replicaCount, stripeCount);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
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
    public StatusOnlyReturnForXmlRpc glusterVolumeReplaceBrickStart(String volumeName, String existingBrickDir,
            String newBrickDir) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.glusterVolumeReplaceBrickStart(volumeName, existingBrickDir,
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
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepStatus() {
        try {
            Map<String, Object> response = vdsServer.glusterVolumeGeoRepStatus();
            return new GlusterVolumeGeoRepStatusForXmlRpc(response);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepStatus(String volumeName) {
        try {
            Map<String, Object> response;
            response = vdsServer.glusterVolumeGeoRepStatus(volumeName);
            return new GlusterVolumeGeoRepStatusForXmlRpc(response);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepStatus(String volumeName, String slaveHost, String slaveVolumeName) {
        try {
            Map<String, Object> response;
            response = vdsServer.glusterVolumeGeoRepStatus(volumeName, slaveHost, slaveVolumeName);
            return new GlusterVolumeGeoRepStatusForXmlRpc(response);
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public GlusterVolumeGeoRepStatusDetailForXmlRpc glusterVolumeGeoRepStatusDetail(String volumeName, String slaveHost, String slaveVolumeName) {
        try {
            Map<String, Object> response = vdsServer.glusterVolumeGeoRepStatusDetail(volumeName, slaveHost, slaveVolumeName);
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
    public StatusOnlyReturnForXmlRpc setNumberOfCpus(String vmId, String numberOfCpus) {
        try {
            return new StatusOnlyReturnForXmlRpc(vdsServer.setNumberOfCpus(vmId, numberOfCpus));
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
}
