package org.ovirt.engine.core.bll.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.AuthenticationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.uutils.ssh.ConstraintByteArrayOutputStream;
import org.ovirt.engine.core.uutils.ssh.SSHClient;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Singleton
public class GlusterUtil {
    private static final Logger log = LoggerFactory.getLogger(GlusterUtil.class);
    private static final String PEER = "peer";
    private static final String HOST_NAME = "hostname";
    private static final String STATE = "state";
    private static final int PEER_IN_CLUSTER = 3;
    private static final String FEATURES_SHARD = "features.shard";

    private static final String PERFORMANCE_STRICT_O_DIRECT = "performance.strict-o-direct";

    private static final String REMOTE_DIO = "remote-dio";

    private static final String NETWORK_REMOTE_DIO = "network.remote-dio";

    @Inject
    private ResourceManager resourceManager;
    @Inject
    private VdsDao vdsDao;

    @Inject
    private GlusterServerDao glusterServerDao;

    @Inject
    private AuditLogDao auditLogDao;

    @Inject
    private BackendInternal backend;

    @Inject
    private GlusterAuditLogUtil glusterAuditLogUtil;

    @Inject
    private GlusterDBUtils glusterDBUtils;

    @Inject
    private AlertDirector alertDirector;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private ClusterDao clusterDao;

    /**
     * Returns a server that is in {@link VDSStatus#Up} status.<br>
     * This server is chosen randomly from all the Up servers.
     *
     * @return One of the servers in up status
     */
    public VDS getRandomUpServer(Guid clusterId) {
        List<VDS> servers = getAllUpServers(clusterId);
        if (CollectionUtils.isEmpty(servers)) {
            return null;
        }
        return servers.get(new Random().nextInt(servers.size()));
    }

    /**
     * Returns a server that is in {@link VDSStatus#Up} status.<br>
     * This server is returned as first from list of the Up servers.
     *
     * @return One of the servers in up status
     */
    public VDS getUpServer(Guid clusterId) {
        List<VDS> servers = getAllUpServers(clusterId);
        if (CollectionUtils.isEmpty(servers)) {
            return null;
        }
        return servers.get(0);
    }

    public List<VDS> getAllUpServers(Guid clusterId) {
        return vdsDao.getAllForClusterWithStatusAndPeerStatus(clusterId, VDSStatus.Up, PeerStatus.CONNECTED);
    }



    /**
     * Fetches gluster peers of the given server
     *
     * @param server
     *            Server whose peers are to be fetched
     * @param password
     *            Root password of the server
     * @return Set of peers of the server
     * @throws AuthenticationException
     *             If SSH authentication with given root password fails
     */
    public Set<String> getPeers(String server, String username, String password) throws AuthenticationException,
            IOException {

        try (final SSHClient client = getSSHClient()) {
            connect(client, server, username, password);
            authenticate(client);
            String serversXml = executePeerStatusCommand(client);
            return extractServers(serversXml);
        }
    }

    /**
     * Given an SSHClient (already connected and authenticated), execute the "gluster peer status" command, and return
     * the set of the peers returned by the command. Note that this method does <b>not</b> close the connection, and it
     * is the responsibility of the calling code to do the same.
     *
     * @param client
     *            The already connected and authenticated SSHClient object
     * @return Set of peers of the server
     */
    public Set<String> getPeers(SSHClient client) {
        String serversXml = executePeerStatusCommand(client);
        return extractServers(serversXml);
    }

    /**
     * Fetches gluster peers of the given server
     *
     * @param server
     *            Server whose peers are to be fetched
     * @param username
     *            Privilege username to authenticate with server
     * @param password
     *            password of the server
     * @return Map of peers of the server with key = peer name and value = SSH public key of the peer
     * @throws AuthenticationException
     *             If SSH authentication with given root password fails
     */
    public Map<String, String> getPeersWithSshPublicKeys(String server, String username, String password)
            throws AuthenticationException, IOException {
        try (final SSHClient client = getSSHClient()) {
            connect(client, server, username, password);
            authenticate(client);
            String serversXml = executePeerStatusCommand(client);
            return getPublicKeys(extractServers(serversXml));
        }
    }

