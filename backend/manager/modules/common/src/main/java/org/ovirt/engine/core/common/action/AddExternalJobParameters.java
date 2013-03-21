package org.ovirt.engine.core.common.action;


public class AddExternalJobParameters extends VdcActionParametersBase{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String description;
    private boolean isAutoCleared = true;

    public AddExternalJobParameters(String description, boolean isAutoCleared) {
        super();
        this.description = description;
        this.isAutoCleared = isAutoCleared;
    }

    public AddExternalJobParameters(String description) {
        super();
        this.description = description;
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
