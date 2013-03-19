package org.ovirt.engine.core.utils.ssh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SSH dialog to be used with SSHClient class.
 *
 * Easy processing of stdin/stdout of SSHClient session.
 * Provided the limitations of the SSH implementation this is the
 * ease the usage of the session.
 *
 * The implementation is a wrapper around SSHClient's executeCommand().
 */
public class SSHDialog {

    private static final int BUFFER_SIZE = 10 * 1024;
    private static final int DEFAULT_SSH_PORT = 22;
    private static final long DEFAULT_SOFT_TIMEOUT = 5 * 60;
    private static final long DEFAULT_HARD_TIMEOUT = 15 * 60;

    /**
     * Control interface.
     * Callback for the sink.
     */
    public interface Control {
        /**
         * Disconnect session.
         */
        public void disconnect();
    }

    /**
     * Dialog sink.
     */
    public interface Sink {
        /**
         * Set control interface.
         * @param control control.
         */
        public void setControl(SSHDialog.Control control);

        /**
         * Set streams to process.
         * @param incoming incoming stream.
         * @param outgoing outgoing stream.
         *
         * Streams are null when sink is removed from session.
         */
        public void setStreams(InputStream incoming, OutputStream outgoing);

        /**
         * Start processing.
         * Usually a thread will be created to process streams.
         * This guarantee to be called after setStreams().
         */
        public void start();

        /**
         * Stop processing.
         * Called before streams are set to null.
         */
        public void stop();
    }

    private static final Log log = LogFactory.getLog(SSHDialog.class);

    private String _host;
    private int _port;
    private String _user = "root";
    private KeyPair _keyPair;
    private String _password;
    private long _softTimeout = DEFAULT_SOFT_TIMEOUT;
    private long _hardTimeout = DEFAULT_HARD_TIMEOUT;

    private SSHClient _client;

    /**
     * Get SSH Client.
     * Used for mocking.
     * @internal
     */
    SSHClient _getSSHClient() {
        return new SSHClient();
    }

    /**
     * Destructor.
     */
    @Override
    protected void finalize() {
        disconnect();
    }

    /**
     * Get session public key.
     * @return public key or null.
     */
    public PublicKey getPublicKey() {
        if (_keyPair == null) {
            return null;
        }
        else {
            return _keyPair.getPublic();
        }
    }

    /**
     * Get host public key.
     */
    public PublicKey getHostKey() throws IOException {
        if (_client == null) {
            throw new IOException("Cannot acquire host key, session is disconnected");
        }

        PublicKey hostKey = _client.getHostKey();
        if (hostKey == null) {
            throw new IOException("Unable to retrieve host key");
        }

        return hostKey;
    }

    /**
     * Set host to connect to.
     * @param host host.
     * @param port port.
     */
    public void setHost(String host, int port) {
        _host = host;
        _port = port;
    }

    /**
     * Set host to connect to.
     * @param host host.
     */
    public void setHost(String host) {
        setHost(host, DEFAULT_SSH_PORT);
    }

    /**
     * Set user to use.
     * @param user user.
     */
    public void setUser(String user) {
        _user = user;
    }

    /**
     * Set password to use.
     * If both password and key pair are set key pair
     * is used.
     * @param password.
     */
    public void setPassword(String password) {
        _password = password;
    }

    /**
     * Set key pair.
     * If both password and key pair are set key pair
     * is used.
     * @param keyPair key pair.
     */
    public void setKeyPair(KeyPair keyPair) {
        _keyPair = keyPair;
    }

    /**
     * Set soft timeout.
     * Soft timeout is reset when there is session activity.
     * @param timeout timeout in milliseconds.
     */
    public void setSoftTimeout(long timeout) {
        _softTimeout = timeout;
    }

    /**
     * Set hard timeout.
     * Hard timeout is maximum duration of session.
     * @param timeout timeout in milliseconds.
     */
    public void setHardTimeout(long timeout) {
        _hardTimeout = timeout;
    }

