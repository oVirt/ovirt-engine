package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class VDSBrokerFrontendImpl implements VDSBrokerFrontend {

    private Map<Guid, IVdsAsyncCommand> _asyncRunningCommands = new HashMap<Guid, IVdsAsyncCommand>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ovirt.engine.core.bll.VDSBrokerFrontend#RunVdsCommand(org.ovirt.engine.core
     * .common.vdscommands.VDSCommandType,
     * org.ovirt.engine.core.common.vdscommands.VDSParametersBase)
     */
    @Override
    public VDSReturnValue RunVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        return handleVdsResult(getResourceManager().runVdsCommand(commandType, parameters));
    }

    /**
     * Handle the result of the command, throwing an exception if one was thrown by the command ot retirning the result
     * otherwise.
     *
     * @param result
     *            The result of the command.
     * @return The result (if no exception was thrown).
     */
    private VDSReturnValue handleVdsResult(VDSReturnValue result) {
        if (StringUtils.isNotEmpty(result.getExceptionString())) {
            VdcBLLException exp;
            if (result.getVdsError() != null) {
                exp = new VdcBLLException(result.getVdsError().getCode(), result.getExceptionString());
            } else {
                exp = new VdcBLLException(VdcBllErrors.ENGINE, result.getExceptionString());
            }
            throw exp;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ovirt.engine.core.bll.VDSBrokerFrontend#RunAsyncVdsCommand(com.redhat.
     * engine.common.vdscommands.VDSCommandType,
     * org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase,
     * org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand)
     */
    @Override
    public VDSReturnValue RunAsyncVdsCommand(VDSCommandType commandType, VdsAndVmIDVDSParametersBase parameters,
                                             IVdsAsyncCommand command) {
        VDSReturnValue result = RunVdsCommand(commandType, parameters);
        if (result.getSucceeded()) {
            // Add async command to cached commands
            IVdsAsyncCommand prevCommand = _asyncRunningCommands.put(parameters.getVmId(), command);
            if (prevCommand != null && !prevCommand.equals(command)) {
                prevCommand.reportCompleted();
            }
        } else if (!result.getSucceeded()) {
            throw new VdcBLLException(result.getVdsError().getCode(), result.getExceptionString());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ovirt.engine.core.bll.VDSBrokerFrontend#GetAsyncCommandForVm(com.redhat
     * .engine.compat.Guid)
     */
    @Override
    public IVdsAsyncCommand GetAsyncCommandForVm(Guid vmId) {
        IVdsAsyncCommand result = null;
        result = _asyncRunningCommands.get(vmId);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ovirt.engine.core.bll.VDSBrokerFrontend#RemoveAsyncRunningCommand(com.
     * redhat.engine.compat.Guid)
     */
    public IVdsAsyncCommand RemoveAsyncRunningCommand(Guid vmId) {
        return _asyncRunningCommands.remove(vmId);
    }

    @Override
    public FutureVDSCall<VDSReturnValue> runFutureVdsCommand(FutureVDSCommandType commandType,
            VdsIdVDSCommandParametersBase parameters) {
        return getResourceManager().runFutureVdsCommand(commandType, parameters);
    }

    private ResourceManager getResourceManager() {
        return ResourceManager.getInstance();
    }
}
