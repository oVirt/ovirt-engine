package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmLogonVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String _domain;
    private String _password;
    private String _userName;

    public VmLogonVDSCommandParameters(Guid vdsId, Guid vmId, String domain, String userName, String password) {
        super(vdsId, vmId);
        _domain = domain;
        _password = password;
        _userName = userName;
    }

    public String getDomain() {
        return _domain;
    }

    public String getPassword() {
        return _password;
    }

    public String getUserName() {
        return _userName;
    }

    public VmLogonVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("domain", getDomain())
                .appendFiltered("password", getPassword())
                .append("userName", getUserName());
    }
}
