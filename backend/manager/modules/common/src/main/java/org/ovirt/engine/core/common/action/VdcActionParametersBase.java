package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.PreRun;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public class VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 4872560145516614773L;

    private Guid commandId;
    private transient String sessionid;
    private boolean shouldbelogged;
    private DbUser parametersCurrentUser;
    private TransactionScopeOption transctionOption;

    private transient CommandExecutionReason executionReason;

    /**
     * Indicates if the command should use the compensation mechanism or not.
     */
    private boolean compensationEnabled;

    private VdcActionType parentCommand;

    /**
     * Used to determine the correct command to construct for these parameters.
     */
    private VdcActionType commandType;

    private transient VdcActionParametersBase parentParameters;
    // this flag marks if the command ran with MultipleAction for ProcessExceptionToClient
    private boolean multipleAction;

    private EntityInfo entityInfo;

    private List<VdcActionParametersBase> imagesParameters;

    private boolean taskGroupSuccess;

    private List<Guid> vdsmTaskIds;

    private int executionIndex;

    /**
     * A cross system identifier of the executed action
     */
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS, message = "VALIDATION_INVALID_CORRELATION_ID",
            groups = PreRun.class)
    @Size(min = 1, max = BusinessEntitiesDefinitions.CORRELATION_ID_SIZE, groups = PreRun.class)
    private String correlationId;

    private Guid jobId;
    private Guid stepId;

    private LockProperties lockProperties;

    public VdcActionParametersBase() {
        shouldbelogged = true;
        transctionOption = TransactionScopeOption.Required;
        setTaskGroupSuccess(true);
        setParentCommand(VdcActionType.Unknown);
        executionReason = CommandExecutionReason.REGULAR_FLOW;
        compensationEnabled = false;
        parentCommand = VdcActionType.Unknown;
        commandType = VdcActionType.Unknown;
        imagesParameters = new ArrayList<VdcActionParametersBase>();
    }

    public VdcActionParametersBase(String engineSessionId) {
        this();
        sessionid = engineSessionId;
    }

    public void setLockProperties(LockProperties lockProperties) {
        this.lockProperties = lockProperties;
    }

    public LockProperties getLockProperties() {
        return lockProperties;
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

    public DbUser getParametersCurrentUser() {
        return parametersCurrentUser;
    }

    public void setParametersCurrentUser(DbUser value) {
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

    public void setParentParameters(VdcActionParametersBase parameters) {
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
        if (getParametersCurrentUser() != null) {
            builder.append(getParametersCurrentUser().getLoginName());
        }
        builder.append(", commandType: "); //$NON-NLS-1$
        builder.append(getCommandType());
        return builder.toString();
    }
}
