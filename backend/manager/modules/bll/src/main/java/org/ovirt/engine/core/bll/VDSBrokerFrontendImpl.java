package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.errors.EngineException;
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

@Singleton
public class VDSBrokerFrontendImpl implements VDSBrokerFrontend {

    @Inject
    private Instance<ResourceManager> resourceManager;

    private Map<Guid, IVdsAsyncCommand> _asyncRunningCommands = new HashMap<>();

    @Override
    public VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        return VdsHandler.handleVdsResult(getResourceManager().runVdsCommand(commandType, parameters));
    }

    @Override
    public VDSReturnValue runAsyncVdsCommand(VDSCommandType commandType, VdsAndVmIDVDSParametersBase parameters,
                                             IVdsAsyncCommand command) {
        VDSReturnValue result = runVdsCommand(commandType, parameters);
        if (result.getSucceeded()) {
            // Add async command to cached commands
            IVdsAsyncCommand prevCommand = _asyncRunningCommands.put(parameters.getVmId(), command);
            if (prevCommand != null && !prevCommand.equals(command)) {
                prevCommand.reportCompleted();
            }
        } else {
            throw new EngineException(result.getVdsError().getCode(), result.getExceptionString());
        }

        return result;
    }

    @Override
    public IVdsAsyncCommand getAsyncCommandForVm(Guid vmId) {
        IVdsAsyncCommand result = null;
        result = _asyncRunningCommands.get(vmId);
        return result;
    }

    @Override
    public IVdsAsyncCommand removeAsyncRunningCommand(Guid vmId) {
        return _asyncRunningCommands.remove(vmId);
    }

    @Override
    public FutureVDSCall<VDSReturnValue> runFutureVdsCommand(FutureVDSCommandType commandType,
            VdsIdVDSCommandParametersBase parameters) {
        return getResourceManager().runFutureVdsCommand(commandType, parameters);
    }

    private ResourceManager getResourceManager() {
        return resourceManager.get();
    }
}
