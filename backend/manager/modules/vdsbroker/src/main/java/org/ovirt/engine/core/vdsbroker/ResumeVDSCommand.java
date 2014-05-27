package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResumeBrokerVDSCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResumeVDSCommand<P extends ResumeVDSCommandParameters> extends ManagingVmCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(ResumeVDSCommand.class);

    public ResumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        ResumeVDSCommandParameters parameters = getParameters();
        VMStatus retval = VMStatus.Unknown;
        ResumeBrokerVDSCommand<VdsAndVmIDVDSParametersBase> command =
                new ResumeBrokerVDSCommand<VdsAndVmIDVDSParametersBase>(parameters);
        command.execute();
        if (command.getVDSReturnValue().getSucceeded()) {
            retval = VMStatus.PoweringUp;
            ResourceManager.getInstance().AddAsyncRunningVm(parameters.getVmId());
        } else if (command.getVDSReturnValue().getExceptionObject() != null) {
            log.error("VDS::pause Failed resume VM '{}' in VDS = '{}' error = '{}'", parameters
                    .getVmId(), getParameters().getVdsId(), command.getVDSReturnValue()
                    .getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(command.getVDSReturnValue().getExceptionString());
            getVDSReturnValue().setExceptionObject(command.getVDSReturnValue().getExceptionObject());
            getVDSReturnValue().setVdsError(command.getVDSReturnValue().getVdsError());
        }
        getVDSReturnValue().setReturnValue(retval);
    }
}