    /**
     * Disconnect session.
     */
    public void disconnect() {
        if (_client != null) {
            _client.disconnect();
            _client = null;
        }
    }

    /**
     * Connect to host.
     * After connection host fingerprint can be acquired.
     */
    public void connect() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug(
                String.format(
                    "connect enter (%1$s:%2$s, %3$d, %4$d)",
                    _host,
                    _port,
                    _hardTimeout,
                    _softTimeout
                )
            );
        }

        try {
            if (_client != null) {
                throw new IOException("Already connected");
            }

            _client = _getSSHClient();
            _client.setHardTimeout(_hardTimeout);
            _client.setSoftTimeout(_softTimeout);
            _client.setHost(_host, _port);

            log.debug("connecting");
            _client.connect();
        }
        catch(Exception e) {
            log.debug(
                String.format(
                    "Could not connect to host %1$s",
                    _client.getDisplayHost()
                ),
                e
            );
            throw e;
        }
    }

    /**
     * Authenticate.
     */
    public void authenticate() throws Exception {
        _client.setUser(_user);
        _client.setPassword(_password);
        _client.setKeyPair(_keyPair);
        _client.authenticate();
    }

    /**
     * Execute command.
     * @param sink sink to use.
     * @param command command to execute.
     * @param initial initial input streams to send to host before dialog begins.
     */
    public void executeCommand(
        Sink sink,
        String command,
        InputStream initial[]
    ) throws Exception {

        log.info(String.format("SSH execute %1$s '%2$s'", _client.getDisplayHost(), command));

        final PipedInputStream pinStdin = new PipedInputStream(BUFFER_SIZE);
        final OutputStream poutStdin = new PipedOutputStream(pinStdin);
        final PipedInputStream pinStdout = new PipedInputStream(BUFFER_SIZE);
        final OutputStream poutStdout = new PipedOutputStream(pinStdout);
        final ByteArrayOutputStream stderr = new ConstraintByteArrayOutputStream(1024);

        List<InputStream> stdinList;
        if (initial == null) {
            stdinList = new LinkedList<InputStream>();
        }
        else {
            stdinList = new LinkedList<InputStream>(Arrays.asList(initial));
        }
        stdinList.add(pinStdin);

        try {
            sink.setControl(
                new Control() {
                    @Override
                    public void disconnect() {
                        if (_client != null) {
                            _client.disconnect();
                        }
                    }
                }
            );
            sink.setStreams(pinStdout, poutStdin);
            sink.start();

            try {
                _client.executeCommand(
                    command,
                    new SequenceInputStream(Collections.enumeration(stdinList)),
                    poutStdout,
                    stderr
                );
                if (stderr.size() > 0) {
                    throw new IOException("Error messages during execution");
                }
            }
            finally {
                if (stderr.size() > 0) {
                    log.error(
                        String.format(
                            "SSH stderr during command %1$s:'%2$s': stderr: %3$s",
                            _client.getDisplayHost(),
                            command,
                            new String(stderr.toByteArray(), Charset.forName("UTF-8"))
                        )
                    );
                }
            }
        }
        catch (Exception e) {
            log.error(
                String.format(
                    "SSH error running command %1$s:'%2$s'",
                    _client.getDisplayHost(),
                    command
                ),
                e
            );
            throw e;
        }
        finally {
            sink.stop();
            sink.setStreams(null, null);
        }

        log.debug("execute leave");
    }

    /**
     * Send file.
     * Send file using the embedded SSHClient.
     */
    public void sendFile(
        String file1,
        String file2
    ) throws Exception {
        _client.sendFile(file1, file2);
    }

    /**
     * Recieve file.
     * Receive file using the embedded SSHClient.
     */
    public void receiveFile(
        String file1,
        String file2
    ) throws Exception {
        _client.receiveFile(file1, file2);
    }
}
