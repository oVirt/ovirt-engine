package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class VmOperationCommandBase<T extends VmOperationParameterBase> extends VmCommand<T> {

    // The log:
    private static final Log log = LogFactory.getLog(VmOperationCommandBase.class);

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected VmOperationCommandBase(Guid commandId) {
        super(commandId);
    }

    protected VmOperationCommandBase(T parameters) {
        this(parameters, null);
    }

    protected VmOperationCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        if (getRunningOnVds()) {
            perform();
            return;
        }
        setActionReturnValue((getVm() != null) ? getVm().getStatus() : VMStatus.Down);
    }

    /**
     * This method checks if the virtual machine is running in some host. It also
     * has the side effect of storing the reference to the host inside the command.
     *
     * @return <code>true</code> if the virtual machine is running in a any host,
     *   <code>false</code> otherwise
     */
    protected boolean getRunningOnVds() {
        // We will need the virtual machine and the status, so it is worth saving references:
        final VM vm = getVm();
        final VMStatus status = vm.getStatus();

        // If the status of the machine implies that it is not running in a host then
        // there is no need to find the id of the host:
        if (!status.isRunningOrPaused() && status != VMStatus.NotResponding) {
            return false;
        }

        // Find the id of the host where the machine is running:
        Guid hostId = vm.getRunOnVds();
        if (hostId == null) {
            log.warnFormat("Strange, according to the status \"{0}\" virtual machine \"{1}\" should be running in a host but it isn't.", status, vm.getId());
            return false;
        }

        // Find the reference to the host using the id that we got before (setting
        // the host to null is required in order to make sure that the host is
        // reloaded from the database):
        setVdsId(new Guid(hostId.toString()));
        setVds(null);
        if (getVds() == null) {
            log.warnFormat("Strange, virtual machine \"{0}\" is is running in host \"{1}\" but that host can't be found.", vm.getId(), hostId);
            return false;
        }

        // If we are here everything went right, so the machine is running in
        // a host:
        return true;
    }

    protected abstract void perform();
}
