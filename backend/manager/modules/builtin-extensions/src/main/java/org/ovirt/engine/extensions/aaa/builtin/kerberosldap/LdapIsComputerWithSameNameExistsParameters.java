package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

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
