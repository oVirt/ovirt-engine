package org.ovirt.engine.core.bll.adbroker;

public class LdapSearchByUserNameParameters extends LdapBrokerBaseParameters {
    private String privateUserName;

    public String getUserName() {
        return privateUserName;
    }

    private void setUserName(String value) {
        privateUserName = value;
    }

    public LdapSearchByUserNameParameters(String sessionId, String domain, String userName) {
        super(sessionId, domain);
        setUserName(userName);
    }
}
