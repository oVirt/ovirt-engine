package org.ovirt.engine.core.bll.hostdeploy;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.naming.TimeLimitExceededException;

import org.ovirt.engine.core.bll.utils.EngineSSHDialog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.archivers.tar.CachedTar;
import org.ovirt.engine.core.uutils.ssh.SSHDialog;
import org.ovirt.otopi.constants.BaseEnv;
import org.ovirt.otopi.constants.CoreEnv;
import org.ovirt.otopi.constants.Queries;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.otopi.dialog.MachineDialogParser;
import org.ovirt.otopi.dialog.SoftError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VdsDeployBase implements SSHDialog.Sink, Closeable {

    private static final int THREAD_JOIN_TIMEOUT = 20 * 1000; // milliseconds
    private static final String BOOTSTRAP_CUSTOM_ENVIRONMENT_PLACE_HOLDER = "@ENVIRONMENT@";
    private static final String BOOTSTRAP_ENTRY_PLACE_HOLDER = "@ENTRY@";

    private static final Logger log = LoggerFactory.getLogger(VdsDeployBase.class);
    private static volatile CachedTar s_deployPackage;

    /**
     * Customization vector.
     * This is tick based vector, every event execute the next
     * tick.
     */
    // BUG: Arrays.asList() cannot handle single element correctly
    protected final List<Callable<Boolean>> CUSTOMIZATION_DIALOG_EPILOG = new ArrayList() {{ add(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliInstall();
            return true;
        }}
    );}};

    /**
     * Termination vector.
     * This is tick based vector, every event execute the next
     * tick.
     */
    protected final List<Callable<Boolean>> TERMINATION_DIALOG_PROLOG = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _resultError = (Boolean)_parser.cliEnvironmentGet(
                BaseEnv.ERROR
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _aborted = (Boolean)_parser.cliEnvironmentGet(
                BaseEnv.ABORTED
            );
            return true;
        }}
    );
    protected final List<Callable<Boolean>> TERMINATION_DIALOG_EPILOG = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            File logFile = new File(
                EngineLocalConfig.getInstance().getLogDir(),
                String.format(
                    "%1$s%2$s%3$s-%4$s-%5$s-%6$s.log",
                    "host-deploy",
                    File.separator,
                    _logPrefix,
                    new SimpleDateFormat("yyyyMMddHHmmss").format(
                        Calendar.getInstance().getTime()
                    ),
                    _vds.getHostName(),
                    _correlationId
                )
            );
            _messages.post(
                InstallerMessages.Severity.INFO,
                String.format(
                    "Retrieving installation logs to: '%1$s'",
                    logFile
                )
            );
            try (final OutputStream os = new FileOutputStream(logFile)) {
                _parser.cliDownloadLog(os);
            }
            catch (IOException e) {
                throw e;
            }
            catch (Exception e) {
                log.error("Unexpected exception", e);
                throw new RuntimeException(e);
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                CoreEnv.LOG_REMOVE_AT_EXIT,
                true
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliQuit();
            return true;
        }}
    );

    private SSHDialog.Control _control;
    private Thread _thread;
    private EngineSSHDialog _dialog;

    private final String _logPrefix;
    private final String _entryPoint;

    protected MachineDialogParser _parser;
    protected final InstallerMessages _messages;
    protected VDS _vds;
    protected String _correlationId = null;
    protected Exception _failException = null;
    protected boolean _resultError = false;
    protected boolean _aborted = false;

    protected abstract boolean processEvent(Event.Base bevent) throws IOException;
    protected void postExecute() {}

    /*
     * Customization dialog.
     */

    /**
     * Special annotation to specify when the customization is necessary.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface CallWhen {
        /**
         * @return A condition that determines if the customization should run.
         */
        String[] value();
    }
    /**
     * A set of conditions under which the conditional customizations should run.
     */
    private Set<String> _customizationConditions = new HashSet<>();
    /**
     * Customization tick.
     */
    private int _customizationIndex = 0;
    /**
     * Customization aborting.
     */
    private boolean _customizationShouldAbort = false;
    private List<Callable<Boolean>> _customizationDialog = new ArrayList<>();
    /**
     * Execute the next customization vector entry.
     */
    private void _nextCustomizationEntry() throws Exception {
        try {
            if (_customizationShouldAbort) {
                _parser.cliAbort();
            }
            else {
                boolean skip = false;
                Callable<Boolean> customizationStep = _customizationDialog.get(_customizationIndex);
                Method callMethod = customizationStep.getClass().getDeclaredMethod("call");
                if (callMethod != null) {
                    CallWhen ann = callMethod.getAnnotation(CallWhen.class);
                    skip = ann != null && !_customizationConditions.containsAll(Arrays.asList(ann.value()));
                }

                if (skip) {
                    _customizationIndex++;
                    _parser.cliNoop();
                }
                else {
                    if (customizationStep.call()) {
                        _customizationIndex++;
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Protocol violation", e);
        }
        catch (SoftError e) {
            log.error(
                "Soft error during host {} customization dialog: {}",
                _vds.getHostName(),
                e.getMessage()
            );
            log.debug("Exception", e);
            _failException = e;
            _customizationShouldAbort = true;
        }
    }
    protected void addCustomizationDialog(List<Callable<Boolean>> dialog) {
        _customizationDialog.addAll(dialog);
    }
    protected void addCustomizationCondition(String cond) {
        _customizationConditions.add(cond);
    }
    protected void removeCustomizationCondition(String cond) {
        _customizationConditions.remove(cond);
    }

    /*
     * Termination dialog.
     */

    /**
     * Termination dialog tick.
     */
    private int _terminationIndex = 0;
    private final List<Callable<Boolean>> _terminationDialog = new ArrayList<>();
    /**
     * Execute the next termination vector entry.
     */
    private void _nextTerminationEntry() throws Exception {
        try {
            if (_terminationDialog.get(_terminationIndex).call()) {
                _terminationIndex++;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Protocol violation", e);
        }
    }
    protected void addTerminationDialog(List<Callable<Boolean>> dialog) {
        _terminationDialog.addAll(dialog);
    }

    /**
     * Dialog implementation.
     * Handle events incoming from host.
     */
    private void _threadMain() {
        try {
            boolean terminate = false;

            while(!terminate) {
                Event.Base bevent = _parser.nextEvent();

                log.debug(
                    "Installation of {}: Event {}",
                    _vds.getHostName(),
                    bevent
                );

                boolean unknown = true;
                if (bevent instanceof Event.Terminate) {
                    terminate = true;
                    unknown = false;
                }
                else if (bevent instanceof Event.Log) {
                    Event.Log event = (Event.Log)bevent;
                    InstallerMessages.Severity severity;
                    switch (event.severity) {
                    case INFO:
                        severity = InstallerMessages.Severity.INFO;
                        break;
                    case WARNING:
                        severity = InstallerMessages.Severity.WARNING;
                        break;
                    default:
                        severity = InstallerMessages.Severity.ERROR;
                        break;
                    }
                    _messages.post(severity, event.record);
                    unknown = false;
                }
                else if (bevent instanceof Event.QueryString) {
                    Event.QueryString event = (Event.QueryString)bevent;

                    if (Queries.CUSTOMIZATION_COMMAND.equals(event.name)) {
                        _nextCustomizationEntry();
                        unknown = false;
                    }
                    else if (Queries.TERMINATION_COMMAND.equals(event.name)) {
                        _nextTerminationEntry();
                        unknown = false;
                    }
                }
                else if (bevent instanceof Event.QueryValue) {
                    Event.QueryValue event = (Event.QueryValue)bevent;

                    if (Queries.TIME.equals(event.name)) {
                        _messages.post(
                            InstallerMessages.Severity.INFO,
                            "Setting time"
                        );
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssZ");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        event.value = format.format(Calendar.getInstance().getTime());
                        unknown = false;
                    }
                }
                if (unknown) {
                    unknown = processEvent(bevent);
                }

                if (bevent instanceof Event.Confirm) {
                    Event.Confirm event = (Event.Confirm)bevent;
                    if (unknown) {
                        log.warn(
                            "Installation of {}: Not confirming {}: {}",
                            _vds.getHostName(),
                            event.what,
                            event.description
                        );
                    }
                    _parser.sendResponse(event);
                }
                else if (bevent instanceof Event.QueryValue) {
                    Event.QueryValue event = (Event.QueryValue)bevent;
                    if (unknown) {
                        event.abort = true;
                    }
                    _parser.sendResponse(event);
                }
                else if (bevent instanceof Event.QueryMultiString) {
                    Event.QueryMultiString event = (Event.QueryMultiString)bevent;
                    if (unknown) {
                        event.abort = true;
                    }
                    _parser.sendResponse(event);
                }

                if (unknown) {
                    throw new SoftError(
                        String.format(
                            "Unexpected event '%1$s'",
                            bevent
                        )
                    );
                }
            }
        }
        catch (Exception e) {
            _failException = e;
            log.error("Error during deploy dialog", e);
            try {
                _control.close();
            }
            catch (IOException ee) {
                log.error("Error during close", e);
            }
        }
    }

    /*
     * Constructor.
     * @param vds vds to install.
     */
    public VdsDeployBase(String logPrefix, String entryPoint, VDS vds) {
        _logPrefix = logPrefix;
        _entryPoint = entryPoint;
        _vds = vds;

        _messages = new InstallerMessages(_vds);
        _dialog = new EngineSSHDialog();
        _parser = new MachineDialogParser();
        _thread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    _threadMain();
                }
            },
            "VdsDeploy"
        );

        if (s_deployPackage == null) {
            s_deployPackage = new CachedTar(
                new File(
                    EngineLocalConfig.getInstance().getCacheDir(),
                    Config.<String> getValue(ConfigValues.BootstrapPackageName)
                ),
                new File(Config.<String> getValue(ConfigValues.BootstrapPackageDirectory))
            );
        }
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
            log.error("Exception during finalize", e);
        }
    }

    /**
     * Release resources.
     */
    public void close() throws IOException {
        stop();
        if (_dialog != null) {
            _dialog.close();
            _dialog = null;
        }
    }

    public void setCorrelationId(String correlationId) {
        _correlationId = correlationId;
        _messages.setCorrelationId(_correlationId);
    }

    /**
     * Set user.
     * @param user user.
     */
    public void setUser(String user) {
        _dialog.setUser(user);
    }

    /**
     * Set key pair.
     * @param keyPair key pair.
     */
    public void setKeyPair(KeyPair keyPair) {
        _dialog.setKeyPair(keyPair);
    }

    /**
     * Use engine default key pairs.
     */
    public void useDefaultKeyPair() throws KeyStoreException {
        _dialog.useDefaultKeyPair();
    }

    /**
     * Set password.
     * @param password password.
     */
    public void setPassword(String password) {
        _dialog.setPassword(password);
    }

    /**
     * Main method.
     * Execute the command and initiate the dialog.
     */
    public void execute() throws Exception {
        try {
            _dialog.setVds(_vds);
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

            String command = Config.<String> getValue(ConfigValues.BootstrapCommand).replace(
                BOOTSTRAP_ENTRY_PLACE_HOLDER,
                _entryPoint
            ).replace(
                // in future we should set here LANG, LC_ALL
                BOOTSTRAP_CUSTOM_ENVIRONMENT_PLACE_HOLDER,
                ""
            );

            log.info(
                "Installation of {}. Executing command via SSH {} < {}",
                _vds.getHostName(),
                command,
                s_deployPackage.getFileNoUse()
            );

            try (final InputStream in = new FileInputStream(s_deployPackage.getFile())) {
                _dialog.executeCommand(
                    this,
                    command,
                    new InputStream[] {in}
                );
            }

            if (_failException != null) {
                throw _failException;
            }

            if (_resultError) {
                // This is unlikeley as the ssh command will exit with failure.
                throw new RuntimeException(
                    "Installation failed, please refer to installation logs"
                );
            }
            else {
                postExecute();
            }
        }
        catch (TimeLimitExceededException e){
            log.error(
                "Timeout during host {} install",
                _vds.getHostName(),
                e
            );
            _messages.post(
                InstallerMessages.Severity.ERROR,
                "Processing stopped due to timeout"
            );
            throw e;
        }
        catch(Exception e) {
            log.error(
                "Error during host {} install",
                _vds.getHostName(),
                e
            );
            if (_failException == null) {
                throw e;
            }
            else {
                _messages.post(
                    InstallerMessages.Severity.ERROR,
                    e.getMessage()
                );

                log.error(
                    "Error during host {} install, prefering first exception: {}",
                    _vds.getHostName(),
                    _failException.getMessage()
                );
                log.debug("Exception", _failException);
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
        _parser.setStreams(incoming, outgoing);
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
