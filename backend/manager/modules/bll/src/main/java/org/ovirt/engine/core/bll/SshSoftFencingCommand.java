package org.ovirt.engine.core.bll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.ovirt.engine.core.bll.context.CommandContext;
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
        this(parameters, null);
    }

    public SshSoftFencingCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
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
        if (isPmReportsStatusDown()) {
            // do not try to soft-fence if Host is reported as Down via PM
            getReturnValue().setSucceeded(false);
        }
        else {
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
    }

    /**
     * Executes SSH Soft Fencing command
     *
     * @param host
     *            host to execute SSH Soft Fencing command on
     * @returns {@code true} if command has been executed successfully, {@code false} otherwise
     */
    private boolean executeSshSoftFencingCommand(String version) {
        boolean ret = false;
        try (
            final EngineSSHClient sshClient = new EngineSSHClient();
            final ByteArrayOutputStream cmdOut = new ByteArrayOutputStream();
            final ByteArrayOutputStream cmdErr = new ByteArrayOutputStream();
        ) {
            try {
                log.infoFormat("Opening SSH Soft Fencing session on host {0}", getVds().getHostName());
                sshClient.setVds(getVds());
                sshClient.useDefaultKeyPair();
                sshClient.connect();
                sshClient.authenticate();

                log.infoFormat("Executing SSH Soft Fencing command on host {0}", getVds().getHostName());
                sshClient.executeCommand(
                    Config.<String> getValue(ConfigValues.SshSoftFencingCommand, version),
                    null,
                    cmdOut,
                    cmdErr
                );
                ret = true;
            } catch (Exception ex) {
                log.errorFormat("SSH Soft Fencing command failed on host {0}: {1}\nStdout: {2}\nStderr: {3}\nStacktrace: {4}",
                        getVds().getHostName(), ex.getMessage(), cmdOut.toString(), cmdErr.toString(), ex);
            }
        }
        catch(IOException e) {
            log.error("IOException", e);
        }
        return ret;
    }
}
