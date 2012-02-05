package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.ovirt.engine.core.vdsbroker.irsbroker.IsoListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class VdsServerWrapper implements IVdsServer {

    private VdsServerConnector vdsServer;
    private HttpClient httpClient;

    public VdsServerWrapper(VdsServerConnector innerImplementor, HttpClient httpClient) {
        this.vdsServer = innerImplementor;
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public OneVmReturnForXmlRpc create(XmlRpcStruct createInfo) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.create(createInfo.getInnerMap());
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc destroy(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.destroy(vmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.shutdown(vmId, timeout, message);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc shutdownHost(int reboot) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.shutdownHost(reboot);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneVmReturnForXmlRpc pause(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.pause(vmId);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc hibernate(String vmId, String hiberVolHandle) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hibernate(vmId, hiberVolHandle);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneVmReturnForXmlRpc powerDown(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.shutdown(vmId);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneVmReturnForXmlRpc reset(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.reset(vmId);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneVmReturnForXmlRpc resume(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.cont(vmId);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public VMListReturnForXmlRpc list() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.list();
            VMListReturnForXmlRpc wrapper = new VMListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public VDSInfoReturnForXmlRpc getCapabilities() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVdsCapabilities();
            VDSInfoReturnForXmlRpc wrapper = new VDSInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public VDSInfoReturnForXmlRpc getVdsStats() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVdsStats();
            VDSInfoReturnForXmlRpc wrapper = new VDSInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc desktopLogin(String vmId, String domain, String user, String password) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.desktopLogin(vmId, domain, user, password);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc desktopLogoff(String vmId, String force) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.desktopLogoff(vmId, force);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc desktopLock(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.desktopLock(vmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public synchronized VMInfoListReturnForXmlRpc getVmStats(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVmStats(vmId);
            VMInfoListReturnForXmlRpc wrapper = new VMInfoListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public VMInfoListReturnForXmlRpc getAllVmStats() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getAllVmStats();
            VMInfoListReturnForXmlRpc wrapper = new VMInfoListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc migrate(Map<String, String> migrationInfo) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrate(migrationInfo);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc migrateStatus(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrateStatus(vmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc migrateCancel(String vmId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.migrateCancel(vmId);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneVmReturnForXmlRpc changeDisk(String vmId, String imageLocation) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.changeCD(vmId, imageLocation);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneVmReturnForXmlRpc changeFloppy(String vmId, String imageLocation) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.changeFloppy(vmId, imageLocation);
            OneVmReturnForXmlRpc wrapper = new OneVmReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc heartBeat() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.heartBeat();
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc monitorCommand(String vmId, String monitorCommand) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.monitorCommand(vmId, monitorCommand);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc sendHcCmdToDesktop(String vmId, String hcCommand) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.sendHcCmdToDesktop(vmId, hcCommand);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc setVmTicket(String vmId, String otp64, String sec) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setVmTicket(vmId, otp64, sec);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc startSpice(String vdsIp, int port, String ticket) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.startSpice(vdsIp, port, ticket);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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

    public StatusOnlyReturnForXmlRpc delNetwork(String bridge, String vlan, String bond, String[] nics) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.delNetwork(bridge, vlan, bond, nics);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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

    public StatusOnlyReturnForXmlRpc setupNetworks(XmlRpcStruct networks, XmlRpcStruct bonding, XmlRpcStruct options) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setupNetworks(networks.getInnerMap(),
                    bonding.getInnerMap(), options.getInnerMap());
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc setSafeNetworkConfig() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.setSafeNetworkConfig();
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public FenceStatusReturnForXmlRpc fenceNode(String ip, String port, String type, String user, String password,
            String action, String secured, String options) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.fenceNode(ip, port, type, user, password, action,
                    secured, options);
            FenceStatusReturnForXmlRpc wrapper = new FenceStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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

    public ServerConnectionStatusReturnForXmlRpc validateStorageServerConnection(int serverType, String spUUID,
            Map<String, String>[] args) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.validateStorageServerConnection(serverType, spUUID, args);
            ServerConnectionStatusReturnForXmlRpc wrapper =
                    new ServerConnectionStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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

    public ServerConnectionListReturnForXmlRpc getStorageConnectionsList(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageConnectionsList(spUUID);
            ServerConnectionListReturnForXmlRpc wrapper = new ServerConnectionListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc validateStorageDomain(String sdUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.validateStorageDomain(sdUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc createStorageDomain(int domainType, String sdUUID, String domainName, String arg,
            int storageType) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.createStorageDomain(domainType, sdUUID, domainName, arg,
                    storageType);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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

    public StatusOnlyReturnForXmlRpc formatStorageDomain(String sdUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.formatStorageDomain(sdUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc connectStoragePool(String spUUID, int hostSpmId, String SCSIKey,
            String masterdomainId, int masterVersion) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.connectStoragePool(spUUID, hostSpmId, SCSIKey,
                    masterdomainId, masterVersion);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.disconnectStoragePool(spUUID, hostSpmId, SCSIKey);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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

    public StatusOnlyReturnForXmlRpc reconstructMaster(String spUUID, String poolName, String masterDom,
            Map<String, String> domDict, int masterVersion, String lockPolicy, int lockRenewalIntervalSec,
            int leaseTimeSec, int ioOpTimeoutSec, int leaseRetries) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.reconstructMaster(spUUID, poolName, masterDom, domDict,
                    masterVersion, lockPolicy, lockRenewalIntervalSec, leaseTimeSec, ioOpTimeoutSec, leaseRetries);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneStorageDomainStatsReturnForXmlRpc getStorageDomainStats(String sdUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageDomainStats(sdUUID);
            OneStorageDomainStatsReturnForXmlRpc wrapper = new OneStorageDomainStatsReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneStorageDomainInfoReturnForXmlRpc getStorageDomainInfo(String sdUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageDomainInfo(sdUUID);
            OneStorageDomainInfoReturnForXmlRpc wrapper = new OneStorageDomainInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StorageDomainListReturnForXmlRpc getStorageDomainsList(String sdUUID, int domainType, int poolType,
            String path) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getStorageDomainsList(sdUUID, domainType, poolType, path);
            StorageDomainListReturnForXmlRpc wrapper = new StorageDomainListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public IsoListReturnForXmlRpc getIsoList(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getIsoList(spUUID);
            IsoListReturnForXmlRpc wrapper = new IsoListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.createVG(sdUUID, deviceList);
            OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc removeVG(String vgUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.removeVG(vgUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public VGListReturnForXmlRpc getVGList() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVGList();
            VGListReturnForXmlRpc wrapper = new VGListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneVGReturnForXmlRpc getVGInfo(String vgUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getVGInfo(vgUUID);
            OneVGReturnForXmlRpc wrapper = new OneVGReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public LUNListReturnForXmlRpc getDeviceList(int storageType) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getDeviceList(storageType);
            LUNListReturnForXmlRpc wrapper = new LUNListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneLUNReturnForXmlRpc getDeviceInfo(String devGUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getDeviceInfo(devGUID);
            OneLUNReturnForXmlRpc wrapper = new OneLUNReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public DevicesVisibilityMapReturnForXmlRpc getDevicesVisibility(String[] devicesList) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getDevicesVisibility(devicesList);
            DevicesVisibilityMapReturnForXmlRpc wrapper = new DevicesVisibilityMapReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    public IQNListReturnForXmlRpc discoverSendTargets(Map<String, String> args) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.discoverSendTargets(args);
            IQNListReturnForXmlRpc wrapper = new IQNListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public SessionsListReturnForXmlRpc getSessionList() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getSessionList();
            SessionsListReturnForXmlRpc wrapper = new SessionsListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public OneUuidReturnForXmlRpc spmStart(String spUUID, int prevID, String prevLVER, int recoveryMode,
            String SCSIFencing, int maxHostId) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.spmStart(spUUID, prevID, prevLVER, recoveryMode,
                    SCSIFencing, maxHostId);
            OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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

    public StatusOnlyReturnForXmlRpc spmStop(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.spmStop(spUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public SpmStatusReturnForXmlRpc spmStatus(String spUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getSpmStatus(spUUID);
            SpmStatusReturnForXmlRpc wrapper = new SpmStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc fenceSpmStorage(String spUUID, int prevID, String prevLVER) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.fenceSpmStorage(spUUID, prevID, prevLVER);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc refreshStoragePool(String spUUID, String msdUUID, int masterVersion) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.refreshStoragePool(spUUID, msdUUID, masterVersion);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public TaskStatusReturnForXmlRpc getTaskStatus(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getTaskStatus(taskUUID);
            TaskStatusReturnForXmlRpc wrapper = new TaskStatusReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public TaskStatusListReturnForXmlRpc getAllTasksStatuses() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getAllTasksStatuses();
            TaskStatusListReturnForXmlRpc wrapper = new TaskStatusListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public TaskInfoReturnForXmlRpc getTaskInfo(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getTaskInfo(taskUUID);
            TaskInfoReturnForXmlRpc wrapper = new TaskInfoReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public TaskInfoListReturnForXmlRpc getAllTasksInfo() {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.getAllTasksInfo();
            TaskInfoListReturnForXmlRpc wrapper = new TaskInfoListReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc stopTask(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.stopTask(taskUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

    public StatusOnlyReturnForXmlRpc clearTask(String taskUUID) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.clearTask(taskUUID);
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }

    }

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
    public StatusOnlyReturnForXmlRpc hotplugDisk(XmlRpcStruct info) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hotplugDisk(info.getInnerMap());
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotunplugDisk(XmlRpcStruct info) {
        try {
            Map<String, Object> xmlRpcReturnValue = vdsServer.hotunplugDisk(info.getInnerMap());
            StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
            return wrapper;
        } catch (UndeclaredThrowableException ute) {
            throw new XmlRpcRunTimeException(ute);
        }
    }
}
