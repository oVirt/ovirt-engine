package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;

public class AddExternalStepParameters extends VdcActionParametersBase{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Guid parentId;
    private String description;
    private StepEnum stepType;

    public Guid getParentId() {
        return parentId;
    }

    public void setParentId(Guid id) {
        this.parentId = id;
    }

    public StepEnum getStepType() {
        return stepType;
    }

    public void setStepType(StepEnum stepType) {
        this.stepType = stepType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AddExternalStepParameters(Guid id, String description, StepEnum stepType) {
        super();
        this.parentId = id;
        this.description = description;
        this.stepType = stepType;
        this.setParentId(id);
    }
}
