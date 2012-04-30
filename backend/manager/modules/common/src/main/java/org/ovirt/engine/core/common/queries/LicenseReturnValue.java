package org.ovirt.engine.core.common.queries;

public class LicenseReturnValue extends VdcQueryReturnValue {
    private static final long serialVersionUID = 7270804103951142563L;

    private java.util.ArrayList<String> _messages;

    public java.util.ArrayList<String> getMessages() {
        return _messages;
    }

    public void setMessages(java.util.ArrayList<String> value) {
        _messages = value;
    }

    public LicenseReturnValue() {
    }
}
