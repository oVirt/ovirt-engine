package org.ovirt.engine.core.uutils.ssh;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.TimeLimitExceededException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.session.ClientSession.ClientSessionEvent;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.kex.extension.DefaultClientKexExtensionHandler;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.core.CoreModuleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHClient implements Closeable {
    private static final int CONSTRAINT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_SSH_PORT = 22;

    private static final Logger log = LoggerFactory.getLogger(SSHClient.class);
    private static final Duration HEARTBEAT = Duration.ofSeconds(2L);

    private SshClient client;
    private ClientSession session;
    private long softTimeout = 10000;
    private long hardTimeout = 0;
    private String user;
    private String password;
    private KeyPair keyPair;
    private String host;
    private int port = DEFAULT_SSH_PORT;
    private ServerKeyVerifier serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE;
    private final List<NamedFactory<Signature>> expectedSignatures = new ArrayList<>();

    /**
     * Create the client for testing using org.mockito.Mockito.
     *
     * @return client.
     */
    SshClient createSshClient() {
        SshClient sshClient = SshClient.setUpDefaultClient();

        if (isAtLeastOneExpectedSignatureSet()) {
            sshClient.setSignatureFactories(expectedSignatures);
        }

        // Engine uses RSA based SSH keys, so we always need to allow ssh-rsa2 public keys for authentication
        // against FIPS enabled host
        if (isKeyPairSet()) {
            UserAuthPublicKeyFactory clientPubKeyFactory = new UserAuthPublicKeyFactory();
            clientPubKeyFactory.setSignatureFactories(
                    Arrays.asList(
                            BuiltinSignatures.rsaSHA512,
                            BuiltinSignatures.rsaSHA256,
                            BuiltinSignatures.rsa));
            sshClient.setUserAuthFactories(Collections.singletonList(clientPubKeyFactory));
        }

        sshClient.setKexExtensionHandler(new DefaultClientKexExtensionHandler());
        CoreModuleProperties.HEARTBEAT_INTERVAL.set(sshClient, HEARTBEAT);
        return sshClient;
    }

    /**
     * Set soft timeout.
     *
     * @param softTimeout
     *            timeout for network activity.
     *
     *            default is 10 seconds.
     */
    public void setSoftTimeout(long softTimeout) {
        this.softTimeout = softTimeout;
    }

    /**
     * Set hard timeout.
     *
     * @param hardTimeout
     *            timeout for the entire transaction.
     *
     *            timeout of 0 is infinite.
     *
     *            The timeout is evaluate at softTimeout intervals.
     */
    public void setHardTimeout(long hardTimeout) {
        this.hardTimeout = hardTimeout;
    }

    /**
     * Set user.
     *
     * @param user
     *            user.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Set password.
     *
     * @param password
     *            password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set keypair.
     *
     * @param keyPair
     *            key pair.
     */
    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /**
     * Set host.
     *
     * @param host
     *            host.
     * @param port
     *            port.
     */
    public void setHost(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Set host.
     *
     * @param host
     *            host.
     * @param port
     *            port.
     */
    public void setHost(String host, Integer port) {
        setHost(host, port == null ? DEFAULT_SSH_PORT : port);
    }

    /**
     * Set host.
     *
     * @param host
     *            host.
     */
    public void setHost(String host) {
        setHost(host, DEFAULT_SSH_PORT);
    }

    /**
     * Get host.
     *
     * @return host as set by setHost()
     */
    public String getHost() {
        return host;
    }

    /**
     * Get port.
     *
     * @return port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get hard timeout.
     *
     * @return timeout.
     */
    public long getHardTimeout() {
        return hardTimeout;
    }

    /**
     * Get soft timeout.
     *
     * @return timeout.
     */
    public long getSoftTimeout() {
        return softTimeout;
    }

    /**
     * Get user.
     *
     * @return user.
     */
    public String getUser() {
        return user;
    }

    public String getDisplayHost() {
        StringBuilder ret = new StringBuilder(100);
        if (host == null) {
            ret.append("N/A");
        } else {
            if (user != null) {
                ret.append(user);
                ret.append("@");
            }
            ret.append(host);
            if (port != DEFAULT_SSH_PORT) {
                ret.append(":");
                ret.append(port);
            }
        }
        return ret.toString();
    }

    /**
     * Connect to host.
     */
    public void connect() throws Exception {

        log.debug("Connecting '{}'", this.getDisplayHost());

        try {
            client = createSshClient();
            client.setServerKeyVerifier(serverKeyVerifier);
            client.start();

            ConnectFuture cfuture = client.connect(user, host, port);
            if (!cfuture.await(softTimeout)) {
                throw new TimeLimitExceededException(
                        String.format(
                                "SSH connection timed out connecting to '%1$s'",
                                this.getDisplayHost()));
            }

            session = cfuture.getSession();

            /*
             * Wait for authentication phase so we have host key.
             */
            Set<ClientSessionEvent> stat = session.waitFor(
                    EnumSet.of(
                            ClientSessionEvent.CLOSED,
                            ClientSessionEvent.WAIT_AUTH,
                            ClientSessionEvent.TIMEOUT),
                    softTimeout);
            if (stat.contains(ClientSessionEvent.CLOSED)) {
                throw new IOException(
                        String.format(
                                "SSH session closed during connection '%1$s'",
                                this.getDisplayHost()));
            }
            if (stat.contains(ClientSessionEvent.TIMEOUT)) {
                throw new TimeLimitExceededException(
                        String.format(
                                "SSH timed out waiting for authentication request '%1$s'",
                                this.getDisplayHost()));
            }
        } catch (Exception e) {
            log.debug("Connect error", e);
            throw e;
        }

        log.debug("Connected: '{}'", this.getDisplayHost());
    }

    /**
     * Authenticate to host.
     */
    public void authenticate() throws Exception {

        log.debug("Authenticating: '{}'", this.getDisplayHost());

        try {
            AuthFuture afuture;
            if (isKeyPairSet()) {
                session.addPublicKeyIdentity(keyPair);
            } else if (password != null) {
                session.addPasswordIdentity(password);
            } else {
                throw new AuthenticationException(
                        String.format(
                                "SSH authentication failure '%1$s', no password or key",
                                this.getDisplayHost()));
            }
            afuture = session.auth();
            if (!afuture.await(softTimeout)) {
                throw new TimeLimitExceededException(
                        String.format(
                                "SSH authentication timed out connecting to '%1$s'",
                                this.getDisplayHost()));
            }
            if (!afuture.isSuccess()) {
                throw new AuthenticationException(
                        String.format(
                                "SSH authentication to '%1$s' failed. Please verify provided credentials. %2$s",
                                this.getDisplayHost(),
                                keyPair == null ? "Make sure host is configured for password authentication"
                                        : "Make sure key is authorized at host"));
            }
        } catch (Exception e) {
            log.debug("Connect error", e);
            throw e;
        }

        log.debug("Authenticated: '{}'", this.getDisplayHost());
    }

    protected boolean isKeyPairSet() {
        return keyPair != null;
    }

    /**
     * Disconnect and cleanup.
     *
     * Must be called when done with client.
     */
    public void close() throws IOException {
        try {
            if (session != null) {
                session.close(true);
                session = null;
            }
            if (client != null) {
                client.stop();
                client = null;
            }
        } catch (Exception e) {
            log.error("Failed to close session {}", ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
            throw new IOException(e);
        }
    }

    /**
     * Execute generic command.
     *
     * @param command
     *            command to execute.
     * @param in
     *            stdin.
     * @param out
     *            stdout.
     * @param err
     *            stderr.
     */
    public void executeCommand(
            String command,
            InputStream in,
            OutputStream out,
            OutputStream err) throws Exception {

        log.debug("Executing: '{}'", command);

        if (in == null) {
            in = new ByteArrayInputStream(new byte[0]);
        }
        if (out == null) {
            out = new ConstraintByteArrayOutputStream(CONSTRAINT_BUFFER_SIZE);
        }
        if (err == null) {
            err = new ConstraintByteArrayOutputStream(CONSTRAINT_BUFFER_SIZE);
        }

        /*
         * Redirect streams into indexed streams.
         */
        ClientChannel channel = null;
        try (
                final ProgressInputStream iin = new ProgressInputStream(in);
                final ProgressOutputStream iout = new ProgressOutputStream(out);
                final ProgressOutputStream ierr = new ProgressOutputStream(err)) {
            channel = session.createExecChannel(command);
            channel.setIn(iin);
            channel.setOut(iout);
            channel.setErr(ierr);
            channel.open();

            long hardEnd = 0;
            if (hardTimeout != 0) {
                hardEnd = System.currentTimeMillis() + hardTimeout;
            }

            boolean hardTimeout = false;
            Set<ClientChannelEvent> stat;
            boolean activity;
            do {
                stat = channel.waitFor(
                        EnumSet.of(
                                ClientChannelEvent.CLOSED,
                                ClientChannelEvent.EOF,
                                ClientChannelEvent.TIMEOUT),
                        softTimeout);

                hardTimeout = hardEnd != 0 && System.currentTimeMillis() >= hardEnd;

                /*
                 * Notice that we should visit all so do not cascade statement.
                 */
                activity = iin.wasProgress();
                activity = iout.wasProgress() || activity;
                activity = ierr.wasProgress() || activity;
            } while (!hardTimeout &&
                    stat.contains(ClientChannelEvent.TIMEOUT) &&
                    activity);

            if (hardTimeout) {
                throw new TimeLimitExceededException(
                        String.format(
                                "SSH session hard timeout host '%1$s'",
                                this.getDisplayHost()));
            }

            if (stat.contains(ClientChannelEvent.TIMEOUT)) {
                throw new TimeLimitExceededException(
                        String.format(
                                "SSH session timeout host '%1$s'",
                                this.getDisplayHost()));
            }

            stat = channel.waitFor(
                    EnumSet.of(
                            ClientChannelEvent.CLOSED,
                            ClientChannelEvent.EXIT_STATUS,
                            ClientChannelEvent.EXIT_SIGNAL,
                            ClientChannelEvent.TIMEOUT),
                    softTimeout);

            if (stat.contains(ClientChannelEvent.EXIT_SIGNAL)) {
                throw new IOException(
                        String.format(
                                "Signal received during SSH session host '%1$s'",
                                this.getDisplayHost()));
            }

            if (stat.contains(ClientChannelEvent.EXIT_STATUS) && channel.getExitStatus() != 0) {
                throw new IOException(
                        String.format(
                                "Command returned failure code %2$d during SSH session '%1$s'",
                                this.getDisplayHost(),
                                channel.getExitStatus()));
            }

            if (stat.contains(ClientChannelEvent.TIMEOUT)) {
                throw new TimeLimitExceededException(
                        String.format(
                                "SSH session timeout waiting for status host '%1$s'",
                                this.getDisplayHost()));
            }

            // the PipedOutputStream does not
            // flush streams at close
            // this leads other side of pipe
            // to miss last bytes
            // not sure why it is required as
            // FilteredOutputStream does flush
            // on close.
            out.flush();
            err.flush();
        } catch (RuntimeException e) {
            log.error("Execute failed {}", ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
            throw e;
        } finally {
            if (channel != null) {
                Set<ClientChannelEvent> stat = channel.waitFor(
                        EnumSet.of(
                                ClientChannelEvent.CLOSED,
                                ClientChannelEvent.TIMEOUT),
                        1);
                if (stat.contains(ClientChannelEvent.CLOSED)) {
                    channel.close(true);
                }
            }
        }

        log.debug("Executed: '{}'", command);
    }

    protected void setServerKeyVerifier(ServerKeyVerifier serverKeyVerifier) {
        this.serverKeyVerifier = serverKeyVerifier;
    }

    protected final void addExpectedSignatures(NamedFactory<Signature>... expectedSignatures) {
        this.expectedSignatures.addAll(Arrays.asList(Objects.requireNonNull(expectedSignatures)));
    }

    protected boolean isAtLeastOneExpectedSignatureSet() {
        return !expectedSignatures.isEmpty();
    }
}
