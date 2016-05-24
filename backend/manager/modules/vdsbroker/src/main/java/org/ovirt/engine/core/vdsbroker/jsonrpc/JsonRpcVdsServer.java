package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.commons.httpclient.HttpClient;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
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
import org.ovirt.engine.core.vdsbroker.vdsbroker.AlignmentScanReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BooleanReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DevicesVisibilityMapReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FenceStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostDevListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostJobsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IQNListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ImageSizeReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.LUNListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MigrateStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneMapReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneStorageDomainInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneStorageDomainStatsReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneVGReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OneVmReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.OvfReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ServerConnectionStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SpmStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StorageDomainListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskInfoListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskStatusListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.TaskStatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMInfoListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VMListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VolumeInfoReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;
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
    private final HttpClient httpClient;

    public JsonRpcVdsServer(JsonRpcClient client, HttpClient httpClient) {
        this.client = client;
        this.httpClient = httpClient;
    }

    @Override
    public void close() {
        XmlRpcUtils.shutDownConnection(this.httpClient);
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
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    @SuppressWarnings("rawtypes")
    private String getVmId(Map map) {
        return (String) map.get(VdsProperties.vm_guid);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OneVmReturnForXmlRpc create(Map createInfo) {
        JsonRpcRequest request =
                new RequestBuilder("VM.create").withParameter("vmID", getVmId(createInfo))
                        .withParameter("vmParams", createInfo)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc allocateVolume(String spUUID, String sdUUID, String imgGUID, String volUUID, String size) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.allocate").withParameter("volumeID", volUUID)
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgGUID)
                        .withParameter("size", size)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc copyData(Map src, Map dst, boolean collapse) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.copyData")
                        .withParameter("srcImage", src)
                        .withParameter("dstImage", dst)
                        .withParameter("collapse", collapse)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc createVolumeContainer(String jobId, Map<String, Object> createVolumeInfo) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.create_volume")
                        .withParameter("job_id", jobId)
                        .withParameter("vol_info", createVolumeInfo)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc destroy(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.destroy").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message) {
        JsonRpcRequest request =
                new RequestBuilder("VM.shutdown").withParameter("vmID", vmId)
                        .withOptionalParameter("delay", timeout)
                        .withOptionalParameter("message", message)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc shutdown(String vmId, String timeout, String message, boolean reboot) {
        JsonRpcRequest request =
                new RequestBuilder("VM.shutdown").withParameter("vmID", vmId)
                        .withOptionalParameter("delay", timeout)
                        .withOptionalParameter("message", message)
                        .withParameter("reboot", reboot)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneVmReturnForXmlRpc pause(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.pause").withParameter("vmID", vmId).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc hibernate(String vmId, String hiberVolHandle) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hibernate").withParameter("vmID", vmId)
                        .withParameter("hibernationVolHandle", hiberVolHandle)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneVmReturnForXmlRpc resume(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.cont").withParameter("vmID", vmId).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturnForXmlRpc(response);
    }

    @Override
    public VMListReturnForXmlRpc list() {
        JsonRpcRequest request =
                new RequestBuilder("Host.getVMList").withOptionalParameterAsList("vmList",
                        new ArrayList<>(Arrays.asList(new String[]{}))).withParameter("onlyUUID", false).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                        .withResponseType(Object[].class);
        return new VMListReturnForXmlRpc(response);
    }

    @Override
    public VMListReturnForXmlRpc list(String isFull, String[] vmIds) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getVMFullList").withOptionalParameterAsList("vmList",
                        new ArrayList<>(Arrays.asList(vmIds))).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                        .withResponseType(Object[].class);
        return new VMListReturnForXmlRpc(response);
    }

    @Override
    public VDSInfoReturnForXmlRpc getCapabilities() {
        JsonRpcRequest request = new RequestBuilder("Host.getCapabilities").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new VDSInfoReturnForXmlRpc(response);
    }

    @Override
    public VDSInfoReturnForXmlRpc getHardwareInfo() {
        JsonRpcRequest request = new RequestBuilder("Host.getHardwareInfo").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new VDSInfoReturnForXmlRpc(response);
    }

    @Override
    public VDSInfoReturnForXmlRpc getVdsStats() {
        JsonRpcRequest request = new RequestBuilder("Host.getStats").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new VDSInfoReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc setMOMPolicyParameters(Map<String, Object> values) {
        JsonRpcRequest request =
                new RequestBuilder("Host.setMOMPolicyParameters").withParameter("key_value_store", values).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc desktopLogin(String vmId, String domain, String user, String password) {
        JsonRpcRequest request =
                new RequestBuilder("VM.desktopLogin").withParameter("vmID", vmId)
                        .withParameter("domain", domain)
                        .withParameter("username", user)
                        .withParameter("password", password)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc desktopLogoff(String vmId, String force) {
        JsonRpcRequest request =
                new RequestBuilder("VM.desktopLogoff").withParameter("vmID", vmId)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public VMInfoListReturnForXmlRpc getVmStats(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.getStats").withParameter("vmID", vmId).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("statsList");
        return new VMInfoListReturnForXmlRpc(response);
    }

    @Override
    public VMInfoListReturnForXmlRpc getAllVmStats() {
        JsonRpcRequest request = new RequestBuilder("Host.getAllVmStats").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("statsList")
                        .withResponseType(Object[].class);
        return new VMInfoListReturnForXmlRpc(response);
    }

    @Override
    public HostDevListReturnForXmlRpc hostDevListByCaps() {
        JsonRpcRequest request = new RequestBuilder("Host.hostdevListByCaps").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("deviceList");
        return new HostDevListReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc migrate(Map<String, Object> migrationInfo) {
        JsonRpcRequest request =
                new RequestBuilder("VM.migrate").withParameter("vmID", getVmId(migrationInfo))
                        .withParameter("params", migrationInfo)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public MigrateStatusReturnForXmlRpc migrateStatus(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.getMigrationStatus").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request).withResponseKey("response")
                .withResponseType(Long.class);
        return new MigrateStatusReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc migrateCancel(String vmId) {
        JsonRpcRequest request = new RequestBuilder("VM.migrateCancel").withParameter("vmID", vmId).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneVmReturnForXmlRpc changeDisk(String vmId, String imageLocation) {
        JsonRpcRequest request = new RequestBuilder("VM.changeCD").withParameter("vmID", vmId)
                .withParameter("driveSpec", imageLocation)
                .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturnForXmlRpc(response);
    }

    @Override
    public OneVmReturnForXmlRpc changeDisk(String vmId, Map<String, Object> driveSpec) {
        JsonRpcRequest request = new RequestBuilder("VM.changeCD").withParameter("vmID", vmId)
                .withParameter("driveSpec", driveSpec)
                .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturnForXmlRpc(response);
    }

    @Override
    public OneVmReturnForXmlRpc changeFloppy(String vmId, String imageLocation) {
        // TODO DriveSpec should be used instead of imageLocation
        JsonRpcRequest request = new RequestBuilder("VM.changeFloppy").withParameter("vmID", vmId)
                .withParameter("driveSpec", imageLocation).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList");
        return new OneVmReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc monitorCommand(String vmId, String monitorCommand) {
        JsonRpcRequest request =
                new RequestBuilder("VM.monitorCommand").withParameter("vmID", vmId)
                        .withParameter("command", monitorCommand)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc addNetwork(String bridge,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc delNetwork(String bridge, String vlan, String bond, String[] nics) {
        // No options params (do we need it during this operation)
        JsonRpcRequest request = new RequestBuilder("Host.delNetwork").withParameter("bridge", bridge)
                .withOptionalParameter("vlan", vlan)
                .withOptionalParameter("bond", bond)
                .withOptionalParameterAsList("nics", new ArrayList<>(Arrays.asList(nics)))
                .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc editNetwork(String oldBridge,
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
        return new StatusOnlyReturnForXmlRpc(response);
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
                int connectionId = client.getConnectionId();
                updateHeartbeatPolicy(client.getClientRetryPolicy().clone(), false);

                if (client.isClosed() && client.getConnectionId() == connectionId) {
                    waitUntilCheck(client -> client.isClosed(),
                            "Waiting on losing connection to {}",
                            "Connection lost for {}");
                }
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
                        return false;
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

    /**
     * The method waits on {@link Predicate#test} condition. If it is not met after 10 seconds it would
     * throw {@link VDSNetworkException}.
     *
     * @param check - a lambda which provides condition function.
     * @param formatBefore - log formatter which accepts hostname as parameter. It is logged in debug level
     *                       before calling condition function.
     * @param formatAfter - log formatter which accepts hostname as parameter. Is is logged in debug level
     *                      after successful wait.
     * @throws InterruptedException - It is thrown when waiting operation was interrupted.
     */
    private void waitUntilCheck(Predicate<JsonRpcClient> check, String formatBefore, String formatAfter)
            throws InterruptedException {
        String hostname = client.getHostname();
        logger.debug(formatBefore, hostname);
        int retries = 50;
        while (check.test(this.client)) {
            if (retries == 0) {
                throw new VDSNetworkException("Unable to reconnect to " + hostname + " after policy reset");
            }
            retries--;
            TimeUnit.MILLISECONDS.sleep(200);
        }
        logger.debug(formatAfter, hostname);
    }

    @Override
    public StatusOnlyReturnForXmlRpc setSafeNetworkConfig() {
        JsonRpcRequest request = new RequestBuilder("Host.setSafeNetworkConfig").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
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

        return new FenceStatusReturnForXmlRpc(response);
    }

    @Override
    public ServerConnectionStatusReturnForXmlRpc connectStorageServer(int serverType,
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
        return new ServerConnectionStatusReturnForXmlRpc(response);
    }

    @Override
    public ServerConnectionStatusReturnForXmlRpc disconnectStorageServer(int serverType,
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
        return new ServerConnectionStatusReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc createStorageDomain(int domainType,
            String sdUUID,
            String domainName,
            String arg,
            int storageType,
            String storageFormatType) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.create").withParameter("storagedomainID", sdUUID)
                        .withParameter("domainType", domainType)
                        .withParameter("typeArgs", arg)
                        .withParameter("name", domainName)
                        .withParameter("domainClass", storageType)
                        .withOptionalParameter("version", storageFormatType)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc formatStorageDomain(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.format").withParameter("storagedomainID", sdUUID)
                        .withParameter("autoDetach", false)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc connectStoragePool(String spUUID,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc disconnectStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.disconnect").withParameter("storagepoolID", spUUID)
                        .withParameter("hostID", hostSpmId)
                        .withParameter("scsiKey", SCSIKey)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
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
        return new StatusOnlyReturnForXmlRpc(response);
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneStorageDomainStatsReturnForXmlRpc getStorageDomainStats(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getStats").withParameter("storagedomainID", sdUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("stats");
        return new OneStorageDomainStatsReturnForXmlRpc(response);
    }

    @Override
    public OneStorageDomainInfoReturnForXmlRpc getStorageDomainInfo(String sdUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StorageDomain.getInfo").withParameter("storagedomainID", sdUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new OneStorageDomainInfoReturnForXmlRpc(response);
    }

    @Override
    public StorageDomainListReturnForXmlRpc getStorageDomainsList(String spUUID,
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
        return new StorageDomainListReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc createVG(String sdUUID, String[] deviceList, boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("LVMVolumeGroup.create").withParameter("name", sdUUID)
                        .withParameter("devlist", new ArrayList<>(Arrays.asList(deviceList)))
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("uuid");
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public OneVGReturnForXmlRpc getVGInfo(String vgUUID) {
        JsonRpcRequest request =
                new RequestBuilder("LVMVolumeGroup.getInfo").withParameter("lvmvolumegroupID", vgUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("info");
        return new OneVGReturnForXmlRpc(response);
    }

    @Override
    public LUNListReturnForXmlRpc getDeviceList(int storageType, String[] devicesList, boolean checkStatus) {
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
        return new LUNListReturnForXmlRpc(response);
    }

    @Override
    public DevicesVisibilityMapReturnForXmlRpc getDevicesVisibility(String[] devicesList) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getDevicesVisibility").withParameter("guidList",
                        new ArrayList<>(Arrays.asList(devicesList))).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("visible");
        return new DevicesVisibilityMapReturnForXmlRpc(response);
    }

    @Override
    public IQNListReturnForXmlRpc discoverSendTargets(Map<String, String> args) {
        JsonRpcRequest request =
                new RequestBuilder("ISCSIConnection.discoverSendTargets").withParameter("host", args.get("connection"))
                        .withParameter("port", args.get("port"))
                        .withOptionalParameter("user", args.get("user"))
                        .withOptionalParameter("password", args.get("password"))
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withResponseKey("fullTargets");
        return new IQNListReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc spmStart(String spUUID,
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
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc spmStop(String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.spmStop").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public SpmStatusReturnForXmlRpc spmStatus(String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getSpmStatus").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("spm_st");
        return new SpmStatusReturnForXmlRpc(response);
    }

    @Override
    public HostJobsReturnForXmlRpc getHostJobs(String jobType, List<String> jobIds) {
        JsonRpcRequest request = new RequestBuilder("Host.getJobs").withOptionalParameter("job_type", jobType).
                withOptionalParameterAsList("job_ids", jobIds).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("jobs");
        return new HostJobsReturnForXmlRpc(response);
    }

    @Override
    public TaskStatusReturnForXmlRpc getTaskStatus(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.getStatus").withParameter("taskID", taskUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("taskStatus");
        return new TaskStatusReturnForXmlRpc(response);
    }

    @Override
    public TaskStatusListReturnForXmlRpc getAllTasksStatuses() {
        JsonRpcRequest request = new RequestBuilder("Host.getAllTasksStatuses").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("allTasksStatus");
        return new TaskStatusListReturnForXmlRpc(response);
    }

    @Override
    public TaskInfoListReturnForXmlRpc getAllTasksInfo() {
        JsonRpcRequest request = new RequestBuilder("Host.getAllTasksInfo").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("allTasksInfo");
        return new TaskInfoListReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc stopTask(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.stop").withParameter("taskID", taskUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc clearTask(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.clear").withParameter("taskID", taskUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc revertTask(String taskUUID) {
        JsonRpcRequest request = new RequestBuilder("Task.revert").withParameter("taskID", taskUUID).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc hotplugDisk(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotplugDisk").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc hotunplugDisk(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotunplugDisk").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc hotPlugNic(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotplugNic").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc hotUnplugNic(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotunplugNic").withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc vmUpdateDevice(String vmId, Map device) {
        JsonRpcRequest request =
                new RequestBuilder("VM.updateDevice").withParameter("vmID", vmId)
                        .withParameter("params", device)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public FutureTask<Map<String, Object>> poll() {
        return timeBoundPoll(2, TimeUnit.SECONDS);
    }

    @Override
    public FutureTask<Map<String, Object>> timeBoundPoll(final long timeout, final TimeUnit unit) {
        final JsonRpcRequest request = new RequestBuilder("Host.ping").build();
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
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks) {
        return snapshot(vmId, disks, null, false);
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory) {
        return snapshot(vmId, disks, memory, false);
    }

    @Override
    public StatusOnlyReturnForXmlRpc snapshot(String vmId, Map<String, String>[] disks, String memory, boolean frozen) {
        JsonRpcRequest request =
                new RequestBuilder("VM.snapshot").withParameter("vmID", vmId)
                        .withParameter("snapDrives", new ArrayList<>(Arrays.asList(disks)))
                        .withOptionalParameter("snapMemory", memory)
                        .withParameter("frozen", frozen)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public AlignmentScanReturnForXmlRpc getDiskAlignment(String vmId, Map<String, String> driveSpecs) {
        JsonRpcRequest request =
                new RequestBuilder("VM.getDiskAlignment").withParameter("vmID", vmId)
                        .withParameter("disk", driveSpecs)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("alignment");
        return new AlignmentScanReturnForXmlRpc(response);
    }

    @Override
    public ImageSizeReturnForXmlRpc diskSizeExtend(String vmId, Map<String, String> diskParams, String newSize) {
        JsonRpcRequest request =
                new RequestBuilder("VM.diskSizeExtend").withParameter("vmID", vmId)
                        .withParameter("driveSpecs", diskParams)
                        .withParameter("newSize", newSize)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("size");
        return new ImageSizeReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc merge(String vmId, Map<String, String> drive,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc glusterVolumeCreate(String volumeName,
            String[] brickList,
            int replicaCount,
            int stripeCount,
            String[] transportList,
            boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.create").withParameter("volumeName", volumeName)
                        .withParameter("bricklist", new ArrayList<>(Arrays.asList(brickList)))
                        .withParameter("replicaCount", replicaCount)
                        .withParameter("stripeCount", stripeCount)
                        .withParameter("transportList", new ArrayList<>(Arrays.asList(transportList)))
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSet(String volumeName, String key, String value) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.set").withParameter("volumeName", volumeName)
                        .withParameter("option", key)
                        .withParameter("value", value)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeStart(String volumeName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.start").withParameter("volumeName", volumeName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeStop(String volumeName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.stop").withParameter("volumeName", volumeName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeDelete(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.delete").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeReset(String volumeName, String volumeOption, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.reset").withParameter("volumeName", volumeName)
                        .withParameter("option", volumeOption)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeOptionsInfoReturnForXmlRpc glusterVolumeSetOptionsList() {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.setOptionsList").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeOptionsInfoReturnForXmlRpc(response);
    }

    @Override
    public GlusterTaskInfoReturnForXmlRpc glusterVolumeRemoveBricksStart(String volumeName,
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
        return new GlusterTaskInfoReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBricksStop(String volumeName,
            String[] brickList,
            int replicaCount) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.removeBrickStop").withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(brickList)))
                        .withParameter("replicaCount", replicaCount)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new GlusterVolumeTaskReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeRemoveBricksCommit(String volumeName,
            String[] brickList,
            int replicaCount) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.removeBrickCommit").withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(brickList)))
                        .withParameter("replicaCount", replicaCount)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeBrickAdd(String volumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterTaskInfoReturnForXmlRpc glusterVolumeRebalanceStart(String volumeName,
            Boolean fixLayoutOnly,
            Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.rebalanceStart").withParameter("volumeName", volumeName)
                        .withParameter("rebalanceType", fixLayoutOnly)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterTaskInfoReturnForXmlRpc(response);
    }

    @Override
    public BooleanReturnForXmlRpc glusterVolumeEmptyCheck(String volumeName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.volumeEmptyCheck").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new BooleanReturnForXmlRpc(response, "volumeEmptyCheck");
    }

    @Override
    public GlusterHostsPubKeyReturnForXmlRpc glusterGeoRepKeysGet() {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepKeysGet").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new GlusterHostsPubKeyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterGeoRepKeysUpdate(List<String> geoRepPubKeys, String userName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepKeysUpdate")
                        .withParameter("geoRepPubKeys", geoRepPubKeys)
                        .withOptionalParameter("userName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterGeoRepMountBrokerSetup(String remoteVolumeName, String userName, String remoteGroupName, Boolean partial) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepMountBrokerSetup").withParameter("remoteVolumeName", remoteVolumeName)
                        .withParameter("partial", partial)
                        .withOptionalParameter("remoteUserName", userName)
                        .withOptionalParameter("remoteGroupName", remoteGroupName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionCreate(String volumeName, String remoteHost, String remotVolumeName, String userName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepSessionCreate").withParameter("volumeName", volumeName)
                .withParameter("remoteHost", remoteHost)
                .withParameter("remoteVolumeName", remotVolumeName)
                .withParameter("force", force)
                .withOptionalParameter("remoteUserName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionResume(String volumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStop(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.rebalanceStop").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeTaskReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeReplaceBrickCommitForce(String volumeName,
            String existingBrickDir,
            String newBrickDir) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.replaceBrickCommitForce").withParameter("volumeName", volumeName)
                        .withParameter("existingBrick", existingBrickDir)
                        .withParameter("newBrick", newBrickDir)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHostRemove(String hostName, Boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHost.remove").withParameter("hostName", hostName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHostAdd(String hostName) {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.add").withParameter("hostName", hostName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionDelete(String volumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStop(String volumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterServersListReturnForXmlRpc glusterServersList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterServersListReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc diskReplicateStart(String vmUUID, Map srcDisk, Map dstDisk) {
        JsonRpcRequest request =
                new RequestBuilder("VM.diskReplicateStart").withParameter("vmID", vmUUID)
                        .withParameter("srcDisk", srcDisk)
                        .withParameter("dstDisk", dstDisk)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc diskReplicateFinish(String vmUUID, Map srcDisk, Map dstDisk) {
        JsonRpcRequest request =
                new RequestBuilder("VM.diskReplicateFinish").withParameter("vmID", vmUUID)
                        .withParameter("srcDisk", srcDisk)
                        .withParameter("dstDisk", dstDisk)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeProfileStart(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.profileStart").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeProfileStop(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.profileStop").withParameter("volumeName", volumeName).build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionPause(String masterVolumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepSessionStart(String volumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigSet(String volumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeGeoRepConfigReset(String volumeName,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeGeoRepConfigListXmlRpc glusterVolumeGeoRepConfigList(String volumeName,
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
        return new GlusterVolumeGeoRepConfigListXmlRpc(response);
    }

    @Override
    public GlusterVolumeStatusReturnForXmlRpc glusterVolumeStatus(Guid clusterId,
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
        return new GlusterVolumeStatusReturnForXmlRpc(clusterId, response);
    }

    @Override
    public GlusterVolumesListReturnForXmlRpc glusterVolumesList(Guid clusterId) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumesListReturnForXmlRpc(clusterId, response);
    }

    @Override
    public GlusterVolumesListReturnForXmlRpc glusterVolumeInfo(Guid clusterId, String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.list").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumesListReturnForXmlRpc(clusterId, response);
    }

    @Override
    public GlusterVolumesHealInfoReturnForXmlRpc glusterVolumeHealInfo(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.healInfo").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumesHealInfoReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeProfileInfoReturnForXmlRpc glusterVolumeProfileInfo(Guid clusterId, String volumeName, boolean nfs) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.profileInfo").withParameter("volumeName", volumeName)
                .withParameter("nfs", nfs).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeProfileInfoReturnForXmlRpc(clusterId, response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookEnable(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.enable").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookDisable(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.disable").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterHooksListReturnForXmlRpc glusterHooksList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHook.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterHooksListReturnForXmlRpc(response);
    }

    @Override
    public OneUuidReturnForXmlRpc glusterHostUUIDGet() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.uuid").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new OneUuidReturnForXmlRpc(response);
    }

    @Override
    public GlusterServicesReturnForXmlRpc glusterServicesList(Guid serverId, String[] serviceNames) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterService.get").withParameter("serviceNames",
                        new ArrayList<>(Arrays.asList(serviceNames))).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterServicesReturnForXmlRpc(serverId, response);
    }

    @Override
    public GlusterHookContentInfoReturnForXmlRpc glusterHookRead(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.read").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterHookContentInfoReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookUpdate(String glusterCommand,
            String stage,
            String hookName,
            String content,
            String checksum) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.update").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .withParameter("hookData", content)
                        .withParameter("hookMd5Sum", checksum)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookAdd(String glusterCommand,
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
                        .withParameter("hookMd5Sum", checksum)
                        .withParameter("enable", enabled)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterHookRemove(String glusterCommand, String stage, String hookName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHook.remove").withParameter("glusterCmd", glusterCommand)
                        .withParameter("hookLevel", stage)
                        .withParameter("hookName", hookName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterServicesReturnForXmlRpc glusterServicesAction(Guid serverId, String[] serviceList, String actionType) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterService.action").withParameter("serviceNames",
                        new ArrayList<>(Arrays.asList(serviceList)))
                        .withParameter("action", actionType)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterServicesReturnForXmlRpc(serverId, response);
    }

    @Override
    public StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID) {
        JsonRpcRequest request =
                new RequestBuilder("StoragePool.getInfo").withParameter("storagepoolID", spUUID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StoragePoolInfoReturnForXmlRpc(response);
    }

    @Override
    public GlusterTasksListReturnForXmlRpc glusterTasksList() {
        JsonRpcRequest request = new RequestBuilder("GlusterTask.list").build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterTasksListReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRebalanceStatus(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.rebalanceStatus").withParameter("volumeName", volumeName).build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeTaskReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList() {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionList").build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatusForXmlRpc(response);
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName) {
        JsonRpcRequest request = new RequestBuilder("GlusterVolume.geoRepSessionList")
                .withParameter("volumeName", volumeName)
                    .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatusForXmlRpc(response);
    }

    @Override
    public GlusterVolumeGeoRepStatusForXmlRpc glusterVolumeGeoRepSessionList(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepSessionList").withParameter("volumeName", volumeName)
                        .withParameter("remoteHost", slaveHost)
                        .withParameter("remoteVolumeName", slaveVolumeName)
                        .withOptionalParameter("remoteUserName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatusForXmlRpc(response);
    }

    @Override
    public GlusterVolumeGeoRepStatusDetailForXmlRpc glusterVolumeGeoRepSessionStatus(String volumeName,
            String slaveHost,
            String slaveVolumeName,
            String userName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.geoRepSessionStatus").withParameter("volumeName", volumeName)
                        .withParameter("remoteHost", slaveHost)
                        .withParameter("remoteVolumeName", slaveVolumeName)
                        .withOptionalParameter("remoteUserName", userName).build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeGeoRepStatusDetailForXmlRpc(response);
    }

    @Override
    public GlusterVolumeTaskReturnForXmlRpc glusterVolumeRemoveBrickStatus(String volumeName, String[] bricksList) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.removeBrickStatus").withParameter("volumeName", volumeName)
                        .withParameter("brickList", new ArrayList<>(Arrays.asList(bricksList)))
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeTaskReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc setNumberOfCpus(String vmId, String numberOfCpus) {
        JsonRpcRequest request =
                new RequestBuilder("VM.setNumberOfCpus").withParameter("vmID", vmId)
                        .withParameter("numberOfCpus", numberOfCpus)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc hotplugMemory(Map info) {
        JsonRpcRequest request =
                new RequestBuilder("VM.hotplugMemory")
                        .withParameter("vmID", getVmId(info))
                        .withParameter("params", info)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatusOnlyReturnForXmlRpc updateVmPolicy(Map params) {
        JsonRpcRequest request =
                new RequestBuilder("VM.updateVmPolicy").withParameter("vmID", (String) params.get("vmId"))
                        .withParameter("params", params)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc setHaMaintenanceMode(String mode, boolean enabled) {
        JsonRpcRequest request =
                new RequestBuilder("Host.setHaMaintenanceMode").withParameter("mode", mode)
                        .withParameter("enabled", enabled)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc add_image_ticket(String ticketId, String[] ops, long timeout,
            long size, String url) {
        HashMap<String, Object> ticketDict = new HashMap<>();
        ticketDict.put("uuid", ticketId);
        ticketDict.put("timeout", timeout);
        ticketDict.put("ops", ops);
        ticketDict.put("size", size);
        ticketDict.put("url", url);

        JsonRpcRequest request =
                new RequestBuilder("Host.add_image_ticket")
                        .withParameter("ticket", ticketDict)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc remove_image_ticket(String ticketId) {
        JsonRpcRequest request =
                new RequestBuilder("Host.remove_image_ticket")
                        .withParameter("uuid", ticketId)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);

    }

    @Override
    public StatusOnlyReturnForXmlRpc extend_image_ticket(String ticketId, long timeout) {
        JsonRpcRequest request =
                new RequestBuilder("Host.extend_image_ticket")
                        .withParameter("uuid", ticketId)
                        .withParameter("timeout", timeout)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneMapReturnForXmlRpc get_image_transfer_session_stats(String ticketId) {
        JsonRpcRequest request =
                new RequestBuilder("Host.get_image_transfer_session_stats")
                        .withParameter("ticketUUID", ticketId)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("statsMap");
        return new OneMapReturnForXmlRpc(response);
    }

    @Override
    public PrepareImageReturnForXmlRpc prepareImage(String spID, String sdID, String imageID,
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
        return new PrepareImageReturnForXmlRpc(response);
    }

    @Override
    public StatusReturnForXmlRpc teardownImage(String spID, String sdID, String imageID, String volumeID) {
        JsonRpcRequest request =
                new RequestBuilder("Image.teardown")
                        .withParameter("storagepoolID", spID)
                        .withParameter("storagedomainID", sdID)
                        .withParameter("imageID", imageID)
                        .withParameter("leafVolID", volumeID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusReturnForXmlRpc(response);
    }

    @Override
    public StatusReturnForXmlRpc verifyUntrustedVolume(String spID, String sdID, String imageID, String volumeID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.verify_untrusted")
                        .withParameter("storagepoolID", spID)
                        .withParameter("storagedomainID", sdID)
                        .withParameter("imageID", imageID)
                        .withParameter("volumeID", volumeID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusReturnForXmlRpc(response);
    }

    @Override
    public VMListReturnForXmlRpc getExternalVmList(String uri, String username, String password) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getExternalVMs")
                        .withParameter("uri", uri)
                        .withParameter("username", username)
                        .withParameter("password", password)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                        .withResponseType(Object[].class);
        return new VMListReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeSnapshotInfoReturnForXmlRpc glusterVolumeSnapshotList(Guid clusterId, String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotList").withOptionalParameter("volumeName", volumeName)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeSnapshotInfoReturnForXmlRpc(clusterId, response);
    }

    @Override
    public GlusterVolumeSnapshotConfigReturnForXmlRpc glusterSnapshotConfigList(Guid clusterId) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.configList").build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new GlusterVolumeSnapshotConfigReturnForXmlRpc(clusterId, response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotDelete(String snapshotName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.delete").withOptionalParameter("snapName", snapshotName)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSnapshotDeleteAll(String volumeName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotDeleteAll").withParameter("volumeName", volumeName)
                    .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotActivate(String snapshotName, boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.activate").withParameter("snapName", snapshotName)
                        .withParameter("force", force)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotDeactivate(String snapshotName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.deactivate").withParameter("snapName", snapshotName)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotRestore(String snapshotName) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.restore").withParameter("snapName", snapshotName)
                    .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public GlusterVolumeSnapshotCreateReturnForXmlRpc glusterVolumeSnapshotCreate(String volumeName,
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
        return new GlusterVolumeSnapshotCreateReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterVolumeSnapshotConfigSet(String volumeName, String configName, String configValue) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotConfigSet").withParameter("volumeName", volumeName)
                        .withParameter("optionName", configName)
                        .withParameter("optionValue", configValue)
                        .build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotConfigSet(String configName, String configValue) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterSnapshot.configSet").withParameter("optionName", configName)
                        .withParameter("optionValue", configValue)
                        .build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StorageDeviceListReturnForXmlRpc glusterStorageDeviceList() {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.storageDevicesList").build();
        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StorageDeviceListReturnForXmlRpc(response);
    }

    @Override
    public OneStorageDeviceReturnForXmlRpc glusterCreateBrick(String lvName,
            String mountPoint,
            Map<String, Object> raidParams, String fsType,
            String[] storageDevices) {
        JsonRpcRequest request = new RequestBuilder("GlusterHost.createBrick").withParameter("name", lvName)
                .withParameter("mountPoint", mountPoint)
                .withParameter("devList", storageDevices)
                .withParameter("fsType", fsType)
                .withOptionalParameterAsMap("raidParams", raidParams).build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new OneStorageDeviceReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc hostdevChangeNumvfs(String deviceName, int numOfVfs) {
        JsonRpcRequest request =
                new RequestBuilder("Host.hostdevChangeNumvfs").withParameter("deviceName", deviceName)
                        .withParameter("numvfs", numOfVfs)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc convertVmFromExternalSystem(String uri,
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
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc convertVmFromOva(String ovaPath, Map<String, Object> vm, String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.convertExternalVmFromOva")
        .withParameter("ova_path", ovaPath)
        .withParameter("vminfo", vm)
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OvfReturnForXmlRpc getConvertedVm(String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getConvertedVm")
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request)
        .withResponseKey("ovf")
        .withResponseType(String.class);
        return new OvfReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc deleteV2VJob(String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.deleteV2VJob")
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc abortV2VJob(String jobUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Host.abortV2VJob")
        .withParameter("jobid", jobUUID)
        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotScheduleOverride(boolean force) {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotScheduleOverride").withParameter("force", force).build();

        Map<String, Object> response = new FutureMap(this.client, request).withIgnoreResponseKey();
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterSnapshotScheduleReset() {
        JsonRpcRequest request =
                new RequestBuilder("GlusterVolume.snapshotScheduleReset").build();

        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    public StatusOnlyReturnForXmlRpc registerSecrets(Map<String, String>[] libvirtSecrets, boolean clearUnusedSecrets) {
        JsonRpcRequest request =
                new RequestBuilder("Host.registerSecrets").withParameter("secrets", libvirtSecrets)
                        .withParameter("clear", clearUnusedSecrets)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc unregisterSecrets(String[] libvirtSecretsUuids) {
        JsonRpcRequest request =
                new RequestBuilder("Host.unregisterSecrets").withParameter("uuids", libvirtSecretsUuids)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc freeze(String vmId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.freeze").withParameter("vmID", vmId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc thaw(String vmId) {
        JsonRpcRequest request =
                new RequestBuilder("VM.thaw").withParameter("vmID", vmId)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc isolateVolume(String sdUUID, String srcImageID, String dstImageID, String volumeID) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.isolateVolume")
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("srcImageID", srcImageID)
                        .withParameter("dstImageID", dstImageID)
                        .withParameter("volumeID", volumeID).build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc wipeVolume(String sdUUID, String imgUUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("SDM.wipeVolume")
                 .withParameter("storagedomainID", sdUUID)
                .withParameter("imageID", imgUUID)
                .withParameter("volumeID", volUUID)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public OneVmReturnForXmlRpc getExternalVmFromOva(String ovaPath) {
        JsonRpcRequest request =
                new RequestBuilder("Host.getExternalVmFromOva")
                        .withParameter("ova_path", ovaPath)
                        .build();
        Map<String, Object> response =
                new FutureMap(this.client, request).withResponseKey("vmList")
                .withResponseType(Object[].class);
        return new OneVmReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc refreshVolume(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.refresh")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgUUID)
                        .withParameter("volumeID", volUUID)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }

    @Override
    public VolumeInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgUUID, String volUUID) {
        JsonRpcRequest request =
                new RequestBuilder("Volume.getInfo")
                        .withParameter("storagepoolID", spUUID)
                        .withParameter("storagedomainID", sdUUID)
                        .withParameter("imageID", imgUUID)
                        .withParameter("volumeID", volUUID)
                        .build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new VolumeInfoReturnForXmlRpc(response);
    }

    @Override
    public StatusOnlyReturnForXmlRpc glusterStopProcesses() {
        JsonRpcRequest request =
                new RequestBuilder("GlusterHost.processesStop").build();
        Map<String, Object> response = new FutureMap(this.client, request);
        return new StatusOnlyReturnForXmlRpc(response);
    }
}
