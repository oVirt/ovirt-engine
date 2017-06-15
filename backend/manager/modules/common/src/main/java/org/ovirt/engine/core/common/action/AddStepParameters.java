package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;

public class AddStepParameters extends ActionParametersBase {

    private static final long serialVersionUID = 2098671430609247442L;

    protected Guid parentId;
    protected String description;
    protected StepEnum stepType;

    public AddStepParameters() {
        super();
    }

    public AddStepParameters(Guid parentId, String description, StepEnum stepType) {
        super();
        this.parentId = parentId;
        this.description = description;
        this.stepType = stepType;
    }

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

}
