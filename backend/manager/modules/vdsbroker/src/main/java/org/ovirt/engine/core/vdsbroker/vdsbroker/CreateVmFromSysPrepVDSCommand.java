package org.ovirt.engine.core.vdsbroker.vdsbroker;


public class CreateVmFromSysPrepVDSCommand<P extends CreateVmFromSysPrepVDSCommandParameters>
        extends CreateVDSCommand<P> {
    public CreateVmFromSysPrepVDSCommand(P parameters) {
        super(parameters);
        String sysPrepContent =
                SysprepHandler.getSysPrep(parameters.getVm(),
                        parameters.getHostName(),
                        parameters.getDomain(),
                        parameters.getSysPrepParams());

        if (!"".equals(sysPrepContent)) {
            builder.buildSysprepVmPayload(sysPrepContent);
        }
    }
}
