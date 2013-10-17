package org.ovirt.engine.core.common.action;

public class AddInternalJobParameters extends AddJobParameters {

    private static final long serialVersionUID = -7824725232199970355L;
    private VdcActionType actionType;

    public AddInternalJobParameters() {
    }

    public AddInternalJobParameters(VdcActionType actionType, boolean isAutoCleared) {
        super();
        this.actionType = actionType;
        this.isAutoCleared = isAutoCleared;
    }

    public AddInternalJobParameters(String description, VdcActionType actionType, boolean isAutoCleared) {
        super();
        this.description = description;
        this.actionType = actionType;
        this.isAutoCleared = isAutoCleared;
    }

    public VdcActionType getActionType() {
        return actionType;
    }

    public void setActionType(VdcActionType actionType) {
        this.actionType = actionType;
    }

}
