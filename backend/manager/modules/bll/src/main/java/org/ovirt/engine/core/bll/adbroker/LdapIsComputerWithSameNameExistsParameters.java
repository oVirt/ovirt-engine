package org.ovirt.engine.core.bll.adbroker;

public class LdapIsComputerWithSameNameExistsParameters extends LdapUserPasswordBaseParameters {
    private String privateComputerName;

    public String getComputerName() {
        return privateComputerName;
    }

    private void setComputerName(String value) {
        privateComputerName = value;
    }

    public LdapIsComputerWithSameNameExistsParameters(String domain, String username, String password, String computerName) {
        super(domain, username, password);
        setComputerName(computerName);
    }
}
