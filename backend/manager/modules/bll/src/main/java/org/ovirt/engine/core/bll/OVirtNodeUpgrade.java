package org.ovirt.engine.core.bll;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.naming.TimeLimitExceededException;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ssh.EngineSSHDialog;
import org.ovirt.engine.core.utils.ssh.SSHDialog;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * ovirt-node upgrade.
 */
public class OVirtNodeUpgrade implements SSHDialog.Sink {

    private static final int BUFFER_SIZE = 10 * 1024;
    private static final int THREAD_JOIN_TIMEOUT = 20 * 1000;

    private static final Log log = LogFactory.getLog(OVirtNodeUpgrade.class);

    private SSHDialog.Control _control;
    private Thread _thread;
    private EngineSSHDialog _dialog;
    private final InstallerMessages _messages;

    private BufferedReader _incoming;

    private VDS _vds;
    private String _iso;

    private Exception _failException = null;

    /**
     * Set vds object status.
     * For this simple task, no need to go via command mechanism.
     * @param status new status.
     */
    private void _setVdsStatus(VDSStatus status) {
        _vds.setStatus(status);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getVdsDynamicDao().update(_vds.getDynamicData());
                return null;
            }
        });
    }

    /**
     * Dialog implementation.
     * Handle events incoming from host.
     */
    private void _threadMain() {
        boolean error = false;
        try {
            String line;
            while (
                _incoming != null &&
                (line = _incoming.readLine()) != null
            ) {
                log.infoFormat("update from host {0}: {1}", _vds.getHostName(), line);
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
            log.error("Error during upgrade", e);
            _control.disconnect();
        }
    }

    /**
     * Constructor.
     * @param vds vds to install.
     * @param iso image to send.
     */
    public OVirtNodeUpgrade(VDS vds, String iso) {
        _vds = vds;
        _iso = Config.resolveOVirtISOsRepositoryPath() + File.separator + iso;

        _messages = new InstallerMessages(_vds);
        _dialog = new EngineSSHDialog();
        _thread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    _threadMain();
                }
            },
            "OVirtNodeUpgrade"
        );
    }

    /**
     * Destructor.
     */
    @Override
    protected void finalize() {
        close();
    }

    public void setCorrelationId(String correlationId) {
        _messages.setCorrelationId(correlationId);
    }

    /**
     * Free resources.
     */
    public void close() {
        stop();
        if (_dialog != null) {
            _dialog.disconnect();
            _dialog = null;
        }
    }

    /**
     * Main method.
     * Execute the command and initiate the dialog.
     */
    public void execute() throws Exception {
        try {
            _setVdsStatus(VDSStatus.Installing);

            _dialog.useDefaultKeyPair();
            _dialog.setHost(_vds.getHostName());
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

            String dest = Config.<String> GetValue(ConfigValues.oVirtUploadPath);

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
                _iso,
                dest
            );

            String command = Config.<String> GetValue(ConfigValues.oVirtUpgradeScriptName);

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

            _setVdsStatus(VDSStatus.Reboot);
        }
        catch (TimeLimitExceededException e){
            log.errorFormat(
                "Timeout during node {0} upgrade",
                _vds.getHostName(),
                e
            );
            _messages.post(
                InstallerMessages.Severity.ERROR,
                "Processing stopped due to timeout"
            );
            _setVdsStatus(VDSStatus.InstallFailed);
            throw e;
        }
        catch (Exception e) {
            log.errorFormat("Error during node {0} upgrade", _vds.getHostName(), e);
            _setVdsStatus(VDSStatus.InstallFailed);

            if (_failException == null) {
                throw e;
            }
            else {
                log.errorFormat(
                    "Error during node {0} upgrade, prefering first exception",
                    _vds.getHostName(),
                    _failException
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
                Charset.forName("UTF-8")
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
