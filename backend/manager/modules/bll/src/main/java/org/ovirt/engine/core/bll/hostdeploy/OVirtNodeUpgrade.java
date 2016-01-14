package org.ovirt.engine.core.bll.hostdeploy;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.naming.TimeLimitExceededException;

import org.ovirt.engine.core.bll.utils.EngineSSHDialog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.uutils.ssh.SSHDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ovirt-node upgrade.
 */
public class OVirtNodeUpgrade implements SSHDialog.Sink, Closeable {

    public static enum DeployStatus {Failed, Reboot};

    private static final int BUFFER_SIZE = 10 * 1024;
    private static final int THREAD_JOIN_TIMEOUT = 20 * 1000;

    private static final Logger log = LoggerFactory.getLogger(OVirtNodeUpgrade.class);

    private SSHDialog.Control _control;
    private Thread _thread;
    private EngineSSHDialog _dialog;
    private final InstallerMessages _messages;

    private BufferedReader _incoming;

    private VDS _vds;
    private File _iso;

    private Exception _failException = null;
    private DeployStatus _deployStatus = DeployStatus.Failed;

    /**
     * Dialog implementation.
     * Handle events incoming from host.
     */
    private void threadMain() {
        boolean error = false;
        try {
            String line;
            while (
                _incoming != null &&
                (line = _incoming.readLine()) != null
            ) {
                log.info("update from host '{}': {}", _vds.getHostName(), line);
                error = _messages.postOldXmlFormat(line) || error;
            }

            if (error) {
                throw new RuntimeException(
                    "Upgrade failed, please refer to logs for further information"
                );
            }
        }
        catch (Exception e) {
            _failException = e;
            log.error("Error during upgrade: {}", e.getMessage());
            log.debug("Exception", e);
            try {
                _control.close();
            }
            catch (IOException ee) {
                log.error("Error during close: {}", ee.getMessage());
                log.debug("Exception", ee);
            }
        }
    }

    /**
     * Constructor.
     * @param vds vds to install.
     * @param iso image to send.
     */
    public OVirtNodeUpgrade(VDS vds, File iso) {
        _vds = vds;
        _iso = iso;

        _messages = new InstallerMessages(_vds);
        _dialog = new EngineSSHDialog();
        _thread = new Thread(
                () -> threadMain(),
                "OVirtNodeUpgrade"
        );
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
            log.error("Exception during finalize: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }

    public void setCorrelationId(String correlationId) {
        _messages.setCorrelationId(correlationId);
    }

    /**
     * Free resources.
     */
    @Override
    public void close() throws IOException {
        stop();
        if (_dialog != null) {
            _dialog.close();
            _dialog = null;
        }
    }

    /**
     * Returns the installation status
     *
     * @return the installation status
     */
    public DeployStatus getDeployStatus() {
        return _deployStatus;
    }

    /**
     * Main method.
     * Execute the command and initiate the dialog.
     */
    public void execute() throws Exception {
        try {
            _dialog.setVds(_vds);
            _dialog.useDefaultKeyPair();
            _dialog.connect();
            _messages.post(
                InstallerMessages.Severity.INFO,
                String.format(
                    "Connected to host %1$s with SSH key fingerprint: %2$s",
                    _vds.getHostName(),
                    _dialog.getHostFingerprint()
                )
            );
            _dialog.authenticate();

            String dest = Config.<String> getValue(ConfigValues.oVirtUploadPath);

            _messages.post(
                InstallerMessages.Severity.INFO,
                String.format(
                    "Sending file %1$s to %2$s",
                    _iso,
                    dest
                )
            );

            /*
             * Create the directory where
             * file is stored, in the past
             * it was done by vdsm, then vdsm-reg
             * well, as we use hard coded path
             * we can as well do this, until we
             * have proper node upgrade script
             * that can take the image from stdin.
             */
            _dialog.executeCommand(
                new SSHDialog.Sink() {
                    @Override
                    public void setControl(SSHDialog.Control control) {}
                    @Override
                    public void setStreams(InputStream incoming, OutputStream outgoing) {}
                    @Override
                    public void start() {}
                    @Override
                    public void stop() {}
                },
                String.format(
                    "mkdir -p '%1$s'",
                    new File(dest).getParent()
                ),
                null
            );

            if (_failException != null) {
                throw _failException;
            }
            _dialog.sendFile(
                _iso.getAbsolutePath(),
                dest
            );

            String command = Config.<String> getValue(ConfigValues.oVirtUpgradeScriptName);

            _messages.post(
                InstallerMessages.Severity.INFO,
                String.format(
                    "Executing %1$s",
                    command
                )
            );

            _dialog.executeCommand(
                this,
                command,
                null
            );

            if (_failException != null) {
                throw _failException;
            }

            _deployStatus = DeployStatus.Reboot;
        }
        catch (TimeLimitExceededException e){
            log.error(
                "Timeout during node '{}' upgrade: {}",
                _vds.getHostName(),
                e.getMessage()
            );
            log.debug("Exception", e);
            _messages.post(
                InstallerMessages.Severity.ERROR,
                "Processing stopped due to timeout"
            );
            throw e;
        }
        catch (Exception e) {
            log.error("Error during node '{}' upgrade: {}", _vds.getHostName(), e.getMessage());
            log.error("Exception", e);

            if (_failException == null) {
                throw e;
            }
            else {
                log.error(
                    "Error during node '{}' upgrade, prefering first exception: {}",
                    _vds.getHostName(),
                    _failException.getMessage()
                );
                throw _failException;
            }
        }
    }

    /*
     * SSHDialog.Sink
     */

    @Override
    public void setControl(SSHDialog.Control control) {
        _control = control;
    }

    @Override
    public void setStreams(InputStream incoming, OutputStream outgoing) {
        _incoming = incoming == null ? null : new BufferedReader(
            new InputStreamReader(
                incoming,
                StandardCharsets.UTF_8
            ),
            BUFFER_SIZE
        );
    }

    @Override
    public void start() {
        _thread.start();
    }

    @Override
    public void stop() {
        if (_thread != null) {
            /*
             * We cannot just interrupt the thread as the
             * implementation of jboss connection pooling
             * drops the connection when interrupted.
             * As we may have log events pending to be written
             * to database, we wait for some time for thread
             * complete before interrupting.
             */
            try {
                _thread.join(THREAD_JOIN_TIMEOUT);
            }
            catch (InterruptedException e) {
                log.error("interrupted", e);
            }
            if (_thread.isAlive()) {
                _thread.interrupt();
                while(true) {
                    try {
                        _thread.join();
                        break;
                    }
                    catch (InterruptedException e) {}
                }
            }
            _thread = null;
        }
    }
}