    protected void connect(SSHClient client, String serverName, String userId, String password) {
        Integer timeout = Config.<Integer> getValue(ConfigValues.ConnectToServerTimeoutInSeconds) * 1000;
        client.setHardTimeout(timeout);
        client.setSoftTimeout(timeout);
        client.setHost(serverName);
        client.setUser(userId);
        client.setPassword(password);
        try {
            client.connect();
        } catch (Exception e) {
            log.debug("Could not connect to server '{}': {}", serverName, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getIpAddressForHost(Guid vdsId, String networkName) {
        Optional<VdsNetworkInterface> nic =
                getGlusterIpaddressUtil(vdsId).stream().filter(v -> v.getNetworkName().equals(networkName)).findFirst();
        if (nic.isEmpty()) {
            return null;
        }
        return !StringUtils.isBlank(nic.get().getIpv4Address()) ?
                nic.get().getIpv4Address() :
                !StringUtils.isBlank(nic.get().getIpv6Address()) ?
                        nic.get().getIpv6Address() : null;
    }

    public Map<VDS, String> getGlusterIpaddressAsMap(Map<String, Network> networkNameObjectMap,
                                                     VDS currentHostVds, List<VDS> hostlist) {

        Map<VDS, String> hostGlusterIpMap = new HashMap<>();
        String networkName = StringUtils.EMPTY;

        Optional<Map.Entry<String, Network>> result =
              networkNameObjectMap.entrySet().stream().filter(v -> v.getValue().getCluster().isGluster()).findFirst();

        if(result.isPresent()) {
            networkName = result.get().getKey();

        } else {
          return null;
        }
        hostGlusterIpMap.put(currentHostVds, getIpAddressForHost(currentHostVds.getId(),
                networkName));
        hostGlusterIpMap.put(hostlist.get(0), getIpAddressForHost(hostlist.get(0).getId(),
                networkName));
        hostGlusterIpMap.put(hostlist.get(1), getIpAddressForHost(hostlist.get(1).getId(),
                networkName));

        Optional<Map.Entry<VDS, String>> hostGlusterIpresult =
                hostGlusterIpMap.entrySet().stream().filter(x -> x.getValue().equals(null)).findAny();

        if(hostGlusterIpresult.isPresent()) {
            return null;
        }
        return hostGlusterIpMap;
    }

    public List<VdsNetworkInterface> getGlusterIpaddressUtil(Guid hostId){
        return interfaceDao.getAllInterfacesForVds(hostId);
    }

    protected void authenticate(SSHClient client) throws AuthenticationException {
        try {
            client.authenticate();
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during authentication: {}", e.getMessage());
            log.debug("Exception", e);
            throw new RuntimeException(e);
        }
    }

    protected String executePeerStatusCommand(SSHClient client) {
        ByteArrayOutputStream out = new ConstraintByteArrayOutputStream(500);
        String command = Config.getValue(ConfigValues.GlusterPeerStatusCommand);
        try {
            client.executeCommand(command, null, out, null);
            return new String(out.toByteArray(), "UTF-8");
        } catch (Exception e) {
            log.error("Error while executing command '{}' on server '{}': {}",
                    command,
                    client.getHost(),
                    e.getMessage());
            log.debug("Exception", e);
            throw new RuntimeException(e);
        }
    }

    private Set<String> getServers(NodeList listOfPeers) {
        Set<String> servers = new HashSet<>();
        for (int i = 0; i < listOfPeers.getLength(); i++) {
            Node firstPeer = listOfPeers.item(i);
            if (firstPeer.getNodeType() == Node.ELEMENT_NODE) {
                Element firstHostElement = (Element) firstPeer;
                int state = XmlUtils.getIntValue(firstHostElement, STATE);
                // Add the server only if the state is 3
                if (state == PEER_IN_CLUSTER) {
                    servers.add(XmlUtils.getTextValue(firstHostElement, HOST_NAME));
                }
            }
        }

        return servers;
    }

    protected Map<String, String> getPublicKeys(Set<String> servers) {
        QueryReturnValue publicKeyReturnValue;
        Map<String, String> publicKeys = new HashMap<>();
        for (String server : servers) {
            publicKeyReturnValue = backend.runInternalQuery(QueryType.GetServerSSHPublicKey,
                    new ServerParameters(server));

            String publicKey = null;
            if (publicKeyReturnValue != null && publicKeyReturnValue.getSucceeded()
                    && publicKeyReturnValue.getReturnValue() != null) {
                publicKey = publicKeyReturnValue.getReturnValue().toString();
            }
            publicKeys.put(server, publicKey != null ? publicKey : "");

        }
        return publicKeys;
    }

    protected Set<String> extractServers(String serversXml) {
        if (StringUtils.isEmpty(serversXml)) {
            throw new RuntimeException("Could not get the peer list!");
        }

        try {
            return getServers(XmlUtils.loadXmlDoc(serversXml).getElementsByTagName(PEER));
        } catch (Exception e) {
            log.error("Error while parsing peer list xml [{}]: {}", serversXml, e.getMessage());
            log.debug("Exception", e);
            throw new RuntimeException(e);
        }
    }

    protected SSHClient getSSHClient() {
        return new EngineSSHClient();
    }

    public EngineLock acquireGlusterLockWait(Guid clusterId) {
        Map<String, Pair<String, String>> exclusiveLocks = new HashMap<>();
        exclusiveLocks.put(clusterId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER,
                        EngineMessage.ACTION_TYPE_FAILED_GLUSTER_OPERATION_INPROGRESS));
        EngineLock lock = new EngineLock(exclusiveLocks, null);
        Injector.get(LockManager.class).acquireLockWait(lock);
        return lock;
    }

    public boolean isHostExists(List<GlusterServerInfo> glusterServers, VDS server) {
        GlusterServer glusterServer = glusterServerDao.getByServerId(server.getId());
        if (glusterServer != null) {
            for (GlusterServerInfo glusterServerInfo : glusterServers) {
                if (glusterServerInfo.getUuid().equals(glusterServer.getGlusterServerUuid())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isVolumeThinlyProvisioned(GlusterVolumeEntity volume) {
        // TODO: As part of disk provisioning feature in oVirt for gluster, a flag would be maintained
        // as part Gluster Volume Entity which depicts if the volume bricks are thinly provisioned or not.
        // The same flag would be used here to decide accordingly later.
        return true;
    }

    public String getCronExpression(GlusterVolumeSnapshotSchedule schedule) {
        String retStr = "";

        switch (schedule.getRecurrence()) {
        case INTERVAL:
            int interval = schedule.getInterval();
            retStr = String.format("0 */%s * * * ? *", interval);
            break;
        case HOURLY:
            retStr = "0 0 0/1 1/1 * ? *";
            break;
        case DAILY:
            Time execTime = schedule.getExecutionTime();
            retStr = String.format("0 %s %s * * ? *", execTime.getMinutes(), execTime.getHours());
            break;
        case WEEKLY:
            String days = schedule.getDays();
            Time execTime1 = schedule.getExecutionTime();
            retStr = String.format("0 %s %s ? * %s *", execTime1.getMinutes(), execTime1.getHours(), days);
            break;
        case MONTHLY:
            String days1 = schedule.getDays();
            Time execTime2 = schedule.getExecutionTime();
            retStr = String.format("0 %s %s %s * ? *", execTime2.getMinutes(), execTime2.getHours(), days1);
            break;
        case UNKNOWN:
            return null;
        }

        return retStr;
    }

    /**
     * Converts the input time to engine's time zone from the provided time zone
     * @param inTime input time
     * @param fromTimeZone time zone from which to convert to engine time zone
     * @return converted time
     */
    public Time convertTime(Time inTime, String fromTimeZone) {
        Calendar calFrom = new GregorianCalendar(TimeZone.getTimeZone(fromTimeZone));
        calFrom.set(Calendar.HOUR_OF_DAY, inTime.getHours());
        calFrom.set(Calendar.MINUTE, inTime.getMinutes());
        calFrom.set(Calendar.SECOND, inTime.getSeconds());

        Calendar calTo = new GregorianCalendar();
        calTo.setTimeInMillis(calFrom.getTimeInMillis());

        return new Time(calTo.get(Calendar.HOUR_OF_DAY), calTo.get(Calendar.MINUTE), calTo.get(Calendar.SECOND));
    }

    private boolean alertVolumeLimitReached(final GlusterVolumeEntity volume, boolean checkHardLimit) {
        AuditLogType logType =
                checkHardLimit ? AuditLogType.GLUSTER_VOLUME_SNAPSHOT_HARD_LIMIT_REACHED
                        : AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SOFT_LIMIT_REACHED;

        List<AuditLog> limitAlerts = auditLogDao.getByVolumeIdAndType(volume.getId(), logType.getValue());
        if (!limitAlerts.isEmpty()) {
            for (AuditLog alert : limitAlerts) {
                if (!alert.isDeleted()) {
                    return true;
                }
            }
        }

        // Alert
        boolean limitReached =
                checkHardLimit ? glusterDBUtils.isVolumeSnapshotHardLimitReached(volume.getId())
                        : glusterDBUtils.isVolumeSnapshotSoftLimitReached(volume.getId());
        if (limitReached) {
            glusterAuditLogUtil.logAuditMessage(volume.getClusterId(),
                    volume.getClusterName(),
                    volume,
                    null,
                    logType,
                    volumeAsMap(volume));
            return true;
        }

        return false;
    }

    private static Map<String, String> volumeAsMap(GlusterVolumeEntity volume) {
        Map<String, String> map = new HashMap<>();
        map.put(GlusterConstants.VOLUME_NAME, volume.getName());
        map.put(GlusterConstants.CLUSTER, volume.getClusterName());
        return map;
    }

    public void alertVolumeSnapshotLimitsReached(final GlusterVolumeEntity volume) {
        if (!alertVolumeLimitReached(volume, true)) {
            alertVolumeLimitReached(volume, false);
        }
    }

    public void checkAndRemoveVolumeSnapshotLimitsAlert(final GlusterVolumeEntity volume) {
        if (!glusterDBUtils.isVolumeSnapshotSoftLimitReached(volume.getId())) {
            alertDirector.removeVolumeAlert(volume.getId(), AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SOFT_LIMIT_REACHED);
        }

        if (!glusterDBUtils.isVolumeSnapshotHardLimitReached(volume.getId())) {
            alertDirector.removeVolumeAlert(volume.getId(), AuditLogType.GLUSTER_VOLUME_SNAPSHOT_HARD_LIMIT_REACHED);
        }
    }

    /**
     * Converts the given date from the given time zone to engine time zone
     * @param inDate input date
     * @param tZone from time zone
     * @return converted date
     */
    public Date convertDate(Date inDate, String tZone) {
        if (inDate == null) {
            return null;
        }

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String formattedStartDate = format.format(inDate);

        format.setTimeZone(TimeZone.getTimeZone(tZone));
        try {
            return format.parse(formattedStartDate);
        } catch (Exception ex) {
            log.error("Error while converting the date to engine time zone");
            return null;
        }
    }

    /**
     * Returns GlusterStatus for checking id vdo service is running
     *
     * @param vdsId
     *            input Guid
     * @return GlusterStatus
     */
    public GlusterStatus isVDORunning(Guid vdsId) {
        VDSReturnValue returnValue = resourceManager.runVdsCommand(VDSCommandType.ManageGlusterService,
                new GlusterServiceVDSParameters(vdsId, Arrays.asList("vdo"), "status"));
        if (!returnValue.getSucceeded()) {
            log.error("Failed to check VDO running status of host : '{}' ", vdsId);
            return GlusterStatus.UNKNOWN;
        }
        ArrayList<GlusterServerService> returnList = (ArrayList<GlusterServerService>) returnValue.getReturnValue();
        if (returnList == null || returnList.isEmpty()) {
            log.error("Failed to check VDO running status of host : '{}' returned empty object", vdsId);
            return GlusterStatus.UNKNOWN;
        }
        GlusterServerService gss = returnList.get(0);
        if (gss != null) {
            return gss.getStatus().getStatusMsg().equalsIgnoreCase("Up") ? GlusterStatus.UP : GlusterStatus.DOWN;
        }
        log.info("Failed to check VDO running status of host : '{}' ", vdsId);
        return GlusterStatus.UNKNOWN;
    }

    /**
     * Returns GlusterStatus for checking id gluster service is running
     *
     * @param vdsId
     *            input Guid
     * @return GlusterStatus
     */
    public GlusterStatus isGlusterRunning(Guid vdsId) {
        VDSReturnValue result = resourceManager.runVdsCommand(VDSCommandType.ManageGlusterService,
                new GlusterServiceVDSParameters(vdsId, Arrays.asList("glusterd"), "status"));

        if (result.getSucceeded()) {
            @SuppressWarnings("unchecked")
            ArrayList<GlusterServerService> serviceList = (ArrayList<GlusterServerService>) result.getReturnValue();
            if (serviceList != null && !serviceList.isEmpty()) {
                return serviceList.get(0).getStatus().getStatusMsg().equalsIgnoreCase("up")
                        ? GlusterStatus.UP
                        : GlusterStatus.DOWN;
            }
        }
        return GlusterStatus.UNKNOWN;
    }

    public EngineMessage validateVolumeForStorageDomain(GlusterVolumeEntity glusterVolume) {
        if (glusterVolume.getOptions() != null && !glusterVolume.getOptions().isEmpty()) {
            String[] options = new String[]{"1", "on", "yes", "true", "enable"};
            if (glusterVolume.getOption(PERFORMANCE_STRICT_O_DIRECT) == null ||
                    !Arrays.stream(options).anyMatch(glusterVolume.getOption(PERFORMANCE_STRICT_O_DIRECT).getValue()::equals)) {
                return EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_PERFORMANCE_O_DIRECT_DISABLE;
            }

            if (glusterVolume.getOption(FEATURES_SHARD) == null
                    || !Arrays.stream(options).anyMatch(glusterVolume.getOption(FEATURES_SHARD).getValue()::equals)) {
                return EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_SHARDING_DISABLE;
            }

            if (glusterVolume.getOption(REMOTE_DIO) == null
                    && glusterVolume.getOption(NETWORK_REMOTE_DIO) == null) {
                return EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_REMOTE_DIO_ON;
            } else {
                String remoteDioOption =
                        glusterVolume.getOption(REMOTE_DIO) == null ? glusterVolume.getOption(NETWORK_REMOTE_DIO).getValue()
                                : glusterVolume.getOption(REMOTE_DIO).getValue();
                if (Arrays.stream(options).anyMatch(remoteDioOption::equals)) {
                    return EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_REMOTE_DIO_ON;
                }

            }
        } else {
                return EngineMessage.ACTION_TYPE_FAILED_INVALID_GLUSTER_OPTIONS;
        }
        return null;
    }

    public GlusterVolumeEntity getGlusterVolInfoFromVolumePath(String volumePath) {
        String[] pathElements = volumePath.split(StorageConstants.GLUSTER_VOL_SEPARATOR);
        if (pathElements.length != 2) {
            // return empty as volume name could not be determined
            log.warn("Volume name could not be determined from storage connection '{}' ", volumePath);
            return null;
        }
        String volumeName = pathElements[1];
        String hostName = pathElements[0];
        String hostAddress;

        hostAddress = resolveHostName(hostName);
        if (hostAddress == null) {
            log.warn("Host name could not be determined from storage connection '{}' ", hostName);
            return null;
        }
        Guid clusterId = clusterDao.getClusterIdForHostByNameOrAddress(hostName, hostAddress);
        if (Guid.isNullOrEmpty(clusterId)) {
            return null;
        }
        return glusterDBUtils.getGlusterVolInfoByClusterIdAndVolName(clusterId, volumeName);
    }

    protected String resolveHostName(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
