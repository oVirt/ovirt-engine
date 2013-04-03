package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.job.JobExecutionStatus;
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
    private JobExecutionStatus status;

    public Guid getParentId() {
        return parentId;
    }

    public void setParentId(Guid id) {
        this.parentId = id;
    }

    public JobExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(JobExecutionStatus status) {
        this.status = status;
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

    public AddExternalStepParameters(Guid id, String description, StepEnum stepType, JobExecutionStatus status) {
        super();
        this.parentId = id;
        this.description = description;
        this.stepType = stepType;
        this.status = status;
        this.setParentId(id);
    }
}
