package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.backendinterfaces.IResourceManager;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

public class VDSBrokerFrontendImpl implements VDSBrokerFrontend {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ovirt.engine.core.bll.VDSBrokerFrontend#RunVdsCommand(org.ovirt.engine.core
     * .common.vdscommands.VDSCommandType,
     * org.ovirt.engine.core.common.vdscommands.VDSParametersBase)
     */
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
        if (!StringHelper.isNullOrEmpty(result.getExceptionString())) {
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

    private java.util.HashMap<Guid, IVdsAsyncCommand> _asyncRunningCommands =
            new java.util.HashMap<Guid, IVdsAsyncCommand>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ovirt.engine.core.bll.VDSBrokerFrontend#RunAsyncVdsCommand(com.redhat.
     * engine.common.vdscommands.VDSCommandType,
     * org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase,
     * org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand)
     */
    public VDSReturnValue RunAsyncVdsCommand(VDSCommandType commandType, VdsAndVmIDVDSParametersBase parameters,
                                             IVdsAsyncCommand command) {
        VDSReturnValue result = RunVdsCommand(commandType, parameters);
        if (result.getSucceeded()) {
            // Add async command to cached commands
            _asyncRunningCommands.put(parameters.getVmId(), command);
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
    public void RemoveAsyncRunningCommand(Guid vmId) {
        if (_asyncRunningCommands.containsKey(vmId)) {
            _asyncRunningCommands.remove(vmId);
        }
    }

    private IResourceManager getResourceManager() {
        return EjbUtils.findBean(BeanType.VDS_BROKER, BeanProxyType.LOCAL);
    }
}
