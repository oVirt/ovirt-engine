package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public abstract class BrokerCommandBase {
    protected abstract String getPROTOCOL();

    protected LdapReturnValueBase _ldapReturnValue = new LdapReturnValueBase();
    private LdapBrokerBaseParameters _parameters;

    protected Object getReturnValue() {
        return _ldapReturnValue.getReturnValue();
    }

    protected void setReturnValue(Object value) {
        _ldapReturnValue.setReturnValue(value);
    }

    protected boolean getSucceeded() {
        return _ldapReturnValue.getSucceeded();
    }

    protected void setSucceeded(boolean value) {
        _ldapReturnValue.setSucceeded(value);
    }

    protected LdapBrokerBaseParameters getParameters() {
        return _parameters;
    }

    protected String getDomain() {
        return getParameters().getDomain();
    }

    protected void setDomain(String value) {
        getParameters().setDomain(value);
    }

    protected String getAuthenticationDomain() {
        return getParameters().getAuthenticationDomain();
    }

    protected void setAuthenticationDomain(String value) {
        getParameters().setAuthenticationDomain(value);
    }

    protected String getLoginName() {
        return getParameters().getLoginName();
    }

    protected void setLoginName(String value) {
        getParameters().setLoginName(value);
    }

    protected String getPassword() {
        return getParameters().getPassword();
    }

    protected void setPassword(String value) {
        getParameters().setPassword(value);
    }

    protected BrokerCommandBase(LdapBrokerBaseParameters parameters) {
        _parameters = parameters;
    }

    public abstract LdapReturnValueBase execute();
}
