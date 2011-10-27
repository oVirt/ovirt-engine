package org.ovirt.engine.core.bll.adbroker;

public class LdapCreateComputerAccountParameters extends LdapUserPasswordBaseParameters {
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    private void setPath(String value) {
        privatePath = value;
    }

    private String privateComputerName;

    public String getComputerName() {
        return privateComputerName;
    }

    private void setComputerName(String value) {
        privateComputerName = value;
    }

    public LdapCreateComputerAccountParameters(String domain, String username, String password, String path,
                                             String computerName) {
        super(domain, username, password);
        setPath(path);
        setComputerName(computerName);
    }
}
