package org.ovirt.engine.core.utils.hostinstall;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.engineencryptutils.OpenSSHUtils;
import org.ovirt.engine.core.utils.ssh.ConstraintByteArrayOutputStream;
import org.ovirt.engine.core.utils.ssh.SSHClient;

public class VdsInstallerSSH {

    private static final Log log = LogFactory.getLog(VdsInstallerSSH.class);

    private SSHClient client;
    private IVdsInstallerCallback callback;
    private String host;
    private int port = 22;

    /* for testing */
    void setPort(int port) {
        this.port = port;
    }

    private void _callbackEndTransfer() {
        if (this.callback != null) {
            this.callback.endTransfer();
        }
    }

    private void _callbackFailed(String message) {
        if (this.callback != null) {
            this.callback.failed(message);
        }
    }

    private void _callbackAddError(String message) {
        if (this.callback != null) {
            this.callback.addError(message);
        }
    }

    private void _callbackAddMessage(String message) {
        if (this.callback != null) {
            this.callback.addMessage(message);
        }
    }

    private void _callbackConnected() {
        if (this.callback != null) {
            this.callback.connected();
        }
    }

    private boolean _doConnect(
        String server,
        String user,
        String userPassword,
        String keyStore,
        String keyStorePassword
    ) {
        return _doConnect(
            server,
            user,
            userPassword,
            keyStore,
            keyStorePassword,
            Config.<Integer>GetValue(
                ConfigValues.SSHInactivityTimoutSeconds
            ) * 1000
        );
    }

    private boolean _doConnect(
        String server,
        String user,
        String userPassword,
        String keyStore,
        String keyStorePassword,
        long timeout
    ) {
        log.debug(
            String.format(
                "_doConnect enter (%1$s, %2$s, %3$s, %4$s, %5$s, %6$d)",
                server,
                user,
                userPassword,
                keyStore,
                keyStorePassword,
                timeout
            )
        );

        boolean ret = false;
        this.host = server;

        try {
            this.client = new SSHClient();
            this.client.setHardTimeout(timeout);
            this.client.setSoftTimeout(timeout);
            this.client.setHost(this.host, this.port);
            this.client.setUser(user);

            if (keyStore == null) {
                log.debug("Using password authentication.");
                this.client.setPassword(userPassword);
            }
            else {
                log.debug("Using Public Key Authentication.");
                String alias = Config.<String>GetValue(ConfigValues.CertAlias);
                KeyStore.PrivateKeyEntry entry;
                InputStream in = null;
                try {
                    in = new FileInputStream(keyStore);

                    KeyStore ks = KeyStore.getInstance("JKS");
                    ks.load(in, null);

                    entry = (KeyStore.PrivateKeyEntry)ks.getEntry(
                        alias,
                        new KeyStore.PasswordProtection(
                            keyStorePassword.toCharArray()
                        )
                    );
                }
                catch (Exception e) {
                    throw new KeyStoreException(
                        String.format(
                            "Failed to get certificate entry from key store: %1$s/%2$s",
                            keyStore,
                            alias
                        ),
                        e
                    );
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch(IOException e) {
                            log.error("Cannot close key store", e);
                        }
                    }
                }

                if (entry == null) {
                    throw new KeyStoreException(
                        String.format(
                            "Bad key store: %1$s/%2$s",
                            keyStore,
                            alias
                        )
                    );
                }

                this.client.setKeyPair(
                    new KeyPair(
                        entry.getCertificate().getPublicKey(),
                        entry.getPrivateKey()
                    )
                );
            }

            log.debug("connecting");
            this.client.connect();

            PublicKey serverKey = this.client.getServerKey();
            String fingerprint = "Unknown";

            if (serverKey == null) {
                log.error("Unable to get host key");
            }
            else {
                String tmpFingerprint = OpenSSHUtils.getKeyFingerprintString(serverKey);

                if (tmpFingerprint == null) {
                    log.error("Unable to get host fingerprint");
                }
                else {
                    fingerprint = tmpFingerprint;
                }
            }

            _callbackAddMessage(
                String.format(
                    "<BSTRAP component='RHEV_INSTALL' status='OK' message='Connected to Host %1$s with SSH key fingerprint: %2$s'/>",
                    this.host,
                    fingerprint
                )
            );

            this.client.authenticate();

            _callbackConnected();
            ret = true;
        }
        catch (Exception e) {
            String m = String.format(
                "Could not connect to server %1$s",
                this.host
            );
            log.error(m, e);
            _callbackFailed(m);
            return false;
        }

