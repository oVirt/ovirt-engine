package org.ovirt.engine.core.bll.adbroker;

public class LdapChangeUserPasswordParameters extends LdapBrokerBaseParameters {
    private String privateDestinationUserName;

    public String getDestinationUserName() {
        return privateDestinationUserName;
    }

    private void setDestinationUserName(String value) {
        privateDestinationUserName = value;
    }

    private String privateDestinationUserPassword;

    public String getDestinationUserPassword() {
        return privateDestinationUserPassword;
    }

    private void setDestinationUserPassword(String value) {
        privateDestinationUserPassword = value;
    }

    private String privateDestinationUserNewPassword;

    public String getDestinationUserNewPassword() {
        return privateDestinationUserNewPassword;
    }

    private void setDestinationUserNewPassword(String value) {
        privateDestinationUserNewPassword = value;
    }

    public LdapChangeUserPasswordParameters(String domain, String destinationUserName, String destinationUserPassword,
                                          String destinationUserNewPassword) {
        super(domain);
        setDestinationUserName(destinationUserName);
        setDestinationUserPassword(destinationUserPassword);
        setDestinationUserNewPassword(destinationUserNewPassword);
    }
}
