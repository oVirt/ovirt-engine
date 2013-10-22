package org.ovirt.engine.core.common.action;

public class AddExternalJobParameters extends AddJobParameters {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AddExternalJobParameters() {
    }

    public AddExternalJobParameters(String description, boolean isAutoCleared) {
        super(description, isAutoCleared);
    }

    public AddExternalJobParameters(String description) {
        super(description, true);
    }

}
