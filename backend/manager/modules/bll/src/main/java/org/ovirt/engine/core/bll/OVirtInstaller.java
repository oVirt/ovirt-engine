package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class OVirtInstaller extends VdsInstaller {
    private String createCertReqCommand = Config.<String> GetValue(ConfigValues.CBCCertificateScriptName)
            + " -O \"{OrganizationName}\" {vds-server} {GUID}";
    private String finishCommandInitial = Config.<String> GetValue(ConfigValues.CBCCloseCertificateScriptName)
            + " -c 'ssl={server_SSL_enabled}' {GUID} 0";

    public OVirtInstaller(VDS vds) // Call base constructor with null password
                                 // (because we are using know public key)
    {
        super(vds, null, false);
        createCertReqCommand = InitInitialCommand(vds, createCertReqCommand);
        finishCommandInitial = InitInitialCommand(vds, finishCommandInitial);
    }

    @Override
    protected String InitInitialCommand(VDS vds, String initialCommand) {
        // Add the noBoot flag so that bootstrap process will not reboot machine
        // (since it's not necessary)
        initialCommand = initialCommand.replace("{URL1}", "-b {URL1}");
        initialCommand = super.InitInitialCommand(vds, initialCommand);
        return initialCommand;
    }

    @Override
    protected void RunStage() {

        switch (_currentInstallStage) {
        // Use ConnectToServer method which does not need password (relies
        // on public key existing on cbc image)
        // Keep base logic for following states
        case Start:
        case SignCertificateRequest:
        case UploadSignedCertificate:
        case UploadCA:
        case DownloadCertificateRequest: {
            log.infoFormat("Installation of {0}. Executing oVirt installation stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            super.RunStage();
            break;
        }
        case ConnectToServer: {
            log.infoFormat("Installation of {0}. Executing oVirt installation stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            _executionSucceded = _wrapper.ConnectToServer(_serverName);
            break;
        }
        case RunScript: {
            log.infoFormat("Installation of {0}. Executing oVirt installation stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            _wrapper.RunSSHCommand(createCertReqCommand);
            break;
        }
        case FinishCommand: {
            log.infoFormat(
                    "Installation of {0}. Executing oVirt installation stage, sending SSH Command {1}. (Stage: {2})",
                    _serverName, finishCommandInitial, getCurrentInstallStage());
            _wrapper.RunSSHCommand(finishCommandInitial);
            break;
        }
            // Ignore unused states : VdsInstallStages.UploadScript,
            // VdsInstallStages.CheckUniqueVds
        default: {
            _executionSucceded = true;
            _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
            break;
        }
        }
    }

    private static Log log = LogFactory.getLog(OVirtInstaller.class);
}
