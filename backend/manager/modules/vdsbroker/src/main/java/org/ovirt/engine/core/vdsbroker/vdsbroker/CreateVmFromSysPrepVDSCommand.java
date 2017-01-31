package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;


public class CreateVmFromSysPrepVDSCommand<P extends CreateVDSCommandParameters>
        extends CreateBrokerVDSCommand<P> {
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
