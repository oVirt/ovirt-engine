package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
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
        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(VDSCommandType.ResumeBroker, parameters);
        if (vdsReturnValue.getSucceeded()) {
            retval = VMStatus.PoweringUp;
            resourceManager.AddAsyncRunningVm(parameters.getVmId());
        } else if (vdsReturnValue.getExceptionObject() != null) {
            log.error("VDS::pause Failed resume VM '{}' in VDS = '{}' error = '{}'", parameters
                    .getVmId(), getParameters().getVdsId(), vdsReturnValue.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
        }
        getVDSReturnValue().setReturnValue(retval);
    }
}
