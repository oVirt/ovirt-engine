package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class CreateVmFromSysPrepVDSCommandParameters extends CreateVmVDSCommandParameters {
    private String _hostName;
    private String _domain;

    public CreateVmFromSysPrepVDSCommandParameters(Guid vdsId, VM vm, String hostName, String domain) {
        super(vdsId, vm);
        _hostName = hostName;
        _domain = domain;
    }

    public String getHostName() {
        return _hostName;
    }

    public String getDomain() {
        return _domain;
    }

    @Override
    public String toString() {
        return String.format("%s, hostName=%s, domain=%s", super.toString(), getHostName(), getDomain());
    }
}
