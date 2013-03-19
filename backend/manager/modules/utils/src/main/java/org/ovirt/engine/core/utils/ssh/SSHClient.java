package org.ovirt.engine.core.utils.ssh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.net.SocketAddress;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.naming.AuthenticationException;
import javax.naming.TimeLimitExceededException;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.ServerKeyVerifier;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;

public class SSHClient {
    private static final String COMMAND_FILE_RECEIVE = "test -r '%2$s' && md5sum -b '%2$s' | cut -d ' ' -f 1 >&2 && %1$s < '%2$s'";
    private static final String COMMAND_FILE_SEND = "%1$s > '%2$s' && md5sum -b '%2$s' | cut -d ' ' -f 1 >&2";
    private static final int STREAM_BUFFER_SIZE = 8192;
    private static final int CONSTRAINT_BUFFER_SIZE = 1024;
    private static final int THREAD_JOIN_WAIT_TIME = 2000;
    private static final int DEFAULT_SSH_PORT = 22;

    private static Log log = LogFactory.getLog(SSHClient.class);

    private SshClient _client;
    private ClientSession _session;
    private long _softTimeout = 10000;
    private long _hardTimeout = 0;
    private String _user;
    private String _password;
    private KeyPair _keyPair;
    private String _host;
    private int _port = DEFAULT_SSH_PORT;
    private PublicKey _hostKey;

    /**
     * Create the client.
     * @return client.
     *
     * For testing using org.mockito.Mockito.
     */
    SshClient _createSshClient() {
        return SshClient.setUpDefaultClient();
    }

    /**
     * Check if file is valid.
     * @param file
     * @throws IllegalArgumentException
     *
     * This is required as we use shell to pipe into
     * file, so no special charachters are allowed.
     */
    private void _remoteFileName(String file) {
        if (
            file.indexOf('\'') != -1 ||
            file.indexOf('\n') != -1 ||
            file.indexOf('\r') != -1 ||
            false
        ) {
            throw new IllegalArgumentException("File name should not contain \"'\"");
        }
    }

    /**
     * Compare string disgest to digest.
     * @param digest MessageDigest.
     * @param actual String digest.
     * @throws IOException.
     */
    private void _validateDigest(
        MessageDigest digest,
        String actual
    ) throws IOException {
        try {
            if (
                !Arrays.equals(
                    digest.digest(),
                    DatatypeConverter.parseHexBinary(actual)
                )
            ) {
                throw new IOException("SSH copy failed, invalid localDigest");
            }
        }
        catch (IllegalArgumentException e) {
            throw new IOException("SSH copy failed, invalid localDigest");
        }
    }

    /**
     * Destructor.
     */
    @Override
    protected void finalize() {
        disconnect();
    }

    /**
     * Set soft timeout.
     * @param softTimeout timeout for network activity.
     *
     * default is 10 seconds.
     */
    public void setSoftTimeout(long softTimeout) {
        _softTimeout = softTimeout;
    }

    /**
     * Set hard timeout.
     * @param hardTimeout timeout for the entire transaction.
     *
     * timeout of 0 is infinite.
     *
     * The timeout is evaluate at softTimeout intervals.
     */
    public void setHardTimeout(long hardTimeout) {
        _hardTimeout = hardTimeout;
    }

    /**
     * Set user.
     * @param user user.
     */
    public void setUser(String user) {
        _user = user;
    }

    /**
     * Set password.
     * @param password password.
     */
    public void setPassword(String password) {
        _password = password;
    }

    /**
     * Set keypair.
     * @param keyPair key pair.
     */
    public void setKeyPair(KeyPair keyPair) {
        _keyPair = keyPair;
    }

    /**
     * Set host.
     * @param host host.
     * @param port port.
     */
    public void setHost(String host, int port) {
        _host = host;
        _port = port;
        _hostKey = null;
    }

    /**
     * Set host.
     * @param host host.
     */
    public void setHost(String host) {
        setHost(host, DEFAULT_SSH_PORT);
    }

    /**
     * Get host.
     * @return host as set by setHost()
     */
    public String getHost() {
        return _host;
    }

    /**
     * Get port.
     * @return port.
     */
    public int getPort() {
        return _port;
    }

