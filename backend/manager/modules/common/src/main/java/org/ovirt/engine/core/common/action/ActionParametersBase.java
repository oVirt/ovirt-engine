package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.HasCorrelationId;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.PreRun;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public class ActionParametersBase implements Serializable, HasCorrelationId {
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

    /**
     * Indicates if the command is set with END_COMMAND annotation.
     */
    private boolean compensationPhaseEndCommand;

    private ActionType parentCommand;

    /**
     * Used to determine the correct command to construct for these parameters.
     */
    private ActionType commandType;

    private transient ActionParametersBase parentParameters;
    // this flag marks if the command ran with MultipleAction for ProcessExceptionToClient
    private boolean multipleAction;

    private EntityInfo entityInfo;

    // no interface here - concrete types are recommended for GWT serialization
    private ArrayList<ActionParametersBase> imagesParameters;

    private boolean taskGroupSuccess;

    // no interface here - concrete types are recommended for GWT serialization
    private ArrayList<Guid> vdsmTaskIds;

    private Guid vdsRunningOn;

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

    /**
     * The weight of the command in the job (if present)
     */
    private Integer jobWeight;

    /**
     * The weight of each operation performed by the command in the job. For example - if a command is executing few
     * child commands, it may calculate the weight of each child command and when executing it pass the weight in the
     * jobWeight parameter (if present).
     */
    private Map<String, Integer> operationsJobWeight = Collections.emptyMap();

    private LockProperties lockProperties;
    private Integer lifeInMinutes;
    private VDSStatus prevVdsStatus;

    public ActionParametersBase() {
        shouldbelogged = true;
        transctionOption = TransactionScopeOption.Required;
        setTaskGroupSuccess(true);
        setParentCommand(ActionType.Unknown);
        executionReason = CommandExecutionReason.REGULAR_FLOW;
        compensationEnabled = false;
        compensationPhaseEndCommand = false;
        parentCommand = ActionType.Unknown;
        commandType = ActionType.Unknown;
        imagesParameters = new ArrayList<>();
        prevVdsStatus = VDSStatus.Unassigned;
    }

    public ActionParametersBase(String engineSessionId) {
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

    public boolean isCompensationPhaseEndCommand() {
        return compensationPhaseEndCommand;
    }

    public void setCompensationPhaseEndCommand(boolean compensationPhaseEndCommand) {
        this.compensationPhaseEndCommand = compensationPhaseEndCommand;
    }

    public ActionType getParentCommand() {
        return parentCommand;
    }

    public void setParentCommand(ActionType value) {
        parentCommand = value;
    }

    public ActionType getCommandType() {
        return commandType;
    }

    public void setCommandType(ActionType commandType) {
        this.commandType = commandType;
    }

    public ActionParametersBase getParentParameters() {
        return parentParameters;
    }

    public void setParentParameters(ActionParametersBase parameters) {
        parentParameters = parameters;
    }

    public boolean getMultipleAction() {
        return multipleAction;
    }

    public void setMultipleAction(boolean value) {
        multipleAction = value;
    }

    public ArrayList<ActionParametersBase> getImagesParameters() {
        return imagesParameters;
    }

    public void setImagesParameters(ArrayList<ActionParametersBase> value) {
        imagesParameters = value;
    }

    public boolean getTaskGroupSuccess() {
        boolean childrenTasksSuccess = taskGroupSuccess;
        if (imagesParameters != null) {
            for (ActionParametersBase childParameters : imagesParameters) {
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

    public ArrayList<Guid> getVdsmTaskIds() {
        return vdsmTaskIds;
    }

    public void setVdsmTaskIds(ArrayList<Guid> value) {
        vdsmTaskIds = value;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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

    public VDSStatus getPrevVdsStatus() {
        return prevVdsStatus;
    }

    public void setPrevVdsStatus(VDSStatus prevVdsStatus) {
        this.prevVdsStatus = prevVdsStatus;
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

    public Integer getJobWeight() {
        return jobWeight;
    }

    public void setJobWeight(Integer jobWeight) {
        this.jobWeight = jobWeight;
    }

    public Map<String, Integer> getOperationsJobWeight() {
        return operationsJobWeight;
    }

    public void setOperationsJobWeight(Map<String, Integer> operationsJobWeight) {
        this.operationsJobWeight = operationsJobWeight;
    }
}
