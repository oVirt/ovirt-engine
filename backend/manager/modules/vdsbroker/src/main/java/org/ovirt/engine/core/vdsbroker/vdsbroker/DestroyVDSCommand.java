package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;

public class DestroyVDSCommand<P extends DestroyVmVDSCommandParameters> extends VdsBrokerCommand<P> {
    public DestroyVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        if (getParameters().getGracefully()) {
            status = getBroker().shutdown(getParameters().getVmId().toString(),
                    (new Integer(getParameters().getSecondsToWait())).toString(),
                    Config.<String> GetValue(ConfigValues.VmGracefulShutdownMessage));
        } else {
            status = getBroker().destroy(getParameters().getVmId().toString());
        }
        ProceedProxyReturnValue();
    }
}
