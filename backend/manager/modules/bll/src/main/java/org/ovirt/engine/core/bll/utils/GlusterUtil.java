package org.ovirt.engine.core.bll.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.naming.AuthenticationException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.uutils.ssh.ConstraintByteArrayOutputStream;
import org.ovirt.engine.core.uutils.ssh.SSHClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GlusterUtil {
    private static GlusterUtil instance = new GlusterUtil();
    private Log log = LogFactory.getLog(getClass());
    private static final int SSH_PORT = 22;
    private static final String PEER = "peer";
    private static final String HOST_NAME = "hostname";
    private static final String STATE = "state";
    private static final int PEER_IN_CLUSTER = 3;

    private GlusterUtil() {

    }

    public static GlusterUtil getInstance() {
        return instance;
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
            connect(client, server);
            authenticate(client, username, password);
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
     * @param fingerprint
     *            pre-approved fingerprint of the server. This is validated against the server before attempting
     *            authentication using the root password.
     * @return Map of peers of the server with key = peer name and value = SSH fingerprint of the peer
     * @throws AuthenticationException
     *             If SSH authentication with given root password fails
     */
    public Map<String, String> getPeers(String server, String username, String password, String fingerprint)
            throws AuthenticationException, IOException {
        try (final SSHClient client = getSSHClient()) {
            connect(client, server);
            authenticate(client, username, password);
            String serversXml = executePeerStatusCommand(client);
            return getFingerprints(extractServers(serversXml));
        }
    }

    protected void connect(SSHClient client, String serverName) {
        Integer timeout = Config.<Integer> getValue(ConfigValues.ConnectToServerTimeoutInSeconds) * 1000;
        client.setHardTimeout(timeout);
        client.setSoftTimeout(timeout);
        client.setHost(serverName, SSH_PORT);
        try {
            client.connect();
        } catch (Exception e) {
            log.debug(String.format("Could not connect to server %1$s: %2$s", serverName, e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    protected void authenticate(SSHClient client, String userId, String password) throws AuthenticationException {
        client.setUser(userId);
        client.setPassword(password);
        try {
            client.authenticate();
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.errorFormat("Exception during authentication!", e);
            throw new RuntimeException(e);
        }
    }

    protected String executePeerStatusCommand(SSHClient client) {
        ByteArrayOutputStream out = new ConstraintByteArrayOutputStream(500);
        String command = Config.<String> getValue(ConfigValues.GlusterPeerStatusCommand);
        try {
            client.executeCommand(command, null, out, null);
            return new String(out.toByteArray(), "UTF-8");
        } catch (Exception e) {
            log.errorFormat("Error while executing command {0} on server {1}!", command, client.getHost(), e);
            throw new RuntimeException(e);
        }
    }

    private Set<String> getServers(NodeList listOfPeers) {
        Set<String> servers = new HashSet<String>();
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

    protected Map<String, String> getFingerprints(Set<String> servers) {
        VdcQueryReturnValue returnValue;
        Map<String, String> fingerprints = new HashMap<String, String>();
        for (String server : servers) {
            returnValue = getBackendInstance().
                    runInternalQuery(VdcQueryType.GetServerSSHKeyFingerprint,
                            new ServerParameters(server), null);
            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null) {
                fingerprints.put(server, returnValue.getReturnValue().toString());
            } else {
                fingerprints.put(server, null);
            }
        }
        return fingerprints;
    }

    protected Set<String> extractServers(String serversXml) {
        if (StringUtils.isEmpty(serversXml)) {
            throw new RuntimeException("Could not get the peer list!");
        }

        try {
            return getServers(XmlUtils.loadXmlDoc(serversXml).getElementsByTagName(PEER));
        } catch (Exception e) {
            log.errorFormat("Error while parsing peer list xml [{0}]!", serversXml, e);
            throw new RuntimeException(e);
        }
    }

    public BackendInternal getBackendInstance() {
        return Backend.getInstance();
    }

    protected SSHClient getSSHClient() {
        return new EngineSSHClient();
    }

    public EngineLock acquireGlusterLockWait(Guid clusterId) {
        Map<String, Pair<String, String>> exclusiveLocks = new HashMap<String, Pair<String, String>>();
        exclusiveLocks.put(clusterId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER,
                        VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_OPERATION_INPROGRESS));
        EngineLock lock = new EngineLock(exclusiveLocks, null);
        LockManagerFactory.getLockManager().acquireLockWait(lock);
        return lock;
    }

    public boolean isHostExists(List<GlusterServerInfo> glusterServers, VDS server) {
        if (GlusterFeatureSupported.glusterHostUuidSupported(server.getVdsGroupCompatibilityVersion())) {
            GlusterServer glusterServer = DbFacade.getInstance().getGlusterServerDao().getByServerId(server.getId());
            if (glusterServer != null) {
                for (GlusterServerInfo glusterServerInfo : glusterServers) {
                    if (glusterServerInfo.getUuid().equals(glusterServer.getGlusterServerUuid())) {
                        return true;
                    }
                }
            }
        }
        else {
            for (GlusterServerInfo glusterServer : glusterServers) {
                if (glusterServer.getHostnameOrIp().equals(server.getHostName())) {
                    return true;
                }
                try {
                    String glusterHostAddr = InetAddress.getByName(glusterServer.getHostnameOrIp()).getHostAddress();
                    for (VdsNetworkInterface vdsNwInterface : getVdsInterfaces(server.getId())) {
                        if (glusterHostAddr.equals(vdsNwInterface.getAddress())) {
                            return true;
                        }
                    }
                } catch (UnknownHostException e) {
                    log.errorFormat("Could not resolve IP address of the host {0}. Error: {1}",
                            glusterServer.getHostnameOrIp(),
                            e.getMessage());
                }
            }
        }
        return false;
    }

    private List<VdsNetworkInterface> getVdsInterfaces(Guid vdsId) {
        List<VdsNetworkInterface> interfaces = DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(vdsId);
        return (interfaces == null) ? new ArrayList<VdsNetworkInterface>() : interfaces;
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

    public Time convertTime(Time inTime, String fromTimeZone) {
        Calendar calFrom = new GregorianCalendar(TimeZone.getTimeZone(fromTimeZone));
        calFrom.set(Calendar.HOUR, inTime.getHours());
        calFrom.set(Calendar.MINUTE, inTime.getMinutes());
        calFrom.set(Calendar.SECOND, inTime.getSeconds());

        Calendar calTo = new GregorianCalendar();
        calTo.setTimeInMillis(calFrom.getTimeInMillis());

        return new Time(calTo.get(Calendar.HOUR_OF_DAY), calTo.get(Calendar.MINUTE), calTo.get(Calendar.SECOND));
    }
}
