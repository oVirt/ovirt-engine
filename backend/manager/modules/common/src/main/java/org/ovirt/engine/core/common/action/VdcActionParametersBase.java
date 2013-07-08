package org.ovirt.engine.core.common.action;

import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.PreRun;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public class VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 4872560145516614773L;

    private Guid commandId;
    private transient String sessionid;
    private boolean shouldbelogged;
    private VdcUser parametersCurrentUser;
    private TransactionScopeOption transctionOption;

    private transient CommandExecutionReason executionReason = CommandExecutionReason.REGULAR_FLOW;

    /**
     * Indicates if the command should use the compensation mechanism or not.
     */
    private boolean compensationEnabled = false;

    private VdcActionType parentCommand = VdcActionType.Unknown;

    /**
     * Used to determine the correct command to construct for these parameters.
     */
    private VdcActionType commandType = VdcActionType.Unknown;

    private transient VdcActionParametersBase parentParameters;
    // this flag marks if the command ran with MultipleAction for ProcessExceptionToClient
    private boolean multipleAction;

    private EntityInfo entityInfo;

    private List<VdcActionParametersBase> imagesParameters;

    private boolean taskGroupSuccess = true;

    private List<Guid> vdsmTaskIds;

    private int executionIndex = 0;

    /**
     * A cross system identifier of the executed action
     */
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS, message = "VALIDATION_INVALID_CORRELATION_ID",
            groups = PreRun.class)
    @Size(min = 1, max = BusinessEntitiesDefinitions.CORRELATION_ID_SIZE, groups = PreRun.class)
    private String correlationId;

    private Guid jobId = null;
    private Guid stepId = null;

    public VdcActionParametersBase() {
        shouldbelogged = true;
        transctionOption = TransactionScopeOption.Required;
        setTaskGroupSuccess(true);
        setParentCommand(VdcActionType.Unknown);
    }

    public Guid getCommandId() {
        return commandId;
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    public String getSessionId() {
        if (sessionid == null) {
            sessionid = "";
        }
        return sessionid;
    }

    public void setSessionId(String value) {
        sessionid = value;
    }

    public VdcUser getParametersCurrentUser() {
        return parametersCurrentUser;
    }


    public void setParametersCurrentUser(VdcUser value) {
        parametersCurrentUser = value;
    }

    public boolean getShouldBeLogged() {
        return shouldbelogged;
    }

    public void setShouldBeLogged(boolean value) {
        shouldbelogged = value;
    }

    public CommandExecutionReason getExecutionReason() {
        return executionReason;
    }

    public void setExecutionReason(CommandExecutionReason executionReason) {
        this.executionReason = executionReason;
    }

    public TransactionScopeOption getTransactionScopeOption() {
        return transctionOption;
    }

    public void setTransactionScopeOption(TransactionScopeOption value) {
        transctionOption = value;
    }

    public boolean isCompensationEnabled() {
        return compensationEnabled;
    }

    public void setCompensationEnabled(boolean compensationEnabled) {
        this.compensationEnabled = compensationEnabled;
    }

    public VdcActionType getParentCommand() {
        return parentCommand;
    }

    public void setParentCommand(VdcActionType value) {
        parentCommand = value;
    }

    public VdcActionType getCommandType() {
        return commandType;
    }

    public void setCommandType(VdcActionType commandType) {
        this.commandType = commandType;
    }

    public VdcActionParametersBase getParentParameters() {
        return parentParameters;
    }

    public void setParentParameters (VdcActionParametersBase parameters) {
        parentParameters = parameters;
    }

    public boolean getMultipleAction() {
        return multipleAction;
    }

    public void setMultipleAction(boolean value) {
        multipleAction = value;
    }

    public List<VdcActionParametersBase> getImagesParameters() {
        return imagesParameters;
    }

    public void setImagesParameters(List<VdcActionParametersBase> value) {
        imagesParameters = value;
    }

    public boolean getTaskGroupSuccess() {
        boolean childrenTasksSuccess = taskGroupSuccess;
        if (imagesParameters != null) {
            for (VdcActionParametersBase childParameters : imagesParameters) {
                childrenTasksSuccess &= childParameters.getTaskGroupSuccess();

                if (!childrenTasksSuccess) {
                    break;
                }
            }
        }

        return childrenTasksSuccess;
    }

    public void setTaskGroupSuccess(boolean value) {
        taskGroupSuccess = value;
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    public void setEntityInfo(EntityInfo value) {
        entityInfo = value;
    }

    public List<Guid> getVdsmTaskIds() {
        return vdsmTaskIds;
    }

    public void setVdsmTaskIds(List<Guid> value) {
        vdsmTaskIds = value;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public int getExecutionIndex() {
        return executionIndex;
    }

    public void setExecutionIndex(int executionIndex) {
        this.executionIndex = executionIndex;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getStepId() {
        return stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

    public void incrementExecutionIndex() {
        executionIndex++;
    }

    public void decrementExecutionIndex() {
        executionIndex--;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((imagesParameters == null) ? 0 : imagesParameters.hashCode());
        result = prime * result + (shouldbelogged ? 1231 : 1237);
        result = prime * result + ((transctionOption == null) ? 0 : transctionOption.hashCode());
        result = prime * result + ((entityInfo == null) ? 0 : entityInfo.hashCode());
        result = prime * result + (multipleAction ? 1231 : 1237);
        result = prime * result + ((parametersCurrentUser == null) ? 0 : parametersCurrentUser.hashCode());
        result = prime * result + ((parentCommand == null) ? 0 : parentCommand.hashCode());
        result = prime * result + ((vdsmTaskIds == null) ? 0 : vdsmTaskIds.hashCode());
        result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());
        result = prime * result + executionIndex;
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        result = prime * result + ((stepId == null) ? 0 : stepId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VdcActionParametersBase other = (VdcActionParametersBase) obj;
        if (imagesParameters == null) {
            if (other.imagesParameters != null)
                return false;
        } else if (!imagesParameters.equals(other.imagesParameters))
            return false;
        if (shouldbelogged != other.shouldbelogged)
            return false;
        if (transctionOption != other.transctionOption)
            return false;
        if (entityInfo == null) {
            if (other.entityInfo != null)
                return false;
        } else if (!entityInfo.equals(other.entityInfo))
            return false;
        if (multipleAction != other.multipleAction)
            return false;
        if (parametersCurrentUser == null) {
            if (other.parametersCurrentUser != null)
                return false;
        } else if (!parametersCurrentUser.equals(other.parametersCurrentUser))
            return false;
        if (parentCommand != other.parentCommand)
            return false;
        if (vdsmTaskIds == null) {
            if (other.vdsmTaskIds != null)
                return false;
        } else if (!vdsmTaskIds.equals(other.vdsmTaskIds))
            return false;
        if (correlationId == null) {
            if (other.correlationId != null)
                return false;
        } else if (!correlationId.equals(other.correlationId))
            return false;
        if (executionIndex != other.executionIndex)
            return false;
        if (jobId == null) {
            if (other.jobId != null)
                return false;
        } else if (!jobId.equals(other.jobId))
            return false;
        if (stepId == null) {
            if (other.stepId != null)
                return false;
        } else if (!stepId.equals(other.stepId))
            return false;

        return true;
    }

    /**
     * Enum for determining the execution reason of the command.
     */
    public enum CommandExecutionReason {
        REGULAR_FLOW,
        ROLLBACK_FLOW;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(50);
        builder.append("commandId: "); //$NON-NLS-1$
        builder.append(getCommandId());
        builder.append(", user: "); //$NON-NLS-1$
        if(getParametersCurrentUser() != null) {
            builder.append(getParametersCurrentUser().getUserName());
        }
        builder.append(", commandType: "); //$NON-NLS-1$
        builder.append(getCommandType());
        return builder.toString();
    }
}
