package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RunVmOnceParams extends RunVmParams {

    private static final long serialVersionUID = -4968552684343593622L;

    private String sysPrepDomainName;

    private String sysPrepUserName;

    private String sysPrepPassword;

    public RunVmOnceParams() {
    }

    public RunVmOnceParams(Guid vmId) {
        super(vmId);
    }

    public RunVmOnceParams(Guid vmId, boolean isInternal) {
        super(vmId, isInternal);
    }

    public void setSysPrepDomainName(String sysPrepDomainName) {
        this.sysPrepDomainName = sysPrepDomainName;
    }

    public String getSysPrepDomainName() {
        return sysPrepDomainName;
    }

    public void setSysPrepUserName(String sysPrepUserName) {
        this.sysPrepUserName = sysPrepUserName;
    }

    public String getSysPrepUserName() {
        return sysPrepUserName;
    }

    public void setSysPrepPassword(String sysPrepPassword) {
        this.sysPrepPassword = sysPrepPassword;
    }

    public String getSysPrepPassword() {
        return sysPrepPassword;
    }

}
