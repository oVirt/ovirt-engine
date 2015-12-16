package org.ovirt.engine.core.uutils.ssh;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSH dialog to be used with SSHClient class.
 *
 * Easy processing of stdin/stdout of SSHClient session.
 * Provided the limitations of the SSH implementation this is the
 * ease the usage of the session.
 *
 * The implementation is a wrapper around SSHClient's executeCommand().
 */
public class SSHDialog implements Closeable {

    private static final int BUFFER_SIZE = 10 * 1024;
    private static final int DEFAULT_SSH_PORT = 22;

    /**
     * Control interface.
     * Callback for the sink.
     */
    public interface Control {
        /**
         * Disconnect session.
         */
        public void close() throws IOException;
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

    private static final Logger log = LoggerFactory.getLogger(SSHDialog.class);

    private String _host;
    private int _port;
    private String _user = "root";
    private KeyPair _keyPair;
    private String _password;
    private long _softTimeout = 0;
    private long _hardTimeout = 0;

    protected SSHClient _client;

    /**
     * Get SSH Client.
     * Used for mocking.
     */
    protected SSHClient getSSHClient() {
        return new SSHClient();
    }

    /**
     * Destructor.
     */
    @Override
    protected void finalize() {
        try {
            close();
        }
        catch (IOException e) {
            log.error("Finalize exception", e);
        }
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
    public void close() throws IOException {
        if (_client != null) {
            _client.close();
            _client = null;
        }
    }

    /**
     * Connect to host.
     * After connection host fingerprint can be acquired.
     */
    public void connect() throws Exception {
        log.debug(
            "connect enter ({}:{}, {}, {})",
            _host,
            _port,
            _hardTimeout,
            _softTimeout
        );

        try {
            if (_client != null) {
                throw new IOException("Already connected");
            }

            _client = getSSHClient();
            if (_hardTimeout != 0) {
                _client.setHardTimeout(_hardTimeout);
            }
            if (_softTimeout != 0) {
                _client.setSoftTimeout(_softTimeout);
            }
            _client.setHost(_host, _port);

            log.debug("connecting");
            _client.setUser(_user);
            _client.connect();
        }
        catch(Exception e) {
            if (_client != null) {
                log.debug(
                    "Could not connect to host '{}'",
                    _client.getDisplayHost()
                );
            }
            else {
                log.debug(
                    "Could not connect to host"
                );
            }
            log.debug("Exception", e);
            throw e;
        }
    }

    /**
     * Authenticate.
     */
    public void authenticate() throws Exception {
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
        InputStream[] initial
    ) throws Exception {

        log.info("SSH execute '{}' '{}'", _client.getDisplayHost(), command);

        try (
            final PipedInputStream pinStdin = new PipedInputStream(BUFFER_SIZE);
            final OutputStream poutStdin = new PipedOutputStream(pinStdin);
            final PipedInputStream pinStdout = new PipedInputStream(BUFFER_SIZE);
            final OutputStream poutStdout = new PipedOutputStream(pinStdout);
            final ByteArrayOutputStream stderr = new ConstraintByteArrayOutputStream(1024);
        ) {
            try {
                List<InputStream> stdinList;
                if (initial == null) {
                    stdinList = new LinkedList<>();
                }
                else {
                    stdinList = new LinkedList<>(Arrays.asList(initial));
                }
                stdinList.add(pinStdin);

                sink.setControl(
                    new Control() {
                        @Override
                        public void close() throws IOException {
                            if (_client != null) {
                                _client.close();
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
                }
                catch (Exception e) {
                    if (stderr.size() == 0) {
                        throw e;
                    }

                    log.error(
                        "Swallowing exception as preferring stderr",
                        e
                    );
                }
                finally {
                    if (stderr.size() > 0) {
                        throw new RuntimeException(
                            String.format(
                                "Unexpected error during execution: %1$s",
                                new String(stderr.toByteArray(), StandardCharsets.UTF_8)
                            )
                        );
                    }
                }
            }
            catch (Exception e) {
                log.error(
                    "SSH error running command {}:'{}': {}",
                    _client.getDisplayHost(),
                    command,
                    e.getMessage()
                );
                log.error("Exception", e);
                throw e;
            }
            finally {
                sink.stop();
                sink.setStreams(null, null);
            }
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
