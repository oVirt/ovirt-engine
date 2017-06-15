package org.ovirt.engine.core.common.action;

import java.util.List;

public class MoveDisksParameters extends ActionParametersBase {
    private static final long serialVersionUID = 2988540138349612909L;

    private List<MoveDiskParameters> parametersList;

    public MoveDisksParameters() {
        // Empty constructor for serializing / deserializing
    }

    public MoveDisksParameters(List<MoveDiskParameters> parametersList) {
        this.parametersList = parametersList;
    }

    public List<MoveDiskParameters> getParametersList() {
        return parametersList;
    }

    public void setParametersList(List<MoveDiskParameters> parametersList) {
        this.parametersList = parametersList;
    }
}
