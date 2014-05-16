package org.ovirt.engine.core.common.action;

import java.io.Serializable;

public class SysPrepParams implements Serializable {

    private static final long serialVersionUID = -139795494679460456L;

    private String sysPrepDomainName;
    private String sysPrepUserName;
    private String sysPrepPassword;

    public SysPrepParams() {
    }

    public SysPrepParams(String sysPrepDomainName, String sysPrepUserName, String sysPrepPassword) {
        this.sysPrepDomainName = sysPrepDomainName;
        this.sysPrepUserName = sysPrepUserName;
        this.sysPrepPassword = sysPrepPassword;
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

    @ShouldNotBeLogged
    public String getSysPrepPassword() {
        return sysPrepPassword;
    }
}
