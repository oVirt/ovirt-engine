package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicket;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.HttpUtils;
import org.ovirt.engine.core.vdsbroker.TransportRunTimeException;
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
import org.ovirt.engine.core.vdsbroker.vdsbroker.BooleanReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DeviceInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DevicesVisibilityMapReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DomainXmlListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FenceStatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostDevListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostJobsReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IQNListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ImageSizeReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ImageTicketInformationReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.LUNListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.LldpReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MeasureReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MigrateStatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NbdServerURLReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneStorageDomainInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneStorageDomainStatsReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneVGReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneVmReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OvfReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.QemuImageInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ScreenshotInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ServerConnectionStatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SpmStatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StorageDomainListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskInfoListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskStatusListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskStatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMInfoListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMNamesListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmExternalDataReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VolumeInfoReturn;
import org.ovirt.vdsm.jsonrpc.client.BrokerCommandCallback;
import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.RequestBuilder;
import org.ovirt.vdsm.jsonrpc.client.internal.ClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of <code>IVdsServer</code> interface which provides JSONRPC by
 * using {@link JsonRpcClient}.
 * Each method uses {@link RequestBuilder} to build request object and sends it
 * using client. The response is represented as {@link FutureMap} which is lazy
 * evaluated.
 *
 */
public class JsonRpcVdsServer implements IVdsServer {

    private static final Logger logger = LoggerFactory.getLogger(JsonRpcVdsServer.class);
    private final JsonRpcClient client;
    private final CloseableHttpClient httpClient;

    public JsonRpcVdsServer(JsonRpcClient client, CloseableHttpClient httpClient) {
        this.client = client;
        this.httpClient = httpClient;
    }

    @Override
    public void close() {
        HttpUtils.shutDownConnection(this.httpClient);
        this.client.close();
    }

    @Override
    public List<Certificate> getPeerCertificates() {
        try {
            return client.getClient().getPeerCertificates();
        } catch (ClientConnectionException | IllegalStateException e) {
            logger.error("Failed to get peer certification for host '{}': {}", client.getHostname(), e.getMessage());
            logger.debug("Exception", e);
            return null;
        }
    }

    @Override
    public CloseableHttpClient getHttpClient() {
        return this.httpClient;
    }

