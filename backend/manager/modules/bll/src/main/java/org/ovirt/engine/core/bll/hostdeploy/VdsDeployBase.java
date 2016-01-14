package org.ovirt.engine.core.bll.hostdeploy;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.logging.Level;

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
import org.ovirt.otopi.constants.SysEnv;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.otopi.dialog.MachineDialogParser;
import org.ovirt.otopi.dialog.SoftError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployBase implements SSHDialog.Sink, Closeable {

    public static enum DeployStatus {Complete, Incomplete, Failed, Reboot};

    private static final int THREAD_JOIN_TIMEOUT = 20 * 1000; // milliseconds
    private static final String BOOTSTRAP_CUSTOM_ENVIRONMENT_PLACE_HOLDER = "@ENVIRONMENT@";
    private static final String BOOTSTRAP_ENTRY_PLACE_HOLDER = "@ENTRY@";

    private static final Logger log = LoggerFactory.getLogger(VdsDeployBase.class);
    private static volatile CachedTar s_deployPackage;

    private static final Map<Event.Log.Severity, Level> SEVERITY_TO_LEVEL = new HashMap<Event.Log.Severity, Level>() {{
        put(Event.Log.Severity.INFO, Level.INFO);
        put(Event.Log.Severity.WARNING, Level.WARNING);
        put(Event.Log.Severity.ERROR, Level.SEVERE);
        put(Event.Log.Severity.CRITICAL, Level.SEVERE);
        put(Event.Log.Severity.FATAL, Level.SEVERE);
    }};

    /**
     * Customization vector.
     * This is tick based vector, every event execute the next
     * tick.
     */
    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG_PROLOG = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            userVisibleLog(
                Level.INFO,
                String.format(
                    "Logs at host located at: '%1$s'",
                    _parser.cliEnvironmentGet(
                        CoreEnv.LOG_FILE_NAME
                    )
                )
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                "OVIRT_ENGINE/correlationId",
                _correlationId
            );
            return true;
        }}
    );
    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG_EPILOG = new ArrayList() {{ add(
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
    private final List<Callable<Boolean>> TERMINATION_DIALOG_EPILOG = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _resultError = (Boolean)_parser.cliEnvironmentGet(
                BaseEnv.ERROR
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _installIncomplete = (Boolean)_parser.cliEnvironmentGet(
                org.ovirt.ovirt_host_deploy.constants.CoreEnv.INSTALL_INCOMPLETE
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _goingToReboot = (Boolean)_parser.cliEnvironmentGet(
                SysEnv.REBOOT
            );
            if (_goingToReboot) {
                userVisibleLog(
                    Level.INFO,
                    "Reboot scheduled"
                );
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (_resultError || !_installIncomplete) {
                _parser.cliNoop();
            }
            else {
                String[] msgs = (String[])_parser.cliEnvironmentGet(
                    org.ovirt.ovirt_host_deploy.constants.CoreEnv.INSTALL_INCOMPLETE_REASONS
                );
                userVisibleLog(
                    Level.WARNING,
                    "Installation is incomplete, manual intervention is required"
                );
                for (String m : msgs) {
                    userVisibleLog(
                        Level.WARNING,
                        m
                    );
                }
            }
            return true;
        }},
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
            userVisibleLog(
                Level.INFO,
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

    private final List<VdsDeployUnit> _units = new ArrayList<>();

    private SSHDialog.Control _control;
    private Thread _thread;
    private EngineSSHDialog _dialog;

    private final String _logPrefix;
    private final String _entryPoint;

    private MachineDialogParser _parser;
    private VDS _vds;
    private String _correlationId;
    private Exception _failException = null;
    private boolean _resultError = false;
    private boolean _installIncomplete = false;
    private boolean _goingToReboot = false;

    /*
     * Customization dialog.
     */

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
    private void nextCustomizationEntry() throws Exception {
        try {
            if (_customizationShouldAbort) {
                _parser.cliAbort();
            }
            else {
                boolean skip = false;
                Callable<Boolean> customizationStep = _customizationDialog.get(_customizationIndex);
                Method callMethod = customizationStep.getClass().getDeclaredMethod("call");
                if (callMethod != null) {
                    VdsDeployUnit.CallWhen ann = callMethod.getAnnotation(VdsDeployUnit.CallWhen.class);
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
    private void nextTerminationEntry() throws Exception {
        try {
            if (_terminationDialog.get(_terminationIndex).call()) {
                _terminationIndex++;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Protocol violation", e);
        }
    }

    /**
     * Dialog implementation.
     * Handle events incoming from host.
     */
    private void threadMain() {
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

        _dialog = new EngineSSHDialog();
        _parser = new MachineDialogParser();
        _thread = new Thread(
                () -> threadMain(),
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
    @Override
    public void close() throws IOException {
        stop();
        if (_dialog != null) {
            _dialog.close();
            _dialog = null;
        }
    }

    public void userVisibleLog(Level level, String message) {
        if (Level.SEVERE.equals(level)) {
            log.error(message);
        } else if (Level.WARNING.equals(level)) {
            log.warn(message);
        } else if (Level.INFO.equals(level)) {
            log.info(message);
        } else {
            log.debug(message);
        }
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

    public void addUnit(VdsDeployUnit... units) {
        _units.addAll(Arrays.asList(units));
        for (VdsDeployUnit unit : units) {
            unit.setVdsDeploy(this);
        }
    }

    public void addCustomizationCondition(String... cond) {
        _customizationConditions.addAll(Arrays.asList(cond));
    }

    public void removeCustomizationCondition(String cond) {
        _customizationConditions.remove(cond);
    }

    public void addCustomizationDialog(List<Callable<Boolean>> dialog) {
        _customizationDialog.addAll(dialog);
    }

    public void addTerminationDialog(List<Callable<Boolean>> dialog) {
        _terminationDialog.addAll(dialog);
    }

    public MachineDialogParser getParser() {
        return _parser;
    }

    public void setCorrelationId(String correlationId) {
        _correlationId = correlationId;
    }

    public String getCorrelationId() {
        return _correlationId;
    }

    public VDS getVds() {
        return _vds;
    }

    /**
     * Returns the installation status
     *
     * @return the installation status
     */
    public DeployStatus getDeployStatus() {
        if (_goingToReboot) {
            return DeployStatus.Reboot;
        }
        else if (_installIncomplete) {
            return DeployStatus.Incomplete;
        } else {
            return DeployStatus.Complete;
        }
    }

    /**
     * Main method.
     * Execute the command and initiate the dialog.
     */
    public void execute() throws Exception {
        _customizationDialog.addAll(CUSTOMIZATION_DIALOG_PROLOG);
        for (VdsDeployUnit unit : _units) {
            unit.init();
        }
        _customizationDialog.addAll(CUSTOMIZATION_DIALOG_EPILOG);
        _terminationDialog.addAll(TERMINATION_DIALOG_EPILOG);

        try {
            _dialog.setVds(_vds);
            _dialog.connect();
            userVisibleLog(
                Level.INFO,
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
        }
        catch (TimeLimitExceededException e){
            log.error(
                "Timeout during host {} install",
                _vds.getHostName(),
                e
            );
            userVisibleLog(
                Level.SEVERE,
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
                userVisibleLog(
                    Level.SEVERE,
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

    protected boolean processEvent(Event.Base bevent) throws Exception {
        boolean unknown = true;

        if (bevent instanceof Event.Log) {
            Event.Log event = (Event.Log)bevent;
            Level level = SEVERITY_TO_LEVEL.get(event.severity);
            if (level == null) {
                level = Level.SEVERE;
            }
            userVisibleLog(level, event.record);
            unknown = false;
        }
        else if (bevent instanceof Event.QueryString) {
            Event.QueryString event = (Event.QueryString)bevent;

            if (Queries.CUSTOMIZATION_COMMAND.equals(event.name)) {
                nextCustomizationEntry();
                unknown = false;
            }
            else if (Queries.TERMINATION_COMMAND.equals(event.name)) {
                nextTerminationEntry();
                unknown = false;
            }
        }
        else if (bevent instanceof Event.QueryValue) {
            Event.QueryValue event = (Event.QueryValue)bevent;

            if (Queries.TIME.equals(event.name)) {
                userVisibleLog(
                    Level.INFO,
                    "Setting time"
                );
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssZ");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                event.value = format.format(Calendar.getInstance().getTime());
                unknown = false;
            }
        }

        if (unknown) {
            for (VdsDeployUnit unit : _units) {
                unknown = unit.processEvent(bevent);
                if (!unknown) {
                    break;
                }
            }
        }

        return unknown;
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
