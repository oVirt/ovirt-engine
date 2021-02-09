package org.ovirt.engine.core.bll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SshHostRebootParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

@NonTransactiveCommandAttribute
public class SshHostRebootCommand<T extends SshHostRebootParameters> extends VdsCommand<T> {

    public SshHostRebootCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    /**
     * Try to reboot the host using a clean ssh systemctl method
     */
    @Override
    protected void executeCommand() {
        boolean result = executeSshReboot(getVds().getClusterCompatibilityVersion().toString());
        if (result) {
            setVdsStatus(VDSStatus.Reboot);

            // preserve maintenance status after reboot or set non-responsive for letting the monitor control the host
            runSleepOnReboot(getParameters().isWaitOnRebootSynchronous(), getStatusAfterReboot());
        }
        setSucceeded(result);
    }

    private VDSStatus getStatusAfterReboot() {
        switch(getParameters().getPrevVdsStatus()) {
            case Maintenance:
            case Installing:
                return getParameters().getPrevVdsStatus();
            default:
                return VDSStatus.NonResponsive;
        }
    }

    /**
     * Executes SSH reboot command
     *
     * @return {@code true} if command has been executed successfully, {@code false} otherwise
     */
    private boolean executeSshReboot(String version) {
        try (
                final EngineSSHClient sshClient = new EngineSSHClient();
                final ByteArrayOutputStream cmdOut = new ByteArrayOutputStream();
                final ByteArrayOutputStream cmdErr = new ByteArrayOutputStream()
        ) {
            try {
                log.info("Opening SSH reboot session on host {}", getVds().getHostName());
                sshClient.setVds(getVds());
                sshClient.useDefaultKeyPair();
                sshClient.connect();
                sshClient.authenticate();

                log.info("Executing SSH reboot command on host {}", getVds().getHostName());
                sshClient.executeCommand(
                        Config.getValue(ConfigValues.SshHostRebootCommand, version),
                        null,
                        cmdOut,
                        cmdErr
                );
                return true;
            } catch (Exception ex) {
                log.error("SSH reboot command failed on host '{}': {}\nStdout: {}\nStderr: {}",
                        getVds().getHostName(),
                        ex.getMessage(),
                        cmdOut,
                        cmdErr);
                log.debug("Exception", ex);
            }
        } catch (IOException e) {
            log.error("Error opening SSH connection to '{}': {}",
                    getVds().getHostName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
        return false;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.SYSTEM_SSH_HOST_RESTART : AuditLogType.SYSTEM_FAILED_SSH_HOST_RESTART;
    }
}
