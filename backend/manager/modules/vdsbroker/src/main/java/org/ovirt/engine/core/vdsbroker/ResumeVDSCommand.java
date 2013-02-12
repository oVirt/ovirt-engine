package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResumeBrokerVDSCommand;

public class ResumeVDSCommand<P extends ResumeVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public ResumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        ResumeVDSCommandParameters parameters = getParameters();
        if (_vdsManager != null) {
            VMStatus retval = VMStatus.Unknown;
            ResumeBrokerVDSCommand<VdsAndVmIDVDSParametersBase> command =
                    new ResumeBrokerVDSCommand<VdsAndVmIDVDSParametersBase>(parameters);
            command.execute();
            if (command.getVDSReturnValue().getSucceeded()) {
                retval = VMStatus.PoweringUp;
                ResourceManager.getInstance().AddAsyncRunningVm(parameters.getVmId());
            } else if (command.getVDSReturnValue().getExceptionObject() != null) {
                log.errorFormat("VDS::pause Failed resume vm '{0}' in vds = {1} : {2}, error = {3}", parameters
                        .getVmId(), getVds().getId(), getVds().getName(), command.getVDSReturnValue()
                        .getExceptionString());
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setExceptionString(command.getVDSReturnValue().getExceptionString());
                getVDSReturnValue().setExceptionObject(command.getVDSReturnValue().getExceptionObject());
                getVDSReturnValue().setVdsError(command.getVDSReturnValue().getVdsError());
            }
            getVDSReturnValue().setReturnValue(retval);
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    private static Log log = LogFactory.getLog(ResumeVDSCommand.class);
}
