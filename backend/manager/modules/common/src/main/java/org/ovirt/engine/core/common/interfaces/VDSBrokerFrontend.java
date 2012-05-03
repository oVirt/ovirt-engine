package org.ovirt.engine.core.common.interfaces;

import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public interface VDSBrokerFrontend {

    VDSReturnValue RunVdsCommand(VDSCommandType commandType, VDSParametersBase parameters);

    VDSReturnValue RunAsyncVdsCommand(VDSCommandType commandType,
            VdsAndVmIDVDSParametersBase parameters, IVdsAsyncCommand command);

    IVdsAsyncCommand GetAsyncCommandForVm(Guid vmId);

    IVdsAsyncCommand RemoveAsyncRunningCommand(Guid vmId);

    FutureVDSCall<VDSReturnValue> runFutureVdsCommand(FutureVDSCommandType commandType,
            VdsIdVDSCommandParametersBase parameters);

}
