package org.ovirt.engine.ui.genericapi.returnvalues;

import org.ovirt.engine.core.common.queries.ValueObject;

public class UIQueryReturnValue {

    private Object returnValue;
    private boolean succeeded;

    public ValueObject getSerializaedReturnValue() {
        return ValueObject.createValueObject(returnValue);
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean value) {
        succeeded = value;
    }
}
