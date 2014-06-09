package org.ovirt.engine.core.common.action;

public class SetDataOnSessionParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = -4340219620005637644L;
    private String key;
    private Object value;

    public SetDataOnSessionParameters() {
    }

    public SetDataOnSessionParameters(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

}
