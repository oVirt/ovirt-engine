package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
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

    private Guid vdsRunningOn;

    private int executionIndex;

    private EndProcedure endProcedure = EndProcedure.PARENT_MANAGED;

    private boolean useCinderCommandCallback;

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
    private Integer lifeInMinutes;

    public VdcActionParametersBase() {
        shouldbelogged = true;
        transctionOption = TransactionScopeOption.Required;
        setTaskGroupSuccess(true);
        setParentCommand(VdcActionType.Unknown);
        executionReason = CommandExecutionReason.REGULAR_FLOW;
        compensationEnabled = false;
        parentCommand = VdcActionType.Unknown;
        commandType = VdcActionType.Unknown;
        imagesParameters = new ArrayList<>();
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

    public Guid getVdsRunningOn() {
        return vdsRunningOn;
    }

    public void setVdsRunningOn(Guid vdsRunningOn) {
        this.vdsRunningOn = vdsRunningOn;
    }

    public Integer getLifeInMinutes() {
        return lifeInMinutes;
    }

    public void setLifeInMinutes(Integer lifeInMinutes) {
        this.lifeInMinutes = lifeInMinutes;
    }

    public boolean isUseCinderCommandCallback() {
        return useCinderCommandCallback;
    }

    public void setUseCinderCommandCallback(boolean useCinderCommandCallback) {
        this.useCinderCommandCallback = useCinderCommandCallback;
    }

    /**
     * Enum for determining the execution reason of the command.
     */
    public enum CommandExecutionReason {
        REGULAR_FLOW,
        ROLLBACK_FLOW;
    }

    /**
     * Enum for determining how the command should end when it has a parent command
     */
    public enum EndProcedure {
        /**
         * The command should be ended soon as its async operations end
         */
        COMMAND_MANAGED,
        /**
         * The command should be ended by its parent command
         */
        PARENT_MANAGED,
        /**
         * For internal use only - indicates that the command end method execution is currently
         * used by the different internal flows (like the retry flow) and should be executed as soon as its called
         */
        FLOW_MANAGED
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb.append("commandId", getCommandId())
                .append("user", getParametersCurrentUser() == null ? null : getParametersCurrentUser().getLoginName())
                .append("commandType", getCommandType());
    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }

    public EndProcedure getEndProcedure() {
        return endProcedure;
    }

    public void setEndProcedure(EndProcedure endProcedure) {
        this.endProcedure = endProcedure;
    }
}
