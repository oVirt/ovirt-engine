package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.ResumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
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
            command.Execute();
            if (command.getVDSReturnValue().getSucceeded()) {
                retval = VMStatus.PoweringUp;
                // get vdsEventListener from callback channel (if wcf-user
                // backend) or resource manager
                if (ResourceManager.getInstance().getBackendCallback() != null) {
                    ResourceManager.getInstance().AddAsyncRunningVm(parameters.getVmId(),
                            ResourceManager.getInstance().getBackendCallback());
                }
            } else if (command.getVDSReturnValue().getExceptionObject() != null) {
                log.errorFormat("VDS::pause Failed resume vm '{0}' in vds = {1} : {2}, error = {3}", parameters
                        .getVmId(), getVds().getvds_id(), getVds().getvds_name(), command.getVDSReturnValue()
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

    private static LogCompat log = LogFactoryCompat.getLog(ResumeVDSCommand.class);
}
