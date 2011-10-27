package org.ovirt.engine.core.bll.adbroker;

public class LdapReturnValueBase {
    private boolean privateSucceeded;

    public boolean getSucceeded() {
        return privateSucceeded;
    }

    public void setSucceeded(boolean value) {
        privateSucceeded = value;
    }

    private Object privateReturnValue;

    public Object getReturnValue() {
        return privateReturnValue;
    }

    public void setReturnValue(Object value) {
        privateReturnValue = value;
    }
}