    /**
     * Get hard timeout.
     * @return timeout.
     */
    public long getHardTimeout() {
        return _hardTimeout;
    }

    /**
     * Get soft timeout.
     * @return timeout.
     */
    public long getSoftTimeout() {
        return _softTimeout;
    }

    /**
     * Get user.
     * @return user.
     */
    public String getUser() {
        return _user;
    }

    public String getDisplayHost() {
        StringBuilder ret = new StringBuilder(100);
        if (_host == null) {
            ret.append("N/A");
        }
        else {
            if (_user != null) {
                ret.append(_user);
                ret.append("@");
            }
            ret.append(_host);
            if (_port != DEFAULT_SSH_PORT) {
                ret.append(":");
                ret.append(_port);
            }
        }
        return ret.toString();
    }

    /**
     * Get host key
     * @return host key.
     */
    public PublicKey getHostKey() {
        return _hostKey;
    }

    /**
     * Connect to host.
     */
    public void connect() throws Exception {

        log.debug(String.format("Connecting: '%1$s'", this.getDisplayHost()));

        try {
            _client = _createSshClient();

            _client.setServerKeyVerifier(
                new ServerKeyVerifier() {
                    @Override
                    public boolean verifyServerKey(
                        ClientSession sshClientSession,
                        SocketAddress remoteAddress,
                        PublicKey serverKey
                    ) {
                        _hostKey = serverKey;
                        return true;
                    }
                }
            );

            _client.start();

            ConnectFuture cfuture = _client.connect(_host, _port);
            if (!cfuture.await(_softTimeout)) {
                throw new TimeLimitExceededException(
                    String.format(
                        "SSH connection timed out connecting to '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }

            _session = cfuture.getSession();

            /*
             * Wait for authentication phase so
             * we have host key.
             */
            int stat = _session.waitFor(
                (
                    ClientSession.CLOSED |
                    ClientSession.WAIT_AUTH |
                    ClientSession.TIMEOUT
                ),
                _softTimeout
            );
            if ((stat & ClientSession.CLOSED) != 0) {
                throw new IOException(
                    String.format(
                        "SSH session closed during connection '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }
            if ((stat & ClientSession.TIMEOUT) != 0) {
                throw new TimeLimitExceededException(
                    String.format(
                        "SSH timed out waiting for authentication request '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }
        }
        catch(Exception e) {
            log.debug("Connect error", e);
            throw e;
        }

        log.debug(String.format("Connected: '%1$s'", this.getDisplayHost()));
    }

    /**
     * Authenticate to host.
     */
    public void authenticate() throws Exception {

        log.debug(String.format("Authenticating: '%1$s'", this.getDisplayHost()));

        try {
            AuthFuture afuture;
            if (_keyPair != null) {
                afuture = _session.authPublicKey(_user, _keyPair);
            }
            else if (_password != null) {
                afuture = _session.authPassword(_user, _password);
            }
            else {
                throw new AuthenticationException(
                    String.format(
                        "SSH authentication failure '%1$s', no password or key",
                        this.getDisplayHost()
                    )
                );
            }
            if (!afuture.await(_softTimeout)) {
                throw new TimeLimitExceededException(
                    String.format(
                        "SSH authentication timed out connecting to '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }
            if (!afuture.isSuccess()) {
                throw new AuthenticationException(
                    String.format(
                        "SSH authentication to '%1$s' failed%2$s",
                        this.getDisplayHost(),
                        (
                            _keyPair == null ?
                            " make sure host is configured for password authentication" :
                            " make sure key is authorized at host"
                        )
                    )
                );
            }
        }
        catch(Exception e) {
            log.debug("Connect error", e);
            throw e;
        }

        log.debug(String.format("Authenticated: '%1$s'", this.getDisplayHost()));
    }

    /**
     * Disconnect and cleanup.
     *
     * Must be called when done with client.
     */
    public void disconnect() {
        if (_session != null) {
            _session.close(true);
            _session = null;
        }
        if (_client != null) {
            _client.stop();
            _client = null;
        }
    }

    /**
     * Execute generic command.
     * @param command command to execute.
     * @param in stdin.
     * @param out stdout.
     * @param err stderr.
     */
    public void executeCommand(
        String command,
        InputStream in,
        OutputStream out,
        OutputStream err
    ) throws Exception {

        log.debug(String.format("Executing: '%1$s'", command));

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
        ProgressInputStream iin = new ProgressInputStream(in);
        ProgressOutputStream iout = new ProgressOutputStream(out);
        ProgressOutputStream ierr = new ProgressOutputStream(err);

        ClientChannel channel = _session.createExecChannel(command);

        try {
            channel.setIn(iin);
            channel.setOut(iout);
            channel.setErr(ierr);
            channel.open();

            long hardEnd = 0;
            if (_hardTimeout != 0) {
                hardEnd = System.currentTimeMillis() + _hardTimeout;
            }

            boolean hardTimeout = false;
            int stat;
            boolean activity;
            do {
                stat = channel.waitFor(
                    (
                        ClientChannel.CLOSED |
                        ClientChannel.EOF |
                        ClientChannel.TIMEOUT
                    ),
                    _softTimeout
                );

                hardTimeout = (hardEnd != 0 && System.currentTimeMillis() >= hardEnd);

                /*
                 * Notice that we should visit all
                 * so do not cascade statement.
                 */
                activity = iin.wasProgress();
                activity = iout.wasProgress() || activity;
                activity = ierr.wasProgress() || activity;
            } while(
                !hardTimeout &&
                (stat & ClientChannel.TIMEOUT) != 0 &&
                activity
            );

            if (hardTimeout) {
                throw new TimeLimitExceededException(
                    String.format(
                        "SSH session hard timeout host '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }

            if ((stat & ClientChannel.TIMEOUT) != 0) {
                throw new TimeLimitExceededException(
                    String.format(
                        "SSH session timeout host '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }

            stat = channel.waitFor(
                (
                    ClientChannel.CLOSED |
                    ClientChannel.EXIT_STATUS |
                    ClientChannel.EXIT_SIGNAL |
                    ClientChannel.TIMEOUT
                ),
                _softTimeout
            );

            if ((stat & ClientChannel.EXIT_SIGNAL) != 0) {
                throw new IOException(
                    String.format(
                        "Signal received during SSH session host '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }

            if ((stat & ClientChannel.EXIT_STATUS) != 0 && channel.getExitStatus() != 0) {
                throw new IOException(
                    String.format(
                        "Command returned failure code %2$d during SSH session '%1$s'",
                        this.getDisplayHost(),
                        channel.getExitStatus()
                    )
                );
            }

            if ((stat & ClientChannel.TIMEOUT) != 0) {
                throw new TimeLimitExceededException(
                    String.format(
                        "SSH session timeout waiting for status host '%1$s'",
                        this.getDisplayHost()
                    )
                );
            }
        }
        catch (RuntimeException e) {
            log.debug("Execute failed", e);
            throw e;
        }
        finally {
            int stat = channel.waitFor(
                (
                    ClientChannel.CLOSED |
                    ClientChannel.TIMEOUT
                ),
                1
            );
            if ((stat & ClientChannel.CLOSED) != 0) {
                channel.close(true);
            }
        }

        log.debug(String.format("Executed: '%1$s'", command));
    }

    /**
     * Send file using compression and digest check.
     *
     * @param file1 source.
     * @param file2 destination.
     *
     * We read the file content into gzip and then
     * pipe it into the ssh.
     * Calculating the remoteDigest on the fly.
     *
     * The digest is printed into stderr for us to collect.
     *
     */
    public void sendFile(
        String file1,
        String file2
    ) throws Exception {

        log.debug(String.format("Sending: '%1$s' '%2$s", file1, file2));

        _remoteFileName(file2);

        MessageDigest localDigest = MessageDigest.getInstance("MD5");

        // file1->{}->digest->in->out->pout->pin->stdin

        final InputStream in = new DigestInputStream(
            new FileInputStream(file1),
            localDigest
        );
        PipedInputStream pin = new PipedInputStream(STREAM_BUFFER_SIZE);
        final OutputStream pout = new PipedOutputStream(pin);

        Thread t = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    OutputStream out = null;
                    try {
                        out = new GZIPOutputStream(pout);
                        byte b[] = new byte[STREAM_BUFFER_SIZE];
                        int n;
                        while ((n = in.read(b)) != -1) {
                            out.write(b, 0, n);
                        }
                    }
                    catch (IOException e) {
                        log.debug("Exceution during stream processing", e);
                    }
                    finally {
                        if (out != null) {
                            try {
                                out.close();
                            }
                            catch (IOException e) {
                                log.error("Cannot close stream", e);
                            }
                        }
                    }
                }
            },
            "SSHClient.compress " + file1
        );
        try {
            t.start();

            ByteArrayOutputStream remoteDigest = new ConstraintByteArrayOutputStream(CONSTRAINT_BUFFER_SIZE);

            executeCommand(
                String.format(COMMAND_FILE_SEND, "gunzip -q", file2),
                pin,
                new ConstraintByteArrayOutputStream(CONSTRAINT_BUFFER_SIZE),
                remoteDigest
            );

            t.join(THREAD_JOIN_WAIT_TIME);
            if (t.getState() != Thread.State.TERMINATED) {
                throw new IllegalStateException("Cannot stop SSH stream thread");
            }

            _validateDigest(localDigest, new String(remoteDigest.toByteArray(), Charset.forName("UTF-8")).trim());
        }
        catch(Exception e) {
            log.debug("Send failed", e);
            throw e;
        }
        finally {
            t.interrupt();
            try {
                in.close();
            }
            catch(IOException e) {
                log.error("Cannot close stream", e);
            }
        }

        log.debug(String.format("Sent: '%1$s' '%2$s", file1, file2));
    }

    /**
     * Receive file using compression and localDigest check.
     *
     * @param file1 source.
     * @param file2 destination.
     *
     * We read the stream and pipe into gunzip, and
     * write into the file.
     * Calculating the remoteDigest on the fly.
     *
     * The localDigest is printed into stderr for us to collect.
     */
    public void receiveFile(
        String file1,
        String file2
    ) throws Exception {

        log.debug(String.format("Receiving: '%1$s' '%2$s", file1, file2));

        _remoteFileName(file1);

        MessageDigest localDigest = MessageDigest.getInstance("MD5");

        // stdout->pout->pin->in->out->digest->{}->file2

        PipedOutputStream pout = new PipedOutputStream();
        final InputStream pin = new PipedInputStream(pout, STREAM_BUFFER_SIZE);
        final OutputStream out = new DigestOutputStream(
            new FileOutputStream(file2),
            localDigest
        );
        Thread t = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    InputStream in = null;
                    try {
                        in = new GZIPInputStream(pin);

                        byte [] b = new byte[STREAM_BUFFER_SIZE];
                        int n;
                        while ((n = in.read(b)) != -1) {
                            out.write(b, 0, n);
                        }
                    }
                    catch (IOException e) {
                        log.debug("Exceution during stream processing", e);
                    }
                    finally {
                        if (in != null) {
                            try {
                                in.close();
                            }
                            catch(IOException e) {
                                log.error("Cannot close stream", e);
                            }
                        }
                    }
                }
            },
            "SSHClient.decompress " + file2
        );

        try {
            t.start();

            ByteArrayOutputStream remoteDigest = new ConstraintByteArrayOutputStream(CONSTRAINT_BUFFER_SIZE);

            executeCommand(
                String.format(COMMAND_FILE_RECEIVE, "gzip -q", file1),
                new ByteArrayInputStream(new byte[0]),
                pout,
                remoteDigest
            );

            t.join(THREAD_JOIN_WAIT_TIME);
            if (t.getState() != Thread.State.TERMINATED) {
                throw new IllegalStateException("Cannot stop SSH stream thread");
            }

            _validateDigest(localDigest, new String(remoteDigest.toByteArray(), Charset.forName("UTF-8")).trim());
        }
        catch(Exception e) {
            log.debug("Receive failed", e);
            throw e;
        }
        finally {
            t.interrupt();
            try {
                out.close();
            }
            catch(IOException e) {
                log.error("Cannot close stream", e);
            }
        }

        log.debug(String.format("Received: '%1$s' '%2$s", file1, file2));
    }
}
