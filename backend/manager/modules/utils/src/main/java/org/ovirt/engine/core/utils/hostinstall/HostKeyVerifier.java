package org.ovirt.engine.core.utils.hostinstall;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PublicKey;

    import org.apache.sshd.ClientSession;
import org.apache.sshd.client.ServerKeyVerifier;
import org.ovirt.engine.core.engineencryptutils.OpenSSHUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class HostKeyVerifier implements ServerKeyVerifier {
    // The log:
    private static final Log log = LogFactory.getLog(HostKeyVerifier.class);

    // This is a singleton:
    public static final ServerKeyVerifier INSTANCE = new HostKeyVerifier();

    // Ge save the fingerprint for later use:
    private String serverKeyFingerprint;

    @Override
    public boolean verifyServerKey(ClientSession sshClientSession, SocketAddress remoteAddress, PublicKey serverKey) {
        // We only support internet addresses (this is not an additional
        // limitation, it is just convenient to use the internet address class
        // to be able to display the host name and IP address in log messages):
        InetSocketAddress inetAddress = null;
        try {
            inetAddress = (InetSocketAddress) remoteAddress;
        }
        catch (ClassCastException exception) {
            log.error("Strange, the remote address is not an internet address.", exception);
            return false;
        }

        // Check that we can calculate a fingerprint from the given key:
        serverKeyFingerprint = OpenSSHUtils.getKeyFingerprintString(serverKey);
        if (serverKeyFingerprint == null) {
            log.error("SSH key fingerprint for host " + inetAddress.getHostName() + " (" + inetAddress.getAddress().getHostAddress() + ") can't be verified.");
            return false;
        }

        // If we are here all the validations succeeded:
        log.info("SSH key fingerprint " + serverKeyFingerprint + " for host " + inetAddress.getHostName() + " (" + inetAddress.getAddress().getHostAddress() + ") has been successfully verified.");
        return true;
    }

    public String getServerFingerprint() {
        return serverKeyFingerprint;
    }
}
