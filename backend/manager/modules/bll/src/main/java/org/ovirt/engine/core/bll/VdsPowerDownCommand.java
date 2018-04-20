package org.ovirt.engine.core.bll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.VdsPowerDownParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.VdsDynamicDao;

/**
 * Tries to shutdown a host using SSH connection. The host has to be in maintenance mode.
 */
@NonTransactiveCommandAttribute
public class VdsPowerDownCommand<T extends VdsPowerDownParameters> extends VdsCommand<T> {

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    public VdsPowerDownCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        return getVds().getStatus() == VDSStatus.Maintenance && super.validate();
    }

    /**
     * Try to shut down the host using a clean ssh poweroff method
     */
    @Override
    protected void executeCommand() {
        setVds(null);
        if (getVds() == null) {
            handleError("SSH power down will not be executed on host {} ({}) since it doesn't exist anymore.");
            return;
        }

        /* Try this only when the Host is in maintenance state */
        if (getVds().getStatus() != VDSStatus.Maintenance) {
            handleError("SSH power down will not be executed on host {} ({}) since it is not in Maintenance.");
            return;
        }

        boolean result = executeSshPowerDown(getVds().getClusterCompatibilityVersion().toString());
        if (result) {
            // SSH powerdown executed without errors set the status to down
            setVdsStatus(VDSStatus.Down);

            // clear the automatic PM flag unless instructed otherwise
            if (!getParameters().getKeepPolicyPMEnabled()) {
                getVds().setPowerManagementControlledByPolicy(false);
                vdsDynamicDao.updateVdsDynamicPowerManagementPolicyFlag(
                        getVdsId(),
                        getVds().getDynamicData().isPowerManagementControlledByPolicy());
            }

        } else if (getParameters().getFallbackToPowerManagement() && getVds().isPmEnabled()) {
            FenceVdsActionParameters parameters = new FenceVdsActionParameters(getVds().getId());
            parameters.setKeepPolicyPMEnabled(getParameters().getKeepPolicyPMEnabled());
            runInternalAction(ActionType.StopVds,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        }
        setSucceeded(result);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDS_STOP : AuditLogType.USER_FAILED_VDS_STOP;
    }

    private void handleError(final String errorMessage) {
        setCommandShouldBeLogged(false);
        log.info(errorMessage,
                getVdsName(),
                getVdsId());
        getReturnValue().setSucceeded(false);
    }

    /**
     * Executes SSH shutdown command
     *
     * @return {@code true} if command has been executed successfully, {@code false} otherwise
     */
    private boolean executeSshPowerDown(String version) {
        boolean ret = false;
        try (
                final EngineSSHClient sshClient = new EngineSSHClient();
                final ByteArrayOutputStream cmdOut = new ByteArrayOutputStream();
                final ByteArrayOutputStream cmdErr = new ByteArrayOutputStream()
        ) {
            try {
                log.info("Opening SSH power down session on host {}", getVds().getHostName());
                sshClient.setVds(getVds());
                sshClient.useDefaultKeyPair();
                sshClient.connect();
                sshClient.authenticate();

                log.info("Executing SSH power down command on host {}", getVds().getHostName());
                sshClient.executeCommand(
                        Config.getValue(ConfigValues.SshVdsPowerdownCommand, version),
                        null,
                        cmdOut,
                        cmdErr
                );
                ret = true;
            } catch (Exception ex) {
                log.error("SSH power down command failed on host '{}': {}\nStdout: {}\nStderr: {}",
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
        return ret;
    }
}
