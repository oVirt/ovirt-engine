package org.ovirt.engine.core.utils.gluster;

import java.io.ByteArrayOutputStream;
import java.security.AccessControlException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.utils.crypt.OpenSSHUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ssh.ConstraintByteArrayOutputStream;
import org.ovirt.engine.core.utils.ssh.SSHClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GlusterUtil {
    private static GlusterUtil instance = new GlusterUtil();
    private Log log = LogFactory.getLog(getClass());
    private static final int SSH_PORT = 22;
    private static final String USER = "root";
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
    public Set<String> getPeers(String server, String password) throws AuthenticationException {
        SSHClient client = null;

        try {
            client = connect(server);
            authenticate(client, USER, password);
            String serversXml = executePeerStatusCommand(client);
            return extractServers(serversXml);
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }
    }

    /**
     * Fetches gluster peers of the given server
     *
     * @param server
     *            Server whose peers are to be fetched
     * @param rootPassword
     *            Root password of the server
     * @param fingerprint
     *            pre-approved fingerprint of the server. This is validated against the server before attempting
     *            authentication using the root password.
     * @return Map of peers of the server with key = peer name and value = SSH fingerprint of the peer
     * @throws AuthenticationException
     *             If SSH authentication with given root password fails
     */
    public Map<String, String> getPeers(String server, String rootPassword, String fingerprint)
            throws AuthenticationException {
        SSHClient client = null;

        try {
            client = connect(server);
            validateFingerprint(client, fingerprint);
            authenticate(client, USER, rootPassword);
            String serversXml = executePeerStatusCommand(client);
            return getFingerprints(extractServers(serversXml));
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }
    }

    protected SSHClient connect(String serverName) {
        SSHClient client = new SSHClient();
        Integer timeout = Config.<Integer> GetValue(ConfigValues.ConnectToServerTimeoutInSeconds) * 1000;
        client.setHardTimeout(timeout);
        client.setSoftTimeout(timeout);
        client.setHost(serverName, SSH_PORT);
        try {
            client.connect();
            return client;
        } catch (Exception e) {
            log.debug(String.format("Could not connect to server %1$s: %2$s", serverName, e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    protected void validateFingerprint(SSHClient client, String fingerprint) {
        if (!fingerprint.equals(getFingerprint(client))) {
            throw new AccessControlException(
                    String.format(
                            "SSH Fingerprint of server '%1$s' did not match expected fingerprint '%2$s'",
                            client.getDisplayHost(),
                            fingerprint
                            ));
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
        String command = Config.<String> GetValue(ConfigValues.GlusterPeerStatusCommand);
        try {
            client.executeCommand(command, null, out, null);
            return new String(out.toByteArray(), "UTF-8");
        } catch (Exception e) {
            log.errorFormat("Error while executing command {0} on server {1}!", command, client.getHost(), e);
            throw new RuntimeException(e);
        }
    }

    protected String getFingerprint(SSHClient client) {
        PublicKey hostKey = client.getHostKey();
        if (hostKey == null) {
            log.error("Could not get server key");
            return null;
        }

        return OpenSSHUtils.getKeyFingerprintString(hostKey);
    }

    public String getFingerprint(String hostName) {
        SSHClient client = null;
        try {
            client = connect(hostName);
            return getFingerprint(client);
        } finally {
            if (client != null) {
                client.disconnect();
            }
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

    private Map<String, String> getFingerprints(Set<String> servers) {
        Map<String, String> fingerprints = new HashMap<String, String>();
        for (String server : servers) {
            fingerprints.put(server, getFingerprint(server));
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
}
