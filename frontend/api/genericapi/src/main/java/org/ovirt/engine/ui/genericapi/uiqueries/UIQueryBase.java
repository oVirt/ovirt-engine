package org.ovirt.engine.ui.genericapi.uiqueries;

import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;
import org.ovirt.engine.ui.genericapi.returnvalues.UIQueryReturnValue;

public abstract class UIQueryBase {

    protected UIQueryParametersBase parameters;
    protected UIQueryReturnValue returnValue;

    public UIQueryBase(UIQueryParametersBase parameters) {
        this.parameters = parameters;
        returnValue = new UIQueryReturnValue();
    }

    public void executeQuery() {
        try {
            runQuery();
            returnValue.setSucceeded(true);
        } catch (RuntimeException ex) {
            // TODO: Log ex.getMessage()
        }
    }

    public abstract void runQuery();

    public UIQueryParametersBase getParameters() {
        return parameters;
    }

    public void setParameters(UIQueryParametersBase parameters) {
        this.parameters = parameters;
    }

    public UIQueryReturnValue getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(UIQueryReturnValue returnValue) {
        this.returnValue = returnValue;
    }
}