    @SuppressWarnings("rawtypes")
    private String getVmId(Map map) {
        return (String) map.get(VdsProperties.vm_guid);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OneVmReturn create(Map createInfo) {
        JsonRpcRequest request =
                new RequestBuilder("VM.create").withParameter("vmID", getVmId(createInfo))
                        .withParameter("vmParams", createInfo)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturn(response);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public StatusOnlyReturn copyData(String jobId, Map src, Map dst, boolean copyBitmaps) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.copy_data")
                        .withParameter("source", src)
                        .withParameter("destination", dst)
                        .withParameter("job_id", jobId)
                        .withParameter("copy_bitmaps", copyBitmaps)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn updateVolume(String jobId, Map<?, ?> volumeInfo, Map<?, ?> volumeAttributes) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.update_volume")
                        .withParameter("job_id", jobId)
                        .withParameter("vol_info", volumeInfo)
                        .withParameter("vol_attr", volumeAttributes)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn moveDomainDevice(String jobId, Map<String, Object> moveParams) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.move_domain_device")
                        .withParameter("job_id", jobId)
                        .withParameter("move_params", moveParams)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }


    @Override
    public StatusOnlyReturn reduceDomain(String jobId, Map<String, Object> reduceParams) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.reduce_domain")
                        .withParameter("job_id", jobId)
                        .withParameter("reduce_params", reduceParams)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn mergeSubchain(String jobId, Map<String, Object> subchainInfo, boolean mergeBitmaps) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.merge")
                        .withParameter("job_id", jobId)
                        .withParameter("subchain_info", subchainInfo)
                        .withParameter("merge_bitmaps", mergeBitmaps)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn destroy(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.destroy").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn shutdown(String vmId, String timeout, String message) {
        JsonRpcRequest request =
                new RequestBuilder("VM.shutdown").withParameter("vmID", vmId)
                        .withOptionalParameter("delay", timeout)
                        .withOptionalParameter("message", message)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn shutdown(String vmId, String timeout, String message, boolean reboot) {
        JsonRpcRequest request =
                new RequestBuilder("VM.shutdown").withParameter("vmID", vmId)
                        .withOptionalParameter("delay", timeout)
                        .withOptionalParameter("message", message)
                        .withParameter("reboot", reboot)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn reset(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.reset").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn setDestroyOnReboot(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.setDestroyOnReboot").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn hibernate(String vmId, String hiberVolHandle) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hibernate").withParameter("vmID", vmId)
                        .withParameter("hibernationVolHandle", hiberVolHandle)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public OneVmReturn resume(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.cont").withParameter("vmID", vmId).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturn(response);
    }

    @Override
    public VMListReturn list() {
        JsonRpcRequest request =
                new RequestBuilder("Host.getVMList").withOptionalParameterAsList("vmList",
                        new ArrayList<>(Arrays.asList(new String[]{}))).withParameter("onlyUUID", false).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                        .withResponseType(Object[].class);
        return new VMListReturn(response);
    }

    @Override
    public VMListReturn fullList(List<String> vmIds) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getVMFullList").withOptionalParameterAsList("vmList", vmIds).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                        .withResponseType(Object[].class);
        return new VMListReturn(response);
    }

    @Override
    public VDSInfoReturn getCapabilities() {
        JsonRpcRequest request = new RequestBuilder("Host.getCapabilities").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new VDSInfoReturn(response);
    }

    @Override
    public void getCapabilities(BrokerCommandCallback callback) {
        JsonRpcRequest request = new RequestBuilder("Host.getCapabilities").build();
        try {
            client.call(request, callback);
        } catch (ClientConnectionException e) {
            throw new TransportRunTimeException("Connection issues during send request", e);
        }
    }

    @Override
    public VDSInfoReturn getHardwareInfo() {
        JsonRpcRequest request = new RequestBuilder("Host.getHardwareInfo").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new VDSInfoReturn(response);
    }

    @Override
    public void getHardwareInfo(BrokerCommandCallback callback) {
        JsonRpcRequest request = new RequestBuilder("Host.getHardwareInfo").build();
        try {
            client.call(request, callback);
        } catch (ClientConnectionException e) {
            throw new TransportRunTimeException("Connection issues during send request", e);
        }
    }

    @Override
    public VDSInfoReturn getVdsStats() {
        JsonRpcRequest request = new RequestBuilder("Host.getStats").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new VDSInfoReturn(response);
    }

    @Override
    public void getVdsStats(BrokerCommandCallback callback) {
        JsonRpcRequest request = new RequestBuilder("Host.getStats").build();
        try {
            client.call(request, callback);
        } catch (ClientConnectionException e) {
            throw new TransportRunTimeException("Connection issues during send request", e);
        }
    }

    @Override
    public StatusOnlyReturn setMOMPolicyParameters(Map<String, Object> values) {
        JsonRpcRequest request =
                new RequestBuilder("Host.setMOMPolicyParameters").withParameter("key_value_store", values).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn desktopLogin(String vmId, String domain, String user, String password) {
        JsonRpcRequest request =
                new RequestBuilder("VM.desktopLogin").withParameter("vmID", vmId)
                        .withParameter("domain", domain)
                        .withParameter("username", user)
                        .withParameter("password", password)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn desktopLogoff(String vmId, String force) {
        JsonRpcRequest request =
                new RequestBuilder("VM.desktopLogoff").withParameter("vmID", vmId)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public VMInfoListReturn getVmStats(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.getStats").withParameter("vmID", vmId).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("statsList");
        return new VMInfoListReturn(response);
    }

    @Override
    public VMInfoListReturn getAllVmStats() {
        JsonRpcRequest request = new RequestBuilder("Host.getAllVmStats").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("statsList")
                        .withResponseType(Object[].class);
        return new VMInfoListReturn(response);
    }

    @Override
    public VmExternalDataReturn getVmExternalData(String vmId, VmExternalDataKind kind, boolean forceUpdate) {
        JsonRpcRequest request =
                new RequestBuilder("VM.getExternalData").withParameter("vmID", vmId)
                        .withParameter("kind", kind.getExternal())
                        .withParameter("forceUpdate", forceUpdate)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new VmExternalDataReturn(response);
    }

    @Override
    public HostDevListReturn hostDevListByCaps() {
        JsonRpcRequest request = new RequestBuilder("Host.hostdevListByCaps").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("deviceList");
        return new HostDevListReturn(response);
    }

    @Override
    public StatusOnlyReturn migrate(Map<String, Object> migrationInfo) {
        JsonRpcRequest request =
                new RequestBuilder("VM.migrate").withParameter("vmID", getVmId(migrationInfo))
                        .withParameter("params", migrationInfo)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public MigrateStatusReturn migrateStatus(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.getMigrationStatus").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request).withResponseKey("response")
                .withResponseType(Long.class);
        return new MigrateStatusReturn(response);
    }

    @Override
    public StatusOnlyReturn migrateCancel(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.migrateCancel").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public OneVmReturn changeDisk(String vmId, String imageLocation) {
        JsonRpcRequest request = new RequestBuilder("VM.changeCD").withParameter("vmID", vmId)
                .withParameter("driveSpec", imageLocation)
                .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturn(response);
    }

    @Override
    public OneVmReturn changeDisk(String vmId, Map<String, Object> driveSpec) {
        JsonRpcRequest request = new RequestBuilder("VM.changeCD").withParameter("vmID", vmId)
                .withParameter("driveSpec", driveSpec)
                .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturn(response);
    }

    @Override
    public StatusOnlyReturn addNetwork(String bridge,
            String vlan,
            String bond,
            String[] nics,
            Map<String, String> options) {
        JsonRpcRequest request =
                new RequestBuilder("Host.addNetwork").withParameter("bridge", bridge)
                        .withOptionalParameter("vlan", vlan)
                        .withOptionalParameter("bond", bond)
                        .withOptionalParameterAsList("nics", new ArrayList<>(Arrays.asList(nics)))
                        .withOptionalParameterAsMap("options", options)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn delNetwork(String bridge, String vlan, String bond, String[] nics) {
        // No options params (do we need it during this operation)
        JsonRpcRequest request = new RequestBuilder("Host.delNetwork").withParameter("bridge", bridge)
                .withOptionalParameter("vlan", vlan)
                .withOptionalParameter("bond", bond)
                .withOptionalParameterAsList("nics", new ArrayList<>(Arrays.asList(nics)))
                .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn editNetwork(String oldBridge,
            String newBridge,
            String vlan,
            String bond,
            String[] nics,
            Map<String, String> options) {
        JsonRpcRequest request =
                new RequestBuilder("Host.editNetwork").withParameter("oldBridge", oldBridge)
                        .withParameter("newBridge", newBridge)
                        .withOptionalParameter("vlan", vlan)
                        .withOptionalParameter("bond", bond)
                        .withOptionalParameterAsList("nics", new ArrayList<>(Arrays.asList(nics)))
                        .withOptionalParameterAsMap("options", options)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    static class FutureCallable implements Callable<Map<String, Object>> {
        private final Callable<Map<String, Object>> callable;

        private FutureMap map;

        public FutureCallable(Callable<Map<String, Object>> callable) {
            this.callable = callable;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            this.map = (FutureMap) this.callable.call();
            return this.map;
        }

        public boolean isDone() {
            if (this.map == null) {
                return false;
            }
            return this.map.isDone();
        }

        public boolean isCallableInvoked() {
            return map != null;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Future<Map<String, Object>> setupNetworks(Map networks, Map bonding, Map options, final boolean isPolicyReset) {
        final JsonRpcRequest request =
                new RequestBuilder("Host.setupNetworks").withParameter("networks", networks)
                        .withParameter("bondings", bonding)
                        .withParameter("options", options)
                        .build();
        final FutureCallable callable = new FutureCallable(() -> {
            if (isPolicyReset) {
                updateHeartbeatPolicy(client.getClientRetryPolicy().clone(), false);
            }
            return new FutureMap(client, request).withResponseKey("status");
        });
        FutureTask<Map<String, Object>> future = new FutureTask<Map<String, Object>>(callable) {

                    @Override
                    public boolean isDone() {
                        if (callable.isDone()) {
                            if (isPolicyReset) {
                                updateHeartbeatPolicy(client.getClientRetryPolicy(), true);
                            }
                            return true;
                        }
                        return !callable.isCallableInvoked() && super.isDone();
                    }
                };
        ThreadPoolUtil.execute(future);
        return future;
    }

    private void updateHeartbeatPolicy(ClientPolicy policy, boolean isHeartbeat) {
        policy.setIncomingHeartbeat(isHeartbeat);
        policy.setOutgoingHeartbeat(isHeartbeat);
        client.setClientRetryPolicy(policy);
    }

    @Override
    public StatusOnlyReturn setSafeNetworkConfig() {
        JsonRpcRequest request = new RequestBuilder("Host.setSafeNetworkConfig").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public FenceStatusReturn fenceNode(String ip,
            String port,
            String type,
            String user,
            String password,
            String action,
            String secured,
            String options,
            Map<String, Object> fencingPolicy) {
        JsonRpcRequest request =
                new RequestBuilder("Host.fenceNode").withParameter("addr", ip)
                        .withParameter("port", port)
                        .withParameter("agent", type)
                        .withParameter("username", user)
                        .withParameter("password", password)
                        .withParameter("action", action)
                        .withOptionalParameter("secure", secured)
                        .withOptionalParameter("options", options)
                        .withOptionalParameterAsMap("policy", fencingPolicy)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();

        return new FenceStatusReturn(response);
    }

    @Override
    public ServerConnectionStatusReturn connectStorageServer(int serverType,
            String spUUID,
            Map<String, String>[] args) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.connectStorageServer").withParameter("storagepoolID", spUUID)
                        .withParameter("domainType", serverType)
                        .withParameter("connectionParams", args)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("statuslist")
                        .withResponseType(Object[].class);
        return new ServerConnectionStatusReturn(response);
    }

    @Override
    public ServerConnectionStatusReturn disconnectStorageServer(int serverType,
            String spUUID,
            Map<String, String>[] args) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.disconnectStorageServer").withParameter("storagepoolID", spUUID)
                        .withParameter("domainType", serverType)
                        .withParameter("connectionParams", args)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("statuslist")
                        .withResponseType(Object[].class);
        return new ServerConnectionStatusReturn(response);
    }

    @Override
    public StatusOnlyReturn createStorageDomain(int domainType,
            String sdUUID,
            String domainName,
            String arg,
            int storageType,
            String storageFormatType,
            Integer blockSize,
            int maxHosts) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.create").withParameter("storagedomainID", sdUUID)
                        .withParameter("domainType", domainType)
                        .withParameter("typeArgs", arg)
                        .withParameter("name", domainName)
                        .withParameter("domainClass", storageType)
                        .withOptionalParameter("version", storageFormatType)
                        .withOptionalParameter("blockSize", blockSize)
                        .withParameter("maxHosts", maxHosts)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn formatStorageDomain(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.format").withParameter("storagedomainID", sdUUID)
                        .withParameter("autoDetach", false)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn connectStoragePool(String spUUID,
            int hostSpmId,
            String SCSIKey,
            String masterdomainId,
            int masterVersion,
            Map<String, String> storageDomains) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.connect").withParameter("storagepoolID", spUUID)
                        .withParameter("hostID", hostSpmId)
                        .withParameter("scsiKey", SCSIKey)
                        .withParameter("masterSdUUID", masterdomainId)
                        .withParameter("masterVersion", masterVersion)
                        .withOptionalParameterAsMap("domainDict", storageDomains)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.disconnect").withParameter("storagepoolID", spUUID)
                        .withParameter("hostID", hostSpmId)
                        .withParameter("scsiKey", SCSIKey)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn createStoragePool(int poolType,
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
        // poolType and lockPolicy not used in vdsm. We can remove from the interface
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.create")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("name", poolName)
                        .withParameter("masterSdUUID", msdUUID)
                        .withParameter("masterVersion", masterVersion)
                        .withParameter("domainList", new ArrayList<>(Arrays.asList(domList)))
                        .withParameter("lockRenewalIntervalSec", lockRenewalIntervalSec)
                        .withParameter("leaseTimeSec", leaseTimeSec)
                        .withParameter("ioOpTimeoutSec", ioOpTimeoutSec)
                        .withParameter("leaseRetries", leaseRetries)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn reconstructMaster(String spUUID,
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
        // no lockPolicy and hostSpmId not needed can be removed from the interface
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.reconstructMaster").withParameter("storagepoolID", spUUID)
                        .withParameter("hostId", hostSpmId)
                        .withParameter("name", poolName)
                        .withParameter("masterSdUUID", masterDom)
                        .withParameter("masterVersion", masterVersion)
                        .withParameter("domainDict", domDict)
                        .withParameter("lockRenewalIntervalSec", lockRenewalIntervalSec)
                        .withParameter("leaseTimeSec", leaseTimeSec)
                        .withParameter("ioOpTimeoutSec", ioOpTimeoutSec)
                        .withParameter("leaseRetries", leaseRetries)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public OneStorageDomainStatsReturn getStorageDomainStats(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getStats").withParameter("storagedomainID", sdUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("stats");
        return new OneStorageDomainStatsReturn(response);
    }

    @Override
    public OneStorageDomainInfoReturn getStorageDomainInfo(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getInfo").withParameter("storagedomainID", sdUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new OneStorageDomainInfoReturn(response);
    }

    @Override
    public StorageDomainListReturn getStorageDomainsList(String spUUID,
            int domainType,
            String poolType,
            String path) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getStorageDomains").withParameter("storagepoolID", spUUID)
                        .withParameter("domainClass", domainType)
                        .withParameter("storageType", poolType)
                        .withParameter("remotePath", path)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("domlist")
                        .withResponseType(Object[].class);
        return new StorageDomainListReturn(response);
    }

    @Override
    public OneUuidReturn createVG(String sdUUID, String[] deviceList, boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("LVMVolumeGroup.create").withParameter("name", sdUUID)
                        .withParameter("devlist", new ArrayList<>(Arrays.asList(deviceList)))
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturn(response);
    }

    @Override
    public OneVGReturn getVGInfo(String vgUUID) {
        JsonRpcRequest request =
                new RequestBuilder("LVMVolumeGroup.getInfo").withParameter("lvmvolumegroupID", vgUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new OneVGReturn(response);
    }

    @Override
    public LUNListReturn getDeviceList(int storageType, String[] devicesList, boolean checkStatus) {
        ArrayList<String> devicesListArray =
                devicesList != null ? new ArrayList<>(Arrays.asList(devicesList)) : null;
        JsonRpcRequest request =
                new RequestBuilder("Host.getDeviceList").withParameter("storageType",
                        storageType)
                        .withOptionalParameterAsList("guids", devicesListArray)
                        .withParameter("checkStatus", checkStatus)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseType(Object[].class)
                        .withResponseKey("devList");
        return new LUNListReturn(response);
    }

    @Override
    public DevicesVisibilityMapReturn getDevicesVisibility(String[] devicesList) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getDevicesVisibility").withParameter("guidList",
                        new ArrayList<>(Arrays.asList(devicesList))).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("visible");
        return new DevicesVisibilityMapReturn(response);
    }

    @Override
    public IQNListReturn discoverSendTargets(Map<String, String> args) {
        JsonRpcRequest request =
                new RequestBuilder("ISCSIConnection.discoverSendTargets").withParameter("host", args.get("connection"))
                        .withParameter("port", args.get("port"))
                        .withOptionalParameter("user", args.get("user"))
                        .withOptionalParameter("password", args.get("password"))
                        .withOptionalParameter("ipv6_enabled", args.get("ipv6_enabled"))
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withResponseKey("fullTargets");
        return new IQNListReturn(response);
    }

    @Override
    public OneUuidReturn spmStart(String spUUID,
            int prevID,
            String prevLVER,
            int recoveryMode,
            String SCSIFencing,
            int maxHostId,
            String storagePoolFormatType) {
        // storagePoolFormatType not used and can be removed from the interface
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.spmStart").withParameter("storagepoolID", spUUID)
                        .withParameter("prevID", prevID)
                        .withParameter("prevLver", prevLVER)
                        .withParameter("enableScsiFencing", SCSIFencing)
                        .withParameter("maxHostID", maxHostId)
                        .withOptionalParameter("domVersion", storagePoolFormatType)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid")
                        .withResponseType(String.class);
        return new OneUuidReturn(response);
    }

    @Override
    public StatusOnlyReturn spmStop(String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.spmStop").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public SpmStatusReturn spmStatus(String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getSpmStatus").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("spm_st");
        return new SpmStatusReturn(response);
    }

    @Override
    public HostJobsReturn getHostJobs(String jobType, List<String> jobIds) {
        JsonRpcRequest request = new RequestBuilder("Host.getJobs").withOptionalParameter("job_type", jobType).
                withOptionalParameterAsList("job_ids", jobIds).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("jobs");
        return new HostJobsReturn(response);
    }

    @Override
    public TaskStatusReturn getTaskStatus(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.getStatus").withParameter("taskID", taskUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("taskStatus");
        return new TaskStatusReturn(response);
    }

    @Override
    public TaskStatusListReturn getAllTasksStatuses() {
        JsonRpcRequest request = new RequestBuilder("Host.getAllTasksStatuses").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("allTasksStatus");
        return new TaskStatusListReturn(response);
    }

    @Override
    public TaskInfoListReturn getAllTasksInfo() {
        JsonRpcRequest request = new RequestBuilder("Host.getAllTasksInfo").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("allTasksInfo");
        return new TaskInfoListReturn(response);
    }

    @Override
    public StatusOnlyReturn stopTask(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.stop").withParameter("taskID", taskUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn clearTask(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.clear").withParameter("taskID", taskUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn revertTask(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.revert").withParameter("taskID", taskUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn cleanStorageDomainMetaData(String sdUUID, String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.detach").withParameter("storagedomainID", sdUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("force", true)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn hotplugDisk(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotplugDisk").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn hotunplugDisk(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotunplugDisk").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public VmInfoReturn hotPlugNic(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotplugNic").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new VmInfoReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn hotUnplugNic(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotunplugNic").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn vmUpdateDevice(String vmId, Map device) {
        JsonRpcRequest request =
                new RequestBuilder("VM.updateDevice").withParameter("vmID", vmId)
                        .withParameter("params", device)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    /**
     * @since engine 4.2.1
     * @since cluster compatibility version >= 4.2
     */
    @Deprecated
    @Override
    public FutureTask<Map<String, Object>> poll() {
        return timeBoundPoll(2, TimeUnit.SECONDS);
    }

    /**
     * @since engine 4.2.1
     * @since cluster compatibility version >= 4.2
     */
    @Deprecated
    @Override
    public FutureTask<Map<String, Object>> timeBoundPoll(final long timeout, final TimeUnit unit) {
        return timeBoundPollInternal(timeout, unit, "Host.ping");
    }

    @Override
    public FutureTask<Map<String, Object>> timeBoundPoll2(final long timeout, final TimeUnit unit) {
        return timeBoundPollInternal(timeout, unit, "Host.ping2");
    }

    @Override
    public FutureTask<Map<String, Object>> timeBoundPollConfirmConnectivity(final long timeout, final TimeUnit unit) {
        return timeBoundPollInternal(timeout, unit, "Host.confirmConnectivity");
    }

    private FutureTask<Map<String, Object>> timeBoundPollInternal(final long timeout, final TimeUnit unit, String verb) {
        final JsonRpcRequest request = new RequestBuilder(verb).build();
        final FutureCallable callable = new FutureCallable(() -> new FutureMap(client, request, timeout, unit, true));

        FutureTask<Map<String, Object>> future = new FutureTask<Map<String, Object>>(callable) {

                    @Override
                    public boolean isDone() {
                        return callable.isDone();
                    }
                };

        ThreadPoolUtil.execute(future);
        return future;
    }

    @Override
    public StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String jobUUID, int timeout) {
        return snapshot(vmId, disks, null, false, jobUUID, timeout);
    }

    @Override
    public StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory, String jobUUID, int timeout) {
        return snapshot(vmId, disks, memory, false, jobUUID, timeout);
    }

    @Override
    public StatusOnlyReturn snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen, String jobUUID, int timeout) {
        String timeoutType = "freeze_timeout";
        if (StringUtils.isNotEmpty(memory)) {
            timeoutType = "timeout";
        }
        JsonRpcRequest request =
                new RequestBuilder("VM.snapshot").withParameter("vmID", vmId)
                        .withParameter("snapDrives", new ArrayList<>(Arrays.asList(disks)))
                        .withOptionalParameter("snapMemory", memory)
                        .withParameter("frozen", frozen)
                        .withParameter("jobUUID", jobUUID)
                        .withParameter(timeoutType, timeout)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public ImageSizeReturn diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize) {
        JsonRpcRequest request =
                new RequestBuilder("VM.diskSizeExtend").withParameter("vmID", vmId)
                        .withParameter("driveSpecs", diskParams)
                        .withParameter("newSize", newSize)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("size");
        return new ImageSizeReturn(response);
    }

    @Override
    public StatusOnlyReturn merge(String vmId, Map<String, String> drive,
            String baseVolUUID, String topVolUUID, String bandwidth, String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("VM.merge").withParameter("vmID", vmId)
                        .withParameter("drive", drive)
                        .withParameter("baseVolUUID", baseVolUUID)
                        .withParameter("topVolUUID", topVolUUID)
                        .withParameter("bandwidth", bandwidth)
                        .withParameter("jobUUID", jobUUID)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public OneUuidReturn glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force,
            boolean isArbiter) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.create").withParameter("volumeName", volumeName)
                        .withParameter("bricklist", new ArrayList<>(Arrays.asList(brickList)))
                        .withParameter("replicaCount", replicaCount)
                        .withParameter("stripeCount", stripeCount)
                        .withParameter("transportList", new ArrayList<>(Arrays.asList(transportList)))
                        .withParameter("force", force)
                        .withParameter("arbiter", isArbiter)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new OneUuidReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeSet(String volumeName, String key, String value) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.set").withParameter("volumeName", volumeName)
                        .withParameter("option", key)
                        .withParameter("value", value)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    public GlusterVolumeGlobalOptionsInfoReturn glusterVolumeGlobalOptionsGet() {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.globalVolumeOptions").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new GlusterVolumeGlobalOptionsInfoReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeStart(String volumeName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.start").withParameter("volumeName", volumeName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeStop(String volumeName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.stop").withParameter("volumeName", volumeName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeDelete(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.delete").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeReset(String volumeName, String volumeOption, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.reset").withParameter("volumeName", volumeName)
                        .withParameter("option", volumeOption)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterVolumeOptionsInfoReturn glusterVolumeSetOptionsList() {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.setOptionsList").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeOptionsInfoReturn(response);
    }

    @Override
    public GlusterTaskInfoReturn glusterVolumeRemoveBricksStart(String volumeName,
            String[] brickList,
            int replicaCount,
            Boolean forceRemove) {
        String command = "GlusterVolume.removeBrickStart";
        if(forceRemove) {
            command = "GlusterVolume.removeBrickForce";
        }
        JsonRpcRequest request = new RequestBuilder(command).withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(brickList)))
                        .withParameter("replicaCount", replicaCount)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterTaskInfoReturn(response);
    }

    @Override
    public GlusterVolumeTaskReturn glusterVolumeRemoveBricksStop(String volumeName,
            String[] brickList,
            int replicaCount) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.removeBrickStop").withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(brickList)))
                        .withParameter("replicaCount", replicaCount)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new GlusterVolumeTaskReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeRemoveBricksCommit(String volumeName,
            String[] brickList,
            int replicaCount) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.removeBrickCommit").withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(brickList)))
                        .withParameter("replicaCount", replicaCount)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeBrickAdd(String volumeName,
            String[] bricks,
            int replicaCount,
            int stripeCount,
            boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.addBrick").withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(bricks)))
                        .withParameter("replicaCount", replicaCount)
                        .withParameter("stripeCount", stripeCount)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterTaskInfoReturn glusterVolumeRebalanceStart(String volumeName,
            Boolean fixLayoutOnly,
            Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.rebalanceStart").withParameter("volumeName", volumeName)
                        .withParameter("rebalanceType", fixLayoutOnly)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterTaskInfoReturn(response);
    }

    @Override
    public BooleanReturn glusterVolumeEmptyCheck(String volumeName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.volumeEmptyCheck").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new BooleanReturn(response, "volumeEmptyCheck");
    }

    @Override
    public GlusterHostsPubKeyReturn glusterGeoRepKeysGet() {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepKeysGet").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new GlusterHostsPubKeyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String userName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepKeysUpdate")
                        .withParameter("geoRepPubKeys", geoRepPubKeys)
                        .withOptionalParameter("userName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterGeoRepMountBrokerSetup(String remoteVolumeName, String userName, String remoteGroupName, Boolean partial) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepMountBrokerSetup").withParameter("remoteVolumeName", remoteVolumeName)
                        .withParameter("partial", partial)
                        .withOptionalParameter("remoteUserName", userName)
                        .withOptionalParameter("remoteGroupName", remoteGroupName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepSessionCreate(String volumeName, String remoteHost, String remotVolumeName, String userName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepSessionCreate").withParameter("volumeName", volumeName)
                .withParameter("remoteHost", remoteHost)
                .withParameter("remoteVolumeName", remotVolumeName)
                .withParameter("force", force)
                .withOptionalParameter("remoteUserName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepSessionResume(String volumeName,
            String slaveHostName,
            String slaveVolumeName,
            String userName,
            boolean force) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionResume").withParameter("volumeName", volumeName)
                        .withParameter("remoteHost", slaveHostName)
                        .withParameter("remoteVolumeName", slaveVolumeName)
                        .withOptionalParameter("remoteUserName", userName)
                        .withParameter("force", force).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterVolumeTaskReturn glusterVolumeRebalanceStop(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.rebalanceStop").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeTaskReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeReplaceBrickCommitForce(String volumeName,
            String existingBrickDir,
            String newBrickDir) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.replaceBrickCommitForce").withParameter("volumeName", volumeName)
                        .withParameter("existingBrick", existingBrickDir)
                        .withParameter("newBrick", newBrickDir)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterHostRemove(String hostName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHost.remove").withParameter("hostName", hostName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterHostAdd(String hostName) {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.add").withParameter("hostName", hostName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepSessionDelete(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionDelete")
                .withParameter("volumeName", volumeName)
                .withParameter("remoteHost", remoteHost)
                .withParameter("remoteVolumeName", remoteVolumeName)
                .withOptionalParameter("remoteUserName", userName)
                .build();

        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepSessionStop(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionStop")
                .withParameter("volumeName", volumeName)
                .withParameter("remoteHost", remoteHost)
                .withParameter("remoteVolumeName", remoteVolumeName)
                .withOptionalParameter("remoteUserName", userName)
                .withParameter("force", force)
                .build();

        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterServersListReturn glusterServersList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterServersListReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk) {
        JsonRpcRequest request =
                new RequestBuilder("VM.diskReplicateStart").withParameter("vmID", vmUUID)
                        .withParameter("srcDisk", srcDisk)
                        .withParameter("dstDisk", dstDisk)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk) {
        JsonRpcRequest request =
                new RequestBuilder("VM.diskReplicateFinish").withParameter("vmID", vmUUID)
                        .withParameter("srcDisk", srcDisk)
                        .withParameter("dstDisk", dstDisk)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeProfileStart(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.profileStart").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeProfileStop(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.profileStop").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepSessionPause(String masterVolumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName,
            boolean force) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionPause").withParameter("volumeName", masterVolumeName)
                        .withParameter("remoteHost", slaveHost)
                        .withParameter("remoteVolumeName", slaveVolumeName)
                        .withOptionalParameter("remoteUserName", userName)
                        .withParameter("force", force).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepSessionStart(String volumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionStart").withParameter("volumeName", volumeName)
                        .withParameter("remoteHost", remoteHost)
                        .withParameter("remoteVolumeName", remoteVolumeName)
                        .withOptionalParameter("remoteUserName", userName)
                        .withParameter("force", force).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepConfigSet(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String configValue,
            String userName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepConfigSet")
                .withParameter("volumeName", volumeName)
                .withParameter("remoteHost", slaveHost)
                .withParameter("remoteVolumeName", slaveVolumeName)
                .withParameter("optionName", configKey)
                .withParameter("optionValue", configValue)
                .withOptionalParameter("remoteUserName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeGeoRepConfigReset(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String configKey,
            String userName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepConfigReset")
                .withParameter("volumeName", volumeName)
                .withParameter("remoteHost", slaveHost)
                .withParameter("remoteVolumeName", slaveVolumeName)
                .withParameter("optionName", configKey)
                .withOptionalParameter("remoteUserName", userName)
                .build();

        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterVolumeGeoRepConfigList glusterVolumeGeoRepConfigList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepConfigList")
                .withParameter("volumeName", volumeName)
                .withParameter("remoteHost", slaveHost)
                .withParameter("remoteVolumeName", slaveVolumeName)
                .withOptionalParameter("remoteUserName", userName)
                .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new GlusterVolumeGeoRepConfigList(response);
    }

    @Override
    public GlusterVolumeStatusReturn glusterVolumeStatus(Guid clusterId,
            String volumeName,
            String brickName,
            String volumeStatusOption) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.status").withParameter("volumeName", volumeName)
                        .withParameter("brick", brickName)
                        .withParameter("statusOption", volumeStatusOption)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeStatusReturn(clusterId, response);
    }

    @Override
    public GlusterLocalLogicalVolumeListReturn glusterLogicalVolumeList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.logicalVolumeList").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterLocalLogicalVolumeListReturn(response);
    }

    @Override
    public GlusterLocalPhysicalVolumeListReturn glusterPhysicalVolumeList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.physicalVolumeList").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterLocalPhysicalVolumeListReturn(response);
    }

    @Override public GlusterVDOVolumeListReturn glusterVDOVolumeList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.vdoVolumeList").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVDOVolumeListReturn(response);
    }

    @Override
    public GlusterVolumesListReturn glusterVolumesList(Guid clusterId) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumesListReturn(clusterId, response);
    }

    @Override
    public GlusterVolumesListReturn glusterVolumeInfo(Guid clusterId, String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.list").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumesListReturn(clusterId, response);
    }

    @Override
    public GlusterVolumesHealInfoReturn glusterVolumeHealInfo(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.healInfo").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumesHealInfoReturn(response);
    }

    @Override
    public GlusterVolumeProfileInfoReturn glusterVolumeProfileInfo(Guid clusterId, String volumeName, boolean nfs) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.profileInfo").withParameter("volumeName", volumeName)
                .withParameter("nfs", nfs).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeProfileInfoReturn(clusterId, response);
    }

    @Override
    public StatusOnlyReturn glusterHookEnable(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.enable").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterHookDisable(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.disable").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterHooksListReturn glusterHooksList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHook.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterHooksListReturn(response);
    }

    @Override
    public OneUuidReturn glusterHostUUIDGet() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.uuid").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new OneUuidReturn(response);
    }

    @Override
    public GlusterServicesReturn glusterServicesList(Guid serverId, String[] serviceNames) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterService.get").withParameter("serviceNames",
                        new ArrayList<>(Arrays.asList(serviceNames))).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterServicesReturn(serverId, response);
    }

    @Override
    public GlusterHookContentInfoReturn glusterHookRead(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.read").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterHookContentInfoReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterHookUpdate(String glusterCommand,
            String stage,
            String hookName,
            String content,
            String checksum) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.update").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .withParameter("hookData", content)
                        .withParameter("hookChecksum", checksum)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterHookAdd(String glusterCommand,
            String stage,
            String hookName,
            String content,
            String checksum,
            Boolean enabled) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.add").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .withParameter("hookData", content)
                        .withParameter("hookChecksum", checksum)
                        .withParameter("enable", enabled)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterHookRemove(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.remove").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterServicesReturn glusterServicesAction(Guid serverId, String[] serviceList, String actionType) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterService.action").withParameter("serviceNames",
                        new ArrayList<>(Arrays.asList(serviceList)))
                        .withParameter("action", actionType)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterServicesReturn(serverId, response);
    }

    @Override
    public StoragePoolInfo getStoragePoolInfo(String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getInfo").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StoragePoolInfo(response);
    }

    @Override
    public GlusterTasksListReturn glusterTasksList() {
        JsonRpcRequest request = new RequestBuilder("GlusterTask.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterTasksListReturn(response);
    }

    @Override
    public GlusterVolumeTaskReturn glusterVolumeRebalanceStatus(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.rebalanceStatus").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeTaskReturn(response);
    }

    @Override
    public GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList() {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionList").build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatus(response);
    }

    @Override
    public GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList(String volumeName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionList")
                .withParameter("volumeName", volumeName)
                    .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatus(response);
    }

    @Override
    public GlusterVolumeGeoRepStatus glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepSessionList").withParameter("volumeName", volumeName)
                        .withParameter("remoteHost", slaveHost)
                        .withParameter("remoteVolumeName", slaveVolumeName)
                        .withOptionalParameter("remoteUserName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatus(response);
    }

    @Override
    public GlusterVolumeGeoRepStatusDetail glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepSessionStatus").withParameter("volumeName", volumeName)
                        .withParameter("remoteHost", slaveHost)
                        .withParameter("remoteVolumeName", slaveVolumeName)
                        .withOptionalParameter("remoteUserName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatusDetail(response);
    }

    @Override
    public GlusterVolumeTaskReturn glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.removeBrickStatus").withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(bricksList)))
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeTaskReturn(response);
    }

    @Override
    public StatusOnlyReturn setNumberOfCpus(String vmId, String numberOfCpus) {
        JsonRpcRequest request =
                new RequestBuilder("VM.setNumberOfCpus").withParameter("vmID", vmId)
                        .withParameter("numberOfCpus", numberOfCpus)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public StatusOnlyReturn hotplugMemory(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotplugMemory")
                        .withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn hotUnplugMemory(Map<String, Object> params) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotunplugMemory")
                        .withParameter("vmID", getVmId(params))
                        .withParameter("params", params)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturn updateVmPolicy(Map params) {
        JsonRpcRequest request =
                new RequestBuilder("VM.updateVmPolicy").withParameter("vmID", (String) params.get("vmId"))
                        .withParameter("params", params)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn setHaMaintenanceMode(String mode, boolean enabled) {
        JsonRpcRequest request =
                new RequestBuilder("Host.setHaMaintenanceMode").withParameter("mode", mode)
                        .withParameter("enabled", enabled)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn add_image_ticket(ImageTicket ticket) {

        JsonRpcRequest request =
                new RequestBuilder("Host.add_image_ticket")
                        .withParameter("ticket", ticket.toDict())
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn remove_image_ticket(String ticketId) {
        JsonRpcRequest request =
                new RequestBuilder("Host.remove_image_ticket")
                        .withParameter("uuid", ticketId)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);

    }

    @Override
    public StatusOnlyReturn extend_image_ticket(String ticketId, long timeout) {
        JsonRpcRequest request =
                new RequestBuilder("Host.extend_image_ticket")
                        .withParameter("uuid", ticketId)
                        .withParameter("timeout", timeout)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public ImageTicketInformationReturn getImageTicket(String ticketId) {
        JsonRpcRequest request =
                new RequestBuilder("Host.get_image_ticket")
                        .withParameter("uuid", ticketId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request)
                .withResponseKey("result");
        return new ImageTicketInformationReturn(response);
    }

    @Override
    public PrepareImageReturn prepareImage(String spID, String sdID, String imageID,
            String volumeID, boolean allowIllegal) {
        JsonRpcRequest request =
                new RequestBuilder("Image.prepare")
                        .withParameter("storagepoolID", spID)
                        .withParameter("storagedomainID", sdID)
                        .withParameter("imageID", imageID)
                        .withParameter("volumeID", volumeID)
                        .withParameter("allowIllegal", allowIllegal)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new PrepareImageReturn(response);
    }

    @Override
    public StatusReturn teardownImage(String spID, String sdID, String imageID, String volumeID) {
        JsonRpcRequest request =
                new RequestBuilder("Image.teardown")
                        .withParameter("storagepoolID", spID)
                        .withParameter("storagedomainID", sdID)
                        .withParameter("imageID", imageID)
                        .withParameter("leafVolID", volumeID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusReturn(response);
    }

    @Override
    public StatusReturn verifyUntrustedVolume(String spID, String sdID, String imageID, String volumeID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.verify_untrusted")
                        .withParameter("storagepoolID", spID)
                        .withParameter("storagedomainID", sdID)
                        .withParameter("imageID", imageID)
                        .withParameter("volumeID", volumeID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusReturn(response);
    }

    @Override
    public VMListReturn getExternalVmList(String uri, String username, String password, List<String> vmsNames) {
        RequestBuilder requestBuilder = new RequestBuilder("Host.getExternalVMs")
                .withParameter("uri", uri)
                .withParameter("username", username)
                .withParameter("password", password)
                .withOptionalParameterAsList("vm_names", vmsNames);
        JsonRpcRequest request = requestBuilder.build();

        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                        .withResponseType(Object[].class);
        return new VMListReturn(response);
    }

    @Override
    public VMNamesListReturn getExternalVmNamesList(String uri, String username, String password) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getExternalVMNames")
                        .withParameter("uri", uri)
                        .withParameter("username", username)
                        .withParameter("password", password)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmNames")
                        .withResponseType(Object[].class);
        return new VMNamesListReturn(response);
    }

    @Override
    public GlusterVolumeSnapshotInfoReturn glusterVolumeSnapshotList(Guid clusterId, String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotList").withOptionalParameter("volumeName", volumeName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeSnapshotInfoReturn(clusterId, response);
    }

    @Override
    public GlusterVolumeSnapshotConfigReturn glusterSnapshotConfigList(Guid clusterId) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.configList").build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeSnapshotConfigReturn(clusterId, response);
    }

    @Override
    public StatusOnlyReturn glusterSnapshotDelete(String snapshotName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.delete").withOptionalParameter("snapName", snapshotName)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeSnapshotDeleteAll(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotDeleteAll").withParameter("volumeName", volumeName)
                    .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterSnapshotActivate(String snapshotName, boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.activate").withParameter("snapName", snapshotName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterSnapshotDeactivate(String snapshotName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.deactivate").withParameter("snapName", snapshotName)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterSnapshotRestore(String snapshotName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.restore").withParameter("snapName", snapshotName)
                    .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public GlusterVolumeSnapshotCreateReturn glusterVolumeSnapshotCreate(String volumeName,
            String snapshotName,
            String description,
            boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotCreate").withParameter("volumeName", volumeName)
                        .withParameter("snapName", snapshotName)
                        .withOptionalParameter("snapDescription", description)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeSnapshotCreateReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeSnapshotConfigSet(String volumeName, String configName, String configValue) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotConfigSet").withParameter("volumeName", volumeName)
                        .withParameter("optionName", configName)
                        .withParameter("optionValue", configValue)
                        .build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterSnapshotConfigSet(String configName, String configValue) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.configSet").withParameter("optionName", configName)
                        .withParameter("optionValue", configValue)
                        .build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StatusOnlyReturn(response);
    }

    @Override
    public StorageDeviceListReturn glusterStorageDeviceList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.storageDevicesList").build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StorageDeviceListReturn(response);
    }

    @Override
    public OneStorageDeviceReturn glusterCreateBrick(String lvName,
            String mountPoint,
            Map<String, Object> raidParams, String fsType,
            String[] storageDevices) {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.createBrick").withParameter("name", lvName)
                .withParameter("mountPoint", mountPoint)
                .withParameter("devList", storageDevices)
                .withParameter("fsType", fsType)
                .withOptionalParameterAsMap("raidParams", raidParams).build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new OneStorageDeviceReturn(response);
    }

    @Override
    public StatusOnlyReturn hostdevChangeNumvfs(String deviceName, int numOfVfs) {
        JsonRpcRequest request =
                new RequestBuilder("Host.hostdevChangeNumvfs").withParameter("deviceName", deviceName)
                        .withParameter("numvfs", numOfVfs)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn convertVmFromExternalSystem(String uri,
            String username,
            String password,
            Map<String, Object> vm,
            String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.convertExternalVm")
        .withParameter("uri", uri)
        .withParameter("username", username)
        .withParameter("password", password)
        .withParameter("vminfo", vm)
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn convertVmFromOva(String ovaPath, Map<String, Object> vm, String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.convertExternalVmFromOva")
        .withParameter("ova_path", ovaPath)
        .withParameter("vminfo", vm)
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public OvfReturn getConvertedVm(String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getConvertedVm")
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request)
        .withResponseKey("ovf")
        .withResponseType(String.class);
        return new OvfReturn(response);
    }

    @Override
    public StatusOnlyReturn deleteV2VJob(String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.deleteV2VJob")
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn abortV2VJob(String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.abortV2VJob")
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterSnapshotScheduleOverride(boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotScheduleOverride").withParameter("force", force).build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterSnapshotScheduleReset() {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotScheduleReset").build();

        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets) {
        JsonRpcRequest request =
                new RequestBuilder("Host.registerSecrets").withParameter("secrets", libvirtSecrets)
                        .withParameter("clear", clearUnusedSecrets)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn unregisterSecrets(String[] libvirtSecretsUuids) {
        JsonRpcRequest request =
                new RequestBuilder("Host.unregisterSecrets").withParameter("uuids", libvirtSecretsUuids)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn freeze(String vmId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.freeze").withParameter("vmID", vmId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn thaw(String vmId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.thaw").withParameter("vmID", vmId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public VmBackupInfo startVmBackup(String vmId, Map<String, Object> backupConfig) {
        JsonRpcRequest request =
                new RequestBuilder("VM.start_backup")
                        .withParameter("vmID", vmId)
                        .withParameter("config", backupConfig)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new VmBackupInfo(response);
    }

    @Override
    public StatusOnlyReturn stopVmBackup(String vmId, String backupId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.stop_backup")
                        .withParameter("vmID", vmId)
                        .withParameter("backup_id", backupId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public VmBackupInfo vmBackupInfo(String vmId, String backupId, String checkpointId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.backup_info")
                        .withParameter("vmID", vmId)
                        .withParameter("backup_id", backupId)
                        .withParameter("checkpoint_id", checkpointId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new VmBackupInfo(response);
    }

    @Override
    public VmCheckpointIds redefineVmCheckpoints(String vmId, Collection<Map<String, Object>> checkpoints) {
        JsonRpcRequest request =
                new RequestBuilder("VM.redefine_checkpoints")
                        .withParameter("vmID", vmId)
                        .withParameter("checkpoints", checkpoints)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new VmCheckpointIds(response);
    }

    @Override
    public VmCheckpointIds deleteVmCheckpoints(String vmId, String[] checkpointIds) {
        JsonRpcRequest request =
                new RequestBuilder("VM.delete_checkpoints")
                        .withParameter("vmID", vmId)
                        .withParameter("checkpoint_ids", checkpointIds)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new VmCheckpointIds(response);
    }

    @Override
    public UUIDListReturn listVmCheckpoints(String vmId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.list_checkpoints")
                        .withParameter("vmID", vmId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withResponseKey("uuidlist")
                .withResponseType(Object[].class);
        return new UUIDListReturn(response);
    }

    @Override
    public StatusOnlyReturn addBitmap(String jobId, Map<String, Object> volInfo, String bitmapName) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.add_bitmap")
                        .withParameter("job_id", jobId)
                        .withParameter("vol_info", volInfo)
                        .withParameter("bitmap", bitmapName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn removeBitmap(String jobId, Map<String, Object> volInfo, String bitmapName) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.remove_bitmap")
                        .withParameter("job_id", jobId)
                        .withParameter("vol_info", volInfo)
                        .withParameter("bitmap", bitmapName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn clearBitmaps(String jobId, Map<String, Object> volInfo) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.clear_bitmaps")
                        .withParameter("job_id", jobId)
                        .withParameter("vol_info", volInfo)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public NbdServerURLReturn startNbdServer(String serverId, Map<String, Object> nbdServerConfig) {
        JsonRpcRequest request =
                new RequestBuilder("NBD.start_server")
                        .withParameter("server_id", serverId)
                        .withParameter("config", nbdServerConfig)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new NbdServerURLReturn(response);
    }

    @Override
    public StatusOnlyReturn stopNbdServer(String serverId) {
        JsonRpcRequest request =
                new RequestBuilder("NBD.stop_server")
                        .withParameter("server_id", serverId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn isolateVolume(String sdUUID, String srcImageID, String dstImageID, String volumeID) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.isolateVolume")
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("srcImageID", srcImageID)
                        .withParameter("dstImageID", dstImageID)
                        .withParameter("volumeID", volumeID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn wipeVolume(String sdUUID, String imgUUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.wipeVolume")
                 .withParameter("storagedomainID", sdUUID)
                .withParameter("imageID", imgUUID)
                .withParameter("volumeID", volUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public OneVmReturn getExternalVmFromOva(String ovaPath) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getExternalVmFromOva")
                        .withParameter("ova_path", ovaPath)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                .withResponseType(Object[].class);
        return new OneVmReturn(response);
    }

    @Override
    public StatusOnlyReturn refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.refresh")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgUUID)
                        .withParameter("volumeID", volUUID)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public VolumeInfoReturn getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.getInfo")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgUUID)
                        .withParameter("volumeID", volUUID)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new VolumeInfoReturn(response);
    }

    @Override
    public MeasureReturn measureVolume(String sdUUID,
            String spUUID,
            String imgUUID,
            String volUUID,
            int dstVolFormat,
            boolean withBacking) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.measure")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgUUID)
                        .withParameter("volumeID", volUUID)
                        .withParameter("dstVolFormat", dstVolFormat)
                        .withParameter("backing_chain", withBacking)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new MeasureReturn(response);
    }

    @Override
    public QemuImageInfoReturn getQemuImageInfo(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.getQemuImageInfo")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgUUID)
                        .withParameter("volumeID", volUUID)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new QemuImageInfoReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterStopProcesses() {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHost.processesStop").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn sparsifyVolume(String jobId, Map<String, Object> volumeAddress) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.sparsify_volume")
                        .withParameter("job_id", jobId)
                        .withParameter("vol_info", volumeAddress)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn amendVolume(String jobId, Map<String, Object> volInfo, Map<String, Object> qcow2_attr) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.amend_volume")
                        .withParameter("job_id", jobId)
                        .withParameter("vol_info", volInfo)
                        .withParameter("qcow2_attr", qcow2_attr)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn sealDisks(String vmId, String jobId, String storagePoolId, List<Map<String, Object>> images) {
        JsonRpcRequest request =
                new RequestBuilder("VM.seal")
                        .withParameter("vmID", vmId)
                        .withParameter("job_id", jobId)
                        .withParameter("sp_id", storagePoolId)
                        .withOptionalParameterAsList("images", images)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public DomainXmlListReturn dumpxmls(List<String> vmIds) {
        JsonRpcRequest request =
                new RequestBuilder("Host.dumpxmls").withOptionalParameterAsList("vmList", vmIds).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("domxmls")
                        .withResponseType(Object[].class);
        return new DomainXmlListReturn(response);
    }

    @Override
    public StatusOnlyReturn hotplugLease(Guid vmId, Guid storageDomainId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotplugLease").withParameter("vmID", vmId.toString())
                        .withParameter("lease", createLeaseDict(vmId, storageDomainId))
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterWebhookAdd(String url, String bearerToken) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterEvent.webhookAdd")
                .withParameter("url", url)
                .withParameter("bearerToken", bearerToken)
                .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn hotunplugLease(Guid vmId, Guid storageDomainId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotunplugLease").withParameter("vmID", vmId.toString())
                        .withParameter("lease", createLeaseDict(vmId, storageDomainId))
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterWebhookSync() {
        JsonRpcRequest request =
                new RequestBuilder("GlusterEvent.webhookSync").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    private Map<String, Object> createLeaseDict(Guid vmId, Guid storageDomainId) {
        Map<String, Object> lease = new HashMap<>();
        lease.put("type", "lease");
        lease.put("lease_id", vmId.toString());
        lease.put("sd_id", storageDomainId.toString());
        return lease;
    }

    @Override
    public LldpReturn getLldp(String[] interfaces) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("devices", interfaces);

        JsonRpcRequest request = new RequestBuilder("Host.getLldp")
                .withParameter("filter", filter).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new LldpReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterWebhookDelete(String url) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterEvent.webhookDelete")
                .withParameter("url", url)
                .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterWebhookUpdate(String url, String bearerToken) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterEvent.webhookUpdate")
                .withParameter("url", url)
                .withParameter("bearerToken", bearerToken)
                .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeResetBrickStart(String volumeName,
            String existingBrickDir) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.resetBrickStart").withParameter("volumeName", volumeName)
                        .withParameter("existingBrick", existingBrickDir)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public StatusOnlyReturn glusterVolumeResetBrickCommitForce(String volumeName,
            String existingBrickDir) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.resetBrickCommitForce").withParameter("volumeName", volumeName)
                        .withParameter("existingBrick", existingBrickDir)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public DeviceInfoReturn attachManagedBlockStorageVolume(Guid volumeId, Map<String, Object> connectionInfo) {
        JsonRpcRequest request =
                new RequestBuilder("ManagedVolume.attach_volume")
                        .withParameter("vol_id", volumeId.toString())
                        .withParameter("connection_info", connectionInfo)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new DeviceInfoReturn(response);
    }

    @Override
    public StatusOnlyReturn detachManagedBlockStorageVolume(Guid volumeId) {
        JsonRpcRequest request =
                new RequestBuilder("ManagedVolume.detach_volume")
                        .withParameter("vol_id", volumeId.toString())
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public VDSInfoReturn getLeaseStatus(String leaseUUID, String sdUUID) {
        Map<String, Object> leaseDict = new HashMap<>();
        leaseDict.put("lease_id", leaseUUID);
        leaseDict.put("sd_id", sdUUID);

        JsonRpcRequest request =
                new RequestBuilder("Lease.status")
                        .withParameter("lease", leaseDict)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new VDSInfoReturn(response);
    }


    @Override
    public StatusOnlyReturn fenceLeaseJob(String leaseUUID, String sdUUID, Map<String, Object> leaseMetadata) {
        Map<String, Object> leaseDict = new HashMap<>();
        leaseDict.put("lease_id", leaseUUID);
        leaseDict.put("sd_id", sdUUID);

        JsonRpcRequest request =
                new RequestBuilder("Lease.fence")
                        .withParameter("lease", leaseDict)
                        .withParameter("metadata", leaseMetadata)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturn(response);
    }

    @Override
    public ScreenshotInfoReturn createScreenshot(String vmId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.screenshot").withParameter("vmID", vmId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new ScreenshotInfoReturn(response);
    }
}
