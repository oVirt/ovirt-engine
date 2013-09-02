package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmLogonVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class VmLogonVDSCommand<P extends VmLogonVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = Guid.Empty;
    private String mDomain;
    private String mUserName;
    private String mPassword;

    public VmLogonVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
        mDomain = parameters.getDomain();
        mUserName = parameters.getUserName();
        if (parameters.getUserName().contains("@")) {
            mUserName = parameters.getUserName().substring(0, parameters.getUserName().indexOf('@'));
        }
        mPassword = (parameters.getPassword() != null) ? parameters.getPassword() : "";
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().desktopLogin(mVmId.toString(), mDomain, mUserName, mPassword);
        proceedProxyReturnValue();
    }
}
