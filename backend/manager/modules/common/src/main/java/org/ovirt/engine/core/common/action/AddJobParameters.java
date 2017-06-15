package org.ovirt.engine.core.common.action;

public class AddJobParameters extends ActionParametersBase {

    private static final long serialVersionUID = -7319747815339126821L;
    protected String description;
    protected boolean isAutoCleared;

    public AddJobParameters() {
        super();
    }

    public AddJobParameters(String description, boolean isAutoCleared) {
        super();
        this.description = description;
        this.isAutoCleared = isAutoCleared;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAutoCleared() {
        return isAutoCleared;
    }

    public void setAutoCleared(boolean isAutoCleared) {
        this.isAutoCleared = isAutoCleared;
    }

}
