package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmLogonVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class VmLogonVDSCommand<P extends VmLogonVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid vmId = Guid.Empty;
    private String domain;
    private String userName;
    private String password;

    public VmLogonVDSCommand(P parameters) {
        super(parameters);
        vmId = parameters.getVmId();
        domain = parameters.getDomain();
        userName = parameters.getUserName();
        if (parameters.getUserName().contains("@")) {
            userName = parameters.getUserName().substring(0, parameters.getUserName().indexOf('@'));
        }
        password = (parameters.getPassword() != null) ? parameters.getPassword() : "";
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().desktopLogin(vmId.toString(), domain, userName, password);
        proceedProxyReturnValue();
    }
}