        log.debug("_doConnect return " + ret);
        return ret;
    }

    /**
     * Destructor.
     */
    public void finalize() throws Exception {
        shutdown();
    }

    /*
     * Start org.ovirt.engine.core.utils.hostinstall.IVdsInstallWrapper implementation
     */

    public void shutdown() {
        log.debug("shutdown enter");

        if (this.client != null) {
            this.client.disconnect();
            this.client = null;
        }

        log.debug("shutdown leave");
    }

    public final void setCallback(IVdsInstallerCallback callback) {
        log.debug("setCallback enter");
        this.callback = callback;
        log.debug("setCallback leave");
    }

    public final boolean connect(String server) {
        return connect(
            server,
            Config.resolveKeyStorePath(),
            Config.<String>GetValue(ConfigValues.keystorePass)
        );
    }

    public final boolean connect(String server, String rootPassword) {
        return _doConnect(
            server,
            "root",
            rootPassword,
            null,
            null
        );
    }

    public final boolean connect(String server, String rootPassword, long timeout) {
        return _doConnect(
            server,
            "root",
            rootPassword,
            null,
            null,
            timeout
        );
    }

    public final boolean connect(String server, String keyStore, String keyStorePassword) {
        return _doConnect(
            server,
            "root",
            null,
            keyStore,
            keyStorePassword
        );
    }

    public final boolean executeCommand(String command, InputStream stdin) {

        /**
         * Send status per line recieved from stdout of command.
         */
        class MessageOutputStream extends OutputStream {

            byte buffer[];
            ByteBuffer bbuffer;

            void _send() {
                int n;
                do {
                    n = -1;
                    for (int i = 0;i < bbuffer.position();i++) {
                        if (buffer[i] == (byte)'\n') {
                            n = i;
                            break;
                        }
                    }

                    if (n != -1) {
                        String message;
                        try {
                            message = new String(buffer, 0, n, "UTF-8");
                        }
                        catch (UnsupportedEncodingException e) {
                            log.error("Cannot decode message", e);
                            message = "[unable to decode message]";
                        }
                        bbuffer.position(bbuffer.position()-n-1);
                        System.arraycopy(
                            buffer,
                            n+1,
                            buffer,
                            0,
                            bbuffer.position()
                        );
                        log.debug(message);
                        _callbackAddMessage(message);
                    }
                } while (n != -1);
            }

            public MessageOutputStream() {
                buffer = new byte[2048];
                bbuffer = ByteBuffer.wrap(buffer);
            }

            @Override
            public void finalize() {
                close();
            }

            @Override
            public void close() {
                if (bbuffer.position() != 0) {
                    String message;
                    try {
                        message = new String(buffer, 0, bbuffer.position(), "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        log.error("Cannot decode message", e);
                        message = "[unable to decode message]";
                    }
                    bbuffer.clear();
                    log.debug(message);
                    _callbackAddMessage(message);
                }
            }

            @Override
            public void write(byte[] b, int off, int len) {
                bbuffer.put(b, off, len);
                _send();
            }

            @Override
            public void write(int b) {
                bbuffer.put((byte)b);
                _send();
            }
        }

        log.info(String.format("SSH execute %1$s '%2$s'", this.host, command));

        boolean ret = false;

        try {
            ByteArrayOutputStream stderr = new ConstraintByteArrayOutputStream(1024);

            try {
                this.client.executeCommand(
                    command,
                    stdin,
                    new MessageOutputStream(),
                    stderr
                );
            }
            finally {
                if (stderr.size() > 0) {
                    _callbackAddError(new String(stderr.toByteArray(), "UTF-8"));
                }
            }

            if (stderr.size() == 0) {
                ret = true;
            }
        }
        catch (Exception e) {
            log.error(
                String.format(
                    "SSH error running command %1$s:'%2$s'",
                    this.host,
                    command
                ),
                e
            );
            _callbackFailed(
                String.format(
                    "SSH command failed while executing at host '%1$s', refer to logs for further information",
                    this.host
                )
            );
        }

        log.debug("executeCommand leave " + ret);
        return ret;
    }

    public final boolean executeCommand(String command) {
        return executeCommand(command, new ByteArrayInputStream(new byte[0]));
    }

    public final boolean receiveFile(String source, String destination) {

        log.info(String.format("SSH receive %1$s:'%2$s' '%3$s')", this.host, source, destination));

        boolean ret = false;

        try {
            this.client.receiveFile(source, destination);
            _callbackEndTransfer();
            ret = true;
        }
        catch (FileNotFoundException e) {
            String m = String.format(
                "SSH could not receive file %1$s:'%2$s': '%3$s'",
                this.host,
                source,
                destination
            );
            log.error(m, e);
            _callbackFailed(m);
        }
        catch (Exception e) {
            String m = String.format(
                "SSH could not receive file %1$s:'%2$s': '%3$s'",
                this.host,
                source,
                destination
            );
            log.error(m, e);
            _callbackAddError(m);
        }

        log.debug("receiveFile leave " + ret);
        return ret;
    }

    public final boolean sendFile(String source, String destination) {

        log.debug(String.format("SSH send %1$s:'%2$s' '%3$s'", this.host, source, destination));

        boolean ret = false;

        try {
            this.client.sendFile(source, destination);
            _callbackEndTransfer();
            ret = true;
        }
        catch (FileNotFoundException e) {
            log.error (
                String.format(
                    "SSH could not send file %2$s %1$s:%3$s",
                    this.host,
                    source,
                    destination
                ),
                e
            );
            _callbackFailed(e.getMessage());
        }
        catch (Exception e) {
            String m = String.format(
                "SSH could not send file %2$s %1$s:%3$s",
                this.host,
                source,
                destination
            );
            log.error(m, e);
            _callbackAddError(m);
        }

        log.debug("sendFile leave " + ret);
        return ret;
    }


    public static String getEngineSSHKeyFingerprint() {
        String fingerprint = null;

        String keystoreFile = Config.<String>GetValue(ConfigValues.keystoreUrl);
        String alias = Config.<String>GetValue(ConfigValues.CertAlias);
        InputStream in = null;
        try {
            in = new FileInputStream(keystoreFile);
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(
                in,
                Config.<String>GetValue(ConfigValues.keystorePass).toCharArray()
            );
            Certificate cert = ks.getCertificate(alias);
            if (cert == null) {
                throw new KeyStoreException(
                    String.format(
                        "Failed to find certificate store '%1$s' using alias '%2%s'",
                        keystoreFile,
                        alias
                    )
                );
            }

            fingerprint =  OpenSSHUtils.getKeyString(
                cert.getPublicKey(),
                Config.<String>GetValue(ConfigValues.SSHKeyAlias)
            );
        }
        catch (Exception e) {
            log.error(
                String.format(
                    "Failed to send own public key from store '%1$s' using alias '%2%s'",
                    keystoreFile,
                    alias
                ),
                e
            );
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch(IOException e) {
                    log.error("Cannot close key store", e);
                }
            }
        }

        return fingerprint;
    }
}
