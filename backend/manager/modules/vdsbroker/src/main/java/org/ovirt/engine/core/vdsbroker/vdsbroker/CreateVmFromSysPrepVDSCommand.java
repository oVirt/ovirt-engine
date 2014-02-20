package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;


public class CreateVmFromSysPrepVDSCommand<P extends CreateVmVDSCommandParameters>
        extends CreateVDSCommand<P> {
    public CreateVmFromSysPrepVDSCommand(P parameters) {
        super(parameters);
        String sysPrepContent =
                SysprepHandler.getSysPrep(parameters.getVm(),
                        parameters.getSysPrepParams());

        if (!"".equals(sysPrepContent)) {
            builder.buildSysprepVmPayload(sysPrepContent);
        }
    }
}
