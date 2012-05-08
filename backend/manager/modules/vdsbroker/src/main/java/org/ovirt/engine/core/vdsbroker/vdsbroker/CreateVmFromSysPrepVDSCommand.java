package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.StringHelper;

public class CreateVmFromSysPrepVDSCommand<P extends CreateVmFromSysPrepVDSCommandParameters>
        extends CreateVDSCommand<P> {
    public CreateVmFromSysPrepVDSCommand(P parameters) {
        super(parameters);
        String sysPrepContent =
                SysprepHandler.GetSysPrep(parameters.getVm(),
                        parameters.getHostName(),
                        parameters.getDomain(),
                        parameters.getSysPrepParams());

        if (!StringHelper.EqOp(sysPrepContent, "")) {
            builder.buildSysprepVmPayload(sysPrepContent);
        }
    }
}
