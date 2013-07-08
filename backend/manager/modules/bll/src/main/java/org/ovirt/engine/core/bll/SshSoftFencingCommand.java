package org.ovirt.engine.core.bll;

import java.io.ByteArrayOutputStream;

import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

/**
 * Tries to restart VDSM using SSH connection
 */
@NonTransactiveCommandAttribute
public class SshSoftFencingCommand<T extends VdsActionParameters> extends VdsCommand<T> {
    /**
     * Creates an instance with specified parameters
     *
     * @param parameters
     *            command parameters
     */
    public SshSoftFencingCommand(T parameters) {
        super(parameters);
    }

    /**
     * If the VDS is not responding, it executes SSH Soft Fencing.
     */
    @Override
    protected void executeCommand() {
        setVds(null);
        if (getVds() == null) {
            setCommandShouldBeLogged(false);
            log.infoFormat("SSH Soft Fencing will not be executed on host {0}({1}) since it doesn't exist anymore.",
                    getVdsName(),
                    getVdsId());
            getReturnValue().setSucceeded(false);
            return;
        }

        VdsValidator validator = new VdsValidator(getVds());
        if (validator.shouldVdsBeFenced()) {
            boolean result = executeSshSoftFencingCommand(getVds().getVdsGroupCompatibilityVersion().toString());
            if (result) {
                // SSH Soft Fencing executed without errors, tell VdsManager about it
                ResourceManager.getInstance().GetVdsManager(getVds().getId()).finishSshSoftFencingExecution(getVds());
            }
            getReturnValue().setSucceeded(result);
        } else {
            setCommandShouldBeLogged(false);
            log.infoFormat("SSH Soft Fencing will not be executed on host {0}({1}) since it's status is ok.",
                    getVdsName(),
                    getVdsId());
            getReturnValue().setSucceeded(false);
        }
    }

    /**
     * Executes SSH Soft Fencing command
     *
     * @param host
     *            host to execute SSH Soft Fencing command on
     * @returns {@code true} if command has been executed successfully, {@code false} otherwise
     */
    private boolean executeSshSoftFencingCommand(String version) {
        boolean result = true;
        EngineSSHClient sshClient = null;
        ByteArrayOutputStream bos = null;

        try {
            sshClient = new EngineSSHClient();
            sshClient.setVds(getVds());
            sshClient.useDefaultKeyPair();
            sshClient.connect();
            sshClient.authenticate();
            bos = new ByteArrayOutputStream();
            sshClient.executeCommand(Config.<String> GetValue(ConfigValues.SshSoftFencingCommand, version),
                    null,
                    bos,
                    null);
            log.info("SSH Soft Fencing command executed on host " + getVds().getHostName());
            log.debug("SSH Soft Fencing command output " + bos.toString());
        } catch (Exception ex) {
            log.error("SSH Soft Fencing command failed on host " + getVds().getHostName(), ex);
            result = false;
        } finally {
            closeSshConnection(sshClient);
        }
        return result;
    }

    /**
     * Tries to close SSH client connection
     *
     * @param sshClient
     *            SSH client to close
     */
    private void closeSshConnection(EngineSSHClient sshClient) {
        if (sshClient != null) {
            try {
                sshClient.disconnect();
            } catch (Exception ex) {
                log.error("Error disconnecting SSH connection", ex);
            }
        }
    }
}
