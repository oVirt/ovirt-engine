package org.ovirt.engine.core.bll;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.VdcException;
import org.ovirt.engine.core.utils.hostinstall.ICAWrapper;
import org.ovirt.engine.core.utils.hostinstall.IVdsInstallCallBack;
import org.ovirt.engine.core.utils.hostinstall.IVdsInstallWrapper;
import org.ovirt.engine.core.utils.hostinstall.VdsInstallerFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class VdsInstaller implements IVdsInstallCallBack {
    private static final String _remoteDirectory = "/tmp";
    private final String _certificatesDirectory = "certs";
    private final String _requestsDirectory = "requests";
    private String _certRequestFileName = "cert.req";
    private String _certFileName = "cert.pem";
    private String _certFileNameLocal;
    private String _caFileName = "ca.pem";
    private static final String FIREWALL_CONFIG_FILENAME_PREFIX = "firewall.conf";
    // Extract file name, since DB entry may include path parts.
    private String _bootstrapRunningScript = new java.io.File(
            Config.<String> GetValue(ConfigValues.BootstrapInstallerFileName)).getName();

    final public static String _getUniqueIdCommand =
            "/bin/echo -e `/bin/bash -c  /usr/sbin/dmidecode|/bin/awk ' /UUID/{ print $2; } ' | /usr/bin/tr '\n' '_' && cat /sys/class/net/*/address | /bin/grep -v '00:00:00:00' | /bin/sort -u | /usr/bin/head --lines=1`";

    protected VdsInstallStages _prevInstallStage = VdsInstallStages.Start;
    protected VdsInstallStages _currentInstallStage = VdsInstallStages.Start;

    private String _failedMessage;
    // private readonly VdsInstallStatusContainer _messages = new
    // VdsInstallStatusContainer();
    private final InstallerMessages _messages;
    private Guid _fileGuid = new Guid();
    private final VDS _vds;
    private String serverInstallationTime;
    private String _bootStrapInitialCommand =
            "chmod +x {vdsInstaller}; {vdsInstaller} -c 'ssl={server_SSL_enabled};management_port={management_port}' -O '{OrganizationName}' -t {utc_time} {OverrideFirewall} {EnginePort} -b {URL1} {URL1} {vds-server} {GUID} {RunFlag}";

    protected String _finishCommand = "";

    protected final IVdsInstallWrapper _wrapper = VdsInstallerFactory.CreateVdsInstallWrapper();
    private final ICAWrapper _caWrapper = VdsInstallerFactory.CreateCaWrapper();
    protected String _serverName;
    private final String _rootPassword;
    private final String _remoteBootstrapRunningScriptPath;
    private final String remoteFwRulesFilePath;
    private boolean isAddOvirtFlow = false;
    protected static final java.util.HashMap<VdsInstallStages, String> _translatedMessages =
            new java.util.HashMap<VdsInstallStages, String>();

    static {
        _translatedMessages.put(VdsInstallStages.Start, "Starting Host installation");
        _translatedMessages.put(VdsInstallStages.ConnectToServer, "Connecting to Host");
        _translatedMessages.put(VdsInstallStages.CheckUniqueVds, "Get the unique vds id");
        _translatedMessages.put(VdsInstallStages.UploadScript, "Upload Installation script to Host");
        _translatedMessages.put(VdsInstallStages.RunScript, "Running first installation script on Host");
        _translatedMessages.put(VdsInstallStages.DownloadCertificateRequest,
                "Downloading certificate request from Host");
        _translatedMessages.put(VdsInstallStages.UploadSignedCertificate, "Upload signed sertificate to Host");
        _translatedMessages.put(VdsInstallStages.UploadCA, "Upload Cerficate Autority to Host");
        _translatedMessages.put(VdsInstallStages.SignCertificateRequest,
                "Sign certificate request and generate certificate");
        _translatedMessages.put(VdsInstallStages.FinishCommand, "Running second installation script on Host");
        _translatedMessages.put(VdsInstallStages.End, "Host installation complete");
        _translatedMessages.put(VdsInstallStages.Error, "Error during Host installation");

    }

    public VdsInstaller(VDS vds, String rootPassword, boolean overrideFirewall) {
        this(vds, rootPassword, overrideFirewall, true);
    }

    public VdsInstaller(VDS vds, String rootPassword, boolean overrideFirewall, boolean rebootAfterInstallation) {
        super();
        _vds = vds;
        this.overrideFirewall = overrideFirewall;
        _messages = new InstallerMessages(vds.getId());

        _fileGuid = Guid.NewGuid();

        _certFileNameLocal = "cert.pem";

        _certRequestFileName = String.format("cert_%1$s.req", _fileGuid);
        _certFileName = String.format("cert_%1$s.pem", _fileGuid);
        _caFileName = String.format("CA_%1$s.pem", _fileGuid);
        String[] parts = _bootstrapRunningScript.split("[.]", -1);
        if (parts.length > 1) {
            _bootstrapRunningScript = String.format("%1$s_%2$s.%3$s", parts[0], _fileGuid, parts[1]);
        } else {
            throw new RuntimeException();
        }
        _remoteBootstrapRunningScriptPath = _remoteDirectory + "/" + _bootstrapRunningScript; // Always
                                                                                              // runs
                                                                                              // on
                                                                                              // Linux
        remoteFwRulesFilePath = String.format("%s/%s.%s", _remoteDirectory,FIREWALL_CONFIG_FILENAME_PREFIX,_fileGuid);
        _bootStrapInitialCommand = InitInitialCommand(vds, _bootStrapInitialCommand);

        _finishCommand = _bootStrapInitialCommand;
        _bootStrapInitialCommand = _bootStrapInitialCommand.replace("{RunFlag}", "False");
        _finishCommand = _finishCommand.replace("{RunFlag}", "True");
        if (!rebootAfterInstallation) {
            _finishCommand = _finishCommand.replace("-b", "");
        }

        _serverName = vds.gethost_name();
        _rootPassword = rootPassword;
        _wrapper.InitCallback(this);

        _certFileNameLocal = _serverName + _certFileNameLocal;
    }

    protected String InitInitialCommand(VDS vds, String initialCommand) {
        initialCommand = initialCommand.replace("{vdsInstaller}", _remoteBootstrapRunningScriptPath);
        initialCommand = initialCommand.replace("{vds-server}", vds.gethost_name());
        initialCommand = initialCommand.replace("{URL1}", Config.<String> GetValue(ConfigValues.VdcBootStrapUrl));
        initialCommand = initialCommand.replace("{GUID}", _fileGuid.toString());
        initialCommand = initialCommand.replace("{server_SSL_enabled}",
                Config.<Boolean> GetValue(ConfigValues.UseSecureConnectionWithServers).toString());
        initialCommand = initialCommand.replace("{OrganizationName}",
                HandleOrganizationNameString(Config.<String> GetValue(ConfigValues.OrganizationName)));
        DateTime utcNow = DateTime.getUtcNow();
        serverInstallationTime = utcNow.toString("yyyy-MM-ddTHH:mm:ss");
        initialCommand = initialCommand.replace("{utc_time}", serverInstallationTime);
        initialCommand = initialCommand.replace("{management_port}", (Integer.toString(vds.getport())));

        String publicUrlPort = Config.<String> GetValue(ConfigValues.PublicURLPort);
        if (StringHelper.isNullOrEmpty(publicUrlPort)) {
            initialCommand = initialCommand.replace("{EnginePort}", "");
        } else {
            initialCommand = initialCommand.replace("{EnginePort}", String.format("-p %1$s", publicUrlPort));
        }

        initialCommand = initialCommand.replace("{OverrideFirewall}", isOverrideFirewallAllowed() ?
                "-f " + remoteFwRulesFilePath : "");
        return initialCommand;
    }

    private boolean isOverrideFirewallAllowed() {
        return overrideFirewall && _vds.getvds_type() == VDSType.VDS
                && StringUtils.isNotEmpty(Config.<String> GetValue(ConfigValues.IPTablesConfig));
    }

    private String HandleOrganizationNameString(String oName) {
        oName = oName.replace("\\", "\\\\");
        oName = oName.replace("'", "'\\''");
        return oName;
    }

    public String getErrorMessage() {
        return _failedMessage;
    }

    protected String getCurrentInstallStage() {
        return _translatedMessages.get(_currentInstallStage);
    }

    protected boolean _executionSucceded = true;
    private boolean overrideFirewall = false;

    public boolean Install() {
        _prevInstallStage = VdsInstallStages.None;
        _currentInstallStage = VdsInstallStages.Start;
        while (_currentInstallStage.getValue() < VdsInstallStages.End.getValue()) {
            // check for error
            if (!_executionSucceded) {
                log.errorFormat("Installation of {0}. Operation failure. (Stage: {1})", _serverName,
                        getCurrentInstallStage());
                _currentInstallStage = VdsInstallStages.Error;
                break;
            } else if (_currentInstallStage == _prevInstallStage) {
                _executionSucceded = false;
                log.errorFormat("Installation of {0}. No meaningful response recieved from Host. (Stage: {1})",
                        _serverName, getCurrentInstallStage());
                _currentInstallStage = VdsInstallStages.Error;
                break;
            }
            // continue logic
            else {
                _executionSucceded = false;
                _prevInstallStage = _currentInstallStage;
                RunStage();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Closing installer connections");
        }
        _wrapper.wrapperShutdown();
        return _currentInstallStage != VdsInstallStages.Error;
    }

    protected void RunStage() {
        if (this.getClass() == VdsInstaller.class) {
            log.infoFormat("Installation of {0}. Executing installation stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
        }
        switch (_currentInstallStage) {
        case Start: {
            _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
            _executionSucceded = true;
            break;
        }
        case ConnectToServer: {
            _executionSucceded = _wrapper.ConnectToServer(_serverName, _rootPassword);
            break;
        }
        case CheckUniqueVds: {
            _wrapper.RunSSHCommand(_getUniqueIdCommand);
            break;
        }
        case UploadScript: {
            String path = Config.resolveBootstrapInstallerPath();
            _executionSucceded = _wrapper.UploadFile(path, _remoteBootstrapRunningScriptPath);
            if (isOverrideFirewallAllowed() && _executionSucceded) {
                _currentInstallStage = VdsInstallStages.UploadScript;
                _executionSucceded = uploadFirewallRulesConfFile();
            }
            break;
        }
        case RunScript: {
            log.infoFormat("Installation of {0}. Sending SSH Command {1}. (Stage: {2})", _serverName,
                    _bootStrapInitialCommand, getCurrentInstallStage());
            Boolean fRes = _wrapper.RunSSHCommand(_bootStrapInitialCommand);
            log.infoFormat(" RunScript ended:" + fRes.toString());
            break;
        }
        case DownloadCertificateRequest: {
            // First parameter will always run on Linux, so use path.combine
            // just for the second param.
            Boolean fRes = _wrapper.DownloadFile(_remoteDirectory + "/" + _certRequestFileName,
                    buildCAPath(_requestsDirectory, _certRequestFileName));
            log.infoFormat(" DownloadCertificateRequest ended:" + fRes.toString());
            break;
        }
        case SignCertificateRequest: {
            _executionSucceded = _caWrapper.SignCertificateRequest(_certRequestFileName,
                    Config.<Integer> GetValue(ConfigValues.VdsCertificateValidityInYears) * 365, _certFileNameLocal);
            log.infoFormat(" SignCertificateRequest ended:" + _executionSucceded);
            if (_executionSucceded) {
                String currRequest = buildCAPath(_requestsDirectory, _certRequestFileName);
                try {
                    FileUtil.deleteFile(currRequest);
                } catch (RuntimeException exp) {
                    log.errorFormat(
                            "Installation of {0}. Could not delete certificate request file from: {1}. error: {2}. (Stage: {3}",
                            _serverName,
                            currRequest,
                            exp.getMessage(),
                            getCurrentInstallStage());
                }
                _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
            } else {
                log.error("Error signing certificate request");
            }
            break;
        }
        case UploadSignedCertificate: {
            // Second parameter will always run on Linux, so use
            // path.combine just for the first param.
            Boolean fRes = _wrapper.UploadFile(buildCAPath(_certificatesDirectory, _certFileNameLocal),
                    _remoteDirectory + "/" + _certFileName);
            log.infoFormat(" UploadSignedCertificate ended:" + fRes.toString());
            break;
        }
        case UploadCA: {
            String path = String.format("%1$s/%2$s", _remoteDirectory, _caFileName);
            _wrapper.UploadFile(Config.resolveCACertificatePath(), path);
            break;
        }
        case FinishCommand: {
            log.infoFormat("Installation of {0}. Sending SSH Command {1}. (Stage: {2})", _serverName, _finishCommand,
                    getCurrentInstallStage());
            Boolean fRes = _wrapper.RunSSHCommand(_finishCommand);
            log.infoFormat(" FinishCommand ended:" + fRes.toString());
            break;
        }
        }
    }

    private boolean uploadFirewallRulesConfFile() {
        boolean isUploaded = false;
        String ipTableConfig = Config.<String> GetValue(ConfigValues.IPTablesConfig);
        if (StringUtils.isNotEmpty(ipTableConfig)) {
            String fwRulesFileNamePath = null;

            try {
                java.io.File fwRulesFile = java.io.File.createTempFile(FIREWALL_CONFIG_FILENAME_PREFIX, null);
                fwRulesFileNamePath = fwRulesFile.getAbsolutePath();
                BufferedWriter out = new BufferedWriter(new FileWriter(fwRulesFile));
                out.write(ipTableConfig);
                out.close();
                isUploaded = _wrapper.UploadFile(fwRulesFileNamePath, remoteFwRulesFilePath);
                fwRulesFile.delete();
            } catch (IOException e) {
                log.errorFormat("Error during create and upload firewall rules temp file {0} to destination {1} with error {2}",
                        fwRulesFileNamePath == null ? FIREWALL_CONFIG_FILENAME_PREFIX : fwRulesFileNamePath,
                        remoteFwRulesFilePath,
                        ExceptionUtils.getMessage(e));
                log.debug(e);
            }
        }
        return isUploaded;
    }

    @Override
    public void AddMessage(String message) {
        if (message.toUpperCase().indexOf("<BSTRAP COMPONENT='RHEV_INSTALL' STATUS='OK'/>") != -1
                && (_currentInstallStage == VdsInstallStages.RunScript || _currentInstallStage == VdsInstallStages.FinishCommand)) {
            _executionSucceded = true;
            log.infoFormat("Installation of {0}. Recieved message: {1}. Stage completed. (Stage: {2})", _serverName,
                    message, getCurrentInstallStage());
            _messages.AddMessage(message);
            _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
        } else if (message.toUpperCase().indexOf("<BSTRAP COMPONENT='RHEV_INSTALL' STATUS='FAIL'/>") != -1
                && (_currentInstallStage == VdsInstallStages.RunScript || _currentInstallStage == VdsInstallStages.FinishCommand)) {
            _executionSucceded = false;
            log.errorFormat("Installation of {0}. Recieved message: {1}. Error occured. (Stage: {2})", _serverName,
                    message, getCurrentInstallStage());
            _messages.AddMessage(message);
        } else if (message.toUpperCase()
                .indexOf("<BSTRAP COMPONENT='RHEV_INSTALL' STATUS='OK' MESSAGE='RHEV-H ACCESSIBLE'/>") != -1
                && _currentInstallStage == VdsInstallStages.RunScript) {
            log.infoFormat("Installation of {0}. Recieved message: {1}. Stage completed. (Stage: {2})", _serverName,
                    message, getCurrentInstallStage());
            // in case and RHEV-H installation was detected - update VDS entity
            updateOvirtHostEntity();
            _messages.AddMessage(message);
            // skip all steps - vds_installer should update vdsm-reg.conf, restart vdsm-reg and quit.
            _currentInstallStage = VdsInstallStages.End;
            _executionSucceded = true;
        } else {
            log.infoFormat("Installation of {0}. Recieved message: {1}. FYI. (Stage: {2})", _serverName, message,
                    getCurrentInstallStage());
            // if its CheckUniqueVds stage assuming the message received is the unique id
            _executionSucceded = true;
            if (_currentInstallStage == VdsInstallStages.CheckUniqueVds)
            {
                _executionSucceded = InsertUniqueId(message);
                if (!_executionSucceded) {
                    _messages.AddMessage(
                        String.format(
                            "<BSTRAP component='RHEV_INSTALL' status='FAIL' message='Failed to install host %1$s, host already exists in oVirt'/>",
                            _serverName
                        )
                    );
                }
            }
            else {
                _messages.AddMessage(message);
            }
        }
    }

    private void updateOvirtHostEntity() {
        isAddOvirtFlow = true;
        _vds.setstatus(VDSStatus.PendingApproval);
        _vds.setvds_type(VDSType.oVirtNode);
        _vds.getStaticData().setOtpValidity(calculateOtp(serverInstallationTime));

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getVdsStaticDAO().update(_vds.getStaticData());
                DbFacade.getInstance().getVdsDynamicDAO().update(_vds.getDynamicData());
                return null;
            }
        });

    }

    // for backward compatibility we update the old id to new id
    // new id: BoardId_MacAddress
    // old id: BoardId
    public static void UpdateUniqueId(String id) {
        String uniqueId = (id.indexOf('_') > 0) ? id.substring(0, id.indexOf('_')) : id;
        List<VDS> list = DbFacade.getInstance().getVdsDAO().getAllWithUniqueId(uniqueId);
        if (list.size() > 0 && !id.equals(uniqueId)) {
            // save the new format of uniqueid
            list.get(0).setUniqueId(id);
            DbFacade.getInstance().getVdsStaticDAO().update(list.get(0).getStaticData());
        }
    }

    private boolean InsertUniqueId(String message) {
        String uniqueId = message.trim();
        UpdateUniqueId(uniqueId);

        if (VdsInstallHelper.isVdsUnique(_vds.getId(), uniqueId)) {
            log.infoFormat("Installation of {0}. Assigning unique id {1} to Host. (Stage: {2})", _serverName, uniqueId,
                    getCurrentInstallStage());
            _vds.setUniqueId(uniqueId);
            DbFacade.getInstance().getVdsStaticDAO().update(_vds.getStaticData());
            _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
            return true;
        }

        log.errorFormat("Installation of {0}. Host with unique id {1} is already present in system. (Stage: {2})",
                _serverName, uniqueId, getCurrentInstallStage());
        return false;

    }

    @Override
    public void Connected() {
        if (_currentInstallStage == VdsInstallStages.ConnectToServer) {
            log.infoFormat("Installation of {0}. Successfully connected to server ssh. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
            _executionSucceded = true;
        }

        else if (_currentInstallStage != VdsInstallStages.Error) {
            log.warnFormat("Installation of {0}. Illegal stage to connect to Host. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            _currentInstallStage = VdsInstallStages.Error;
        }
    }

    @Override
    public void EndTransfer() {
        if (_currentInstallStage == VdsInstallStages.UploadScript
                || _currentInstallStage == VdsInstallStages.DownloadCertificateRequest
                || _currentInstallStage == VdsInstallStages.UploadSignedCertificate
                || _currentInstallStage == VdsInstallStages.UploadCA) {
            log.infoFormat("Installation of {0}. successfully done sftp operation ( Stage: {1})", _serverName,
                    _translatedMessages.get(_currentInstallStage));
            _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
            _executionSucceded = true;
        } else if (_currentInstallStage != VdsInstallStages.Error) {
            log.warnFormat("Installation of {0}. Illegal stage for sftp operation. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            _currentInstallStage = VdsInstallStages.Error;
        }
    }

    @Override
    public void AddError(String error) {
        log.errorFormat("Installation of {0}. Error: {1}. (Stage: {2})", _serverName, error, getCurrentInstallStage());
        _messages.AddMessage(error);
    }

    @Override
    public void Failed(String error) {
        log.errorFormat("Installation of {0} has failed. Failure details: {1}. (Stage: {2})", _serverName, error,
                getCurrentInstallStage());
        _messages.AddMessage(error);
        _currentInstallStage = VdsInstallStages.Error;
        _failedMessage = error;
    }

    public static String buildCAPath(String suffix1, String suffix2) {
        return Config.resolveCABasePath() + java.io.File.separator + suffix1 + java.io.File.separator + suffix2;
    }

    public boolean isAddOvirtFlow() {
        return isAddOvirtFlow;
    }

    /**
     * Converts string material which represents a date in "yyyy-MM-dd'T'HH:mm:ss" into epoch in seconds
     *
     * @param dateMaterial
     *            the date string
     * @return
     */
    private static long calculateOtp(String dateMaterial) {
        SimpleDateFormat utcDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        utcDateTimeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        Date parsedDate;
        try {
            parsedDate = utcDateTimeFormat.parse(dateMaterial);
        } catch (ParseException e) {
            throw new VdcException(String.format("Failed to parse the date of server installation time %s",
                    dateMaterial), e);
        }
        return TimeUnit.MILLISECONDS.toSeconds(parsedDate.getTime());
    }

    private static Log log = LogFactory.getLog(VdsInstaller.class);

}
