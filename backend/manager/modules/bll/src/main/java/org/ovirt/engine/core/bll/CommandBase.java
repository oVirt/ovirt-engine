package org.ovirt.engine.core.bll;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionRolledbackLocalException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.aaa.SsoSessionUtils;
import org.ovirt.engine.core.bll.context.ChildCompensationWrapper;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.context.DefaultCompensationContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.InvalidQuotaParametersException;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.Command;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.CommandExecutionReason;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.ExecutionMethod;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.PersistedCommandContext;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDao;
import org.ovirt.engine.core.dao.EntityDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockingResult;
import org.ovirt.engine.core.utils.transaction.TransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionRollbackListener;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.woorea.openstack.base.client.OpenStackResponseException;

public abstract class CommandBase<T extends ActionParametersBase>
        extends AuditLogableBase
        implements TransactionMethod<Object>, Command<T> {

    protected static final String SYSTEM_USER_NAME = "SYSTEM";
    private static final String DEFAULT_TASK_KEY = "DEFAULT_TASK_KEY";
    private T parameters;
    private ActionReturnValue returnValue;
    private CommandActionState actionState = CommandActionState.EXECUTE;
    private ActionType actionType;
    private final List<Class<?>> validationGroups = new ArrayList<>();
    private final Guid commandId;
    private boolean quotaChanged = false;
    private String description = "";
    private TransactionScopeOption scope;
    private TransactionScopeOption endActionScope;
    private List<QuotaConsumptionParameter> consumptionParameters;
    protected Map<String, Serializable> commandData;
    private Long sessionSeqId;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    protected LockManager lockManager;

    @Inject
    private QuotaManager quotaManager;

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Inject
    protected BackendInternal backend;

    @Inject
    protected VDSBrokerFrontend vdsBroker;

    @Inject
    protected ExecutionHandler executionHandler;

    @Inject
    private EntityDao entityDao;

    @Inject
    private BusinessEntitySnapshotDao businessEntitySnapshotDao;

    @Inject
    private PermissionDao permissionDao;

    @Inject
    private StepDao stepDao;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    private CommandCompensator compensator;

    @Named
    @Inject
    private Predicate<DbUser> isSystemSuperUserPredicate;

    /** Indicates whether the acquired locks should be released after the execute method or not */
    private boolean releaseLocksAtEndOfExecute = true;

    protected Logger log = LoggerFactory.getLogger(getClass());

    /** The context defines how to monitor the command and handle its compensation */
    private final CommandContext context;

    /** A map contains the properties for describing the job */
    protected Map<String, String> jobProperties;

    private CommandStatus commandStatus = CommandStatus.NOT_STARTED;

    protected CommandBase(T parameters, CommandContext cmdContext) {
        this.context = cmdContext;
        this.commandData = new HashMap<>();
        this.parameters = parameters;

        Guid commandIdFromParameters = parameters.getCommandId();
        if (commandIdFromParameters == null) {
            commandIdFromParameters = Guid.newGuid();
            getParameters().setCommandId(commandIdFromParameters);
        }
        commandId = commandIdFromParameters;
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected CommandBase(Guid commandId) {
        this.context = new CommandContext(new EngineContext());
        this.commandId = commandId;
        this.commandData = new HashMap<>();
    }

    /**
     * @see PostConstruct
     */
    @PostConstruct
    protected final void postConstruct() {
        if (!isCompensationContext()) {
            initCommandBase();
            init();
        } else {
            this.context.withCompensationContext(createDefaultCompensationContext());
        }
    }

    protected void initUser() {
        DbUser user = getSessionDataContainer().getUser(context.getEngineContext().getSessionId(), true);
        if (user != null) {
            setCurrentUser(user);
        }
        if (getSessionDataContainer().getPrincipalName(context.getEngineContext().getSessionId()) == null) {
            // command was most probably executed from Quartz job, so session doesn't contain any user info
            // we need to set username to fake internal user so audit logs will not contain "null@N/A" as username
            setUserName(SYSTEM_USER_NAME);
        } else {
            setUserName(getSessionDataContainer().getUserName(context.getEngineContext().getSessionId()));
        }
    }

    private boolean isCompensationContext() {
        return getParameters() == null;
    }

    /**
     * Initializes CommandBase instance when parameters are passed in constructor (non compensation
     * context instance creation)
     */
    private void initCommandBase() {
        initUser();

        ExecutionContext executionContext = context.getExecutionContext();
        if (executionContext.getJob() != null) {
            setJobId(executionContext.getJob().getId());
        } else if (executionContext.getStep() != null) {
            setJobId(executionContext.getStep().getJobId());
        }

        setCorrelationId(parameters.getCorrelationId());
    }

    /**
     * Implement this method whenever you need extra initialization of the command after the
     * constructor. All DB calls or other interaction with the command dependencies for initialization
     * should be done here. It is ensured that all injected dependencies were injected at the time calling.
     */
    protected void init() {
    }

    /**
     * Checks if possible to perform rollback using command, and if so performs it
     *
     * @param commandType
     *            command type for the rollback
     * @param params
     *            parameters for the rollback
     * @param context
     *            context for the rollback
     * @return result of the command execution
     */
    protected ActionReturnValue attemptRollback(ActionType commandType,
            ActionParametersBase params, CommandContext context) {
        if (canPerformRollbackUsingCommand(commandType, params)) {
            params.setExecutionReason(CommandExecutionReason.ROLLBACK_FLOW);
            params.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            return backend.runInternalAction(commandType, params, context);
        }
        return new ActionReturnValue();
    }

    /**
     * Checks if possible to perform rollback using command, and if so performs it
     *
     * @param commandType
     *            command type for the rollback
     * @param params
     *            parameters for the rollback
     * @param context
     *            context for the rollback
     * @return result of the command execution
     */
    protected ActionReturnValue checkAndPerformRollbackUsingCommand(ActionType commandType,
            ActionParametersBase params, CommandContext context) {
        return attemptRollback(commandType, params, context);
    }

    /**
     * Checks if it is possible to rollback the command using a command (and not VDSM)
     *
     * @param commandType
     *            the rollback command to be executed
     * @param params
     *            parameters for the rollback command
     * @return true if it is possible to run rollback using command
     */
    protected boolean canPerformRollbackUsingCommand
            (ActionType commandType,
                    ActionParametersBase params) {
        return true;
    }

    /**
     * Create an appropriate compensation context. The default is one that does nothing for command that don't run in a
     * transaction, and a real one for commands that run in a transaction.
     *
     * @param transactionScopeOption
     *            The transaction scope.
     * @return The compensation context to use.
     */
    private CompensationContext createCompensationContext(TransactionScopeOption transactionScopeOption,
            boolean forceCompensation) {
        if (transactionScopeOption == TransactionScopeOption.Suppress && !forceCompensation) {
            return NoOpCompensationContext.getInstance();
        }

        setCompensationPhaseEndCommand();
        return createDefaultCompensationContext();
    }

    protected DefaultCompensationContext createDefaultCompensationContext() {
        DefaultCompensationContext defaultContext = new DefaultCompensationContext();
        defaultContext.setCommandId(commandId);
        defaultContext.setCommandType(getClass().getName());
        defaultContext.setBusinessEntitySnapshotDao(businessEntitySnapshotDao);
        defaultContext.setSnapshotSerializer(
                SerializationFactory.getSerializer());
        return defaultContext;
    }

    /**
     * Returns true, if the caller of the command wants the command to use compensation context.
     * In case the command uses transaction, it should also use compensation context.
     */
    protected boolean isCompensationEnabledByCaller() {
        return getParameters().isCompensationEnabled();
    }

    private void setCompensationPhaseEndCommand() {
        getParameters().setCompensationPhaseEndCommand(
                getCommandCompensationPhase() == CommandCompensationPhase.END_COMMAND);
    }

    /**
     * @return the compensationContext
     */
    public CompensationContext getCompensationContext() {
        return context.getCompensationContext();
    }

    /**
     * @param compensationContext the compensationContext to set
     */
    public void setCompensationContext(CompensationContext compensationContext) {
        context.withCompensationContext(compensationContext);
    }

    public CompensationContext getCompensationContextIfEnabledByCaller() {
        return isCompensationEnabledByCaller() ? getCompensationContext() : null;
    }

    /**
     * Validates that the pre-conditions for command execution are met.
     * This method is called internally from the code.
     * <p>
     * In general, each command has its own conditions which should met, in order to expect a valid command execution.
     * An attempt to execute a command which failed to meet all the condition will lead to unpredicted result and should
     * be avoided.
     * <p>
     * The violated condition messages are stored by {@link #addValidationMessage(EngineMessage)} and can be reviewed
     * in {@link ActionReturnValue#getValidationMessages()} retrieved by {@link #getReturnValue()}
     *
     * @return ActionReturnValue A container object for the operation result.
     */
    public ActionReturnValue validateOnly() {
        setActionMessageParameters();
        getReturnValue().setValid(internalValidate());
        String tempVar = getDescription();
        getReturnValue().setDescription((tempVar != null) ? tempVar : getReturnValue().getDescription());
        return returnValue;
    }

    public ActionReturnValue executeAction() {
        getSessionDataContainer().updateSessionLastActiveTime(getParameters().getSessionId());
        determineExecutionReason();
        actionState = CommandActionState.EXECUTE;
        String tempVar = getDescription();
        getReturnValue().setDescription((tempVar != null) ? tempVar : getReturnValue().getDescription());
        setActionMessageParameters();
        Step validatingStep=null;
        boolean actionAllowed;
        boolean isExternal = this.getParameters().getJobId() != null || this.getParameters().getStepId() != null;
        if (!isExternal) {
            validatingStep = executionHandler.addStep(getExecutionContext(), StepEnum.VALIDATING, null);
        }

        try {
            if (parentHasCallback()) {
                persistCommand(getParameters().getParentCommand());
            }

            actionAllowed = getReturnValue().isValid() || internalValidate();

            if (!isExternal) {
                executionHandler.endStep(getExecutionContext(), validatingStep, actionAllowed);
            }

            if (actionAllowed) {
                execute();
            } else {
                getReturnValue().setValid(false);
            }
        } finally {
            updateCommandIfNeeded();
            freeLockExecute();
            clearAsyncTasksWithOutVdsmId();
        }
        return getReturnValue();
    }

    private void clearAsyncTasksWithOutVdsmId() {
        if (!getReturnValue().getTaskPlaceHolderIdList().isEmpty()) {
            TransactionSupport.executeInNewTransaction(() -> {
                for (Guid asyncTaskId : getReturnValue().getTaskPlaceHolderIdList()) {
                    AsyncTask task = commandCoordinatorUtil.getAsyncTaskFromDb(asyncTaskId);
                    if (task != null && Guid.isNullOrEmpty(task.getVdsmTaskId())) {
                        commandCoordinatorUtil.removeTaskFromDbByTaskId(task.getTaskId());
                    }

                }
                return null;
            });
        }
    }

    private void determineExecutionReason() {
        if (getParameters().getExecutionReason() == null) {
            getParameters().setExecutionReason(CommandExecutionReason.REGULAR_FLOW);
        }
    }

    /**
     * Run the default compensation logic (inside a new transaction):<br>
     * <ol>
     * <li>Get all the entity snapshots that this command has created.</li>
     * <li>For each snapshot:</li>
     * <ol>
     * <li>Deserialize the entity.</li>
     * <li>Using the entity Dao:</li>
     * <ul>
     * <li>If the entity was added by the command, remove it.</li>
     * <li>Otherwise, If the entity is not in DB anymore, restore it.</li>
     * <li>Otherwise, update it.</li>
     * </ul>
     * </ol>
     * <li>Remove all the snapshots for this command, since we handled them.</li> </ol>
     */
    @SuppressWarnings({ "unchecked", "synthetic-access" })
    protected void compensate() {
        try {
            if (isQuotaDependant()) {
                rollbackQuota();
            }
        } catch (NullPointerException e) {
            log.debug("RollbackQuota: failed (may be because quota is disabled)", e);
        }
        // If the compensation data is not for the command do not perform compensation.
        if (!commandId.equals(getCompensationContext().getCommandId())) {
            return;
        }

        try {
            compensator.compensate(commandId, getClass().getName(), getCompensationContext());
        } catch (Throwable t) {
            log.error("Unable to compensate command {}: {}", commandId, ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    protected void handleStepsOnEnd() {
        if (getCommandStep() != null && getExecutionContext().getStep() != null) {
            executionHandler.endTaskStep(getExecutionContext().getStep().getId(),
                    isEndSuccessfully() ? JobExecutionStatus.FINISHED : JobExecutionStatus.FAILED);
        }

        if (getCommandStep() == null) {
            executionHandler.startFinalizingStep(getExecutionContext());
        }
    }

    /**
     * Returns whether the command updates the progress on its added {@link Step}.
     * @return boolean
     */
    public boolean shouldUpdateStepProgress() {
        return false;
    }

    @Override
    public ActionReturnValue endAction() {
        boolean shouldEndAction = handleCommandExecutionEnded();
        if (shouldEndAction) {
            handleStepsOnEnd();
            handleChildCommands();
            try {
                initiateLockEndAction();
                setActionState();
                handleTransactivity();
                TransactionSupport.executeInScope(endActionScope, this);
            } catch (TransactionRolledbackLocalException e) {
                log.info("endAction: Transaction was aborted in {}", this.getClass().getName());
            } finally {
                freeLockEndAction();
                endStepsAndJobIfNeeded();
                // NOTE: this update persists updates made during the endSuccessfully()/endWithFailure() execution.
                // The update is done intentionally after the freeLock() call, change with care.
                updateCommandIfNeeded();
                if (getCommandShouldBeLogged()) {
                    logCommand();
                }
            }
        } else {
            getReturnValue().setSucceeded(true);
        }

        return getReturnValue();
    }

    private void endStepsAndJobIfNeeded() {
        //TODO: getEndActionTryAgain() isn't supported currently by the coco infrastructure
        boolean endActionWillRunAgain = !getSucceeded() && getReturnValue().getEndActionTryAgain();
        if (endActionWillRunAgain) {
            return;
        }

        boolean succeeded = getSucceeded() && getParameters().getTaskGroupSuccess();
        executionHandler.endFinalizingStepAndCurrentStep(getContext().getExecutionContext(), succeeded);

        if (!parentHasCallback()) {
            executionHandler.endTaskJobIfNeeded(getContext().getExecutionContext(), getSucceeded() && getParameters()
                    .getTaskGroupSuccess());
        }
    }

    /**
     * The following method should initiate a lock , in order to release it at endAction()
     */
    private void initiateLockEndAction() {
        if (context.getLock() == null) {
            LockProperties lockProperties = getLockProperties();
            if (Scope.Command.equals(lockProperties.getScope())) {
                context.withLock(buildLock());
            }

        }
    }

    private void handleTransactivity() {
        scope =
                (getParameters() != null) ? getParameters().getTransactionScopeOption()
                        : TransactionScopeOption.Required;
        endActionScope = scope;
        boolean forceCompensation = getForceCompensation();
        // @NonTransactiveAttribute annotation overrides the scope passed by the
        // command parameters
        if (!getTransactive()) {
            scope = TransactionScopeOption.Suppress;

            // Set the end action scope to suppress only for non-compensating commands, or the end action for commands
            // will run without transaction but compensation is not supported for end action.
            endActionScope = forceCompensation ? endActionScope : scope;
        }

        if (getCompensationContext() == null) {
            context.withCompensationContext(createCompensationContext(scope, forceCompensation));
        }
    }

    private void setActionState() {
        // This mechanism should change,
        // And for ROLLBACK_FLOW we should
        // introduce a new actionState.
        // Currently it was decided that ROLLBACK_FLOW will cause endWithFailure
        if (isEndSuccessfully()) {
            actionState = CommandActionState.END_SUCCESS;
        } else {
            actionState = CommandActionState.END_FAILURE;
        }
    }

    public void handleChildCommands() {
        if (getCallback() != null) {
            List<Guid> childCommands = commandCoordinatorUtil.getChildCommandIds(getCommandId());
            ArrayList<ActionParametersBase> parameters = new ArrayList<>();
            for (Guid id : childCommands) {
                CommandBase<?> command = commandCoordinatorUtil.retrieveCommand(id);
                if (command.getParameters().getEndProcedure() == EndProcedure.PARENT_MANAGED
                        || command.getParameters().getEndProcedure() == EndProcedure.FLOW_MANAGED) {
                    command.getParameters().setEndProcedure(EndProcedure.FLOW_MANAGED);
                    command.getParameters().setCommandType(command.getActionType());
                    parameters.add(command.getParameters());
                }
            }

            getParameters().setImagesParameters(parameters);
        }
    }

    protected boolean isEndSuccessfully() {
        return getParameters().getTaskGroupSuccess()
                && getParameters().getExecutionReason() == CommandExecutionReason.REGULAR_FLOW;
    }

    private boolean isEndProcedureApplicableToEndAction() {
        return getParameters().getEndProcedure() == EndProcedure.FLOW_MANAGED
                || getParameters().getEndProcedure() == EndProcedure.COMMAND_MANAGED;
    }

    private boolean handleCommandExecutionEnded() {
        boolean shouldEndAction = parentHasCallback() ? isEndProcedureApplicableToEndAction() : true;

        CommandStatus newStatus = isEndSuccessfully() ? CommandStatus.SUCCEEDED : CommandStatus.FAILED;
        if (getCallback() == null) {
            setCommandStatus(newStatus);

            if (!shouldEndAction) {
                logEndWillBeExecutedByParent(newStatus);
            }
        }

        return shouldEndAction;
    }

    public void logEndWillBeExecutedByParent(CommandStatus status) {
        log.info("Command [id={}]: Updating status to '{}', The command end method logic will be executed by one of its parent commands.",
                getCommandId(),
                status);
    }

    public void endActionInTransactionScope() {
        boolean useCompensation = false;
        try {
            if (isEndSuccessfully()) {
                internalEndSuccessfully();
                setCommandStatus(CommandStatus.ENDED_SUCCESSFULLY, false);
            } else {
                internalEndWithFailure();
                setCommandStatus(CommandStatus.ENDED_WITH_FAILURE, false);
                if (getCommandCompensationPhase() == CommandCompensationPhase.END_COMMAND) {
                    useCompensation = true;
                }
            }
        } catch (RuntimeException e) {
            useCompensation = true;
            throw e;
        } finally {
            if (TransactionSupport.current() == null) {
                try {
                    getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
                } catch (RuntimeException e) {
                    logExceptionAndCompensate(e);
                }
            } else {
                try {
                    if (!useCompensation && TransactionSupport.current().getStatus() == Status.STATUS_ACTIVE) {
                        getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
                    } else {
                        compensate();
                    }
                } catch (SystemException e) {
                    logExceptionAndCompensate(e);
                }
            }
        }
    }
    /**
     * Log the exception & call compensate.
     *
     * @param e
     *            The exception to log.
     */
    protected void logExceptionAndCompensate(Exception e) {
        log.error("Exception while wrapping-up compensation in endAction", e);
        compensate();
    }

    private void internalEndSuccessfully() {
        log.info("Ending command '{}' successfully.", getClass().getName());
        endSuccessfully();
    }

    protected void endSuccessfully() {
        setSucceeded(true);
    }

    void logRenamedEntity() {
        if (this instanceof RenamedEntityInfoProvider) {
            RenamedEntityInfoProvider renameable = (RenamedEntityInfoProvider) this;
            String oldEntityName = renameable.getEntityOldName();
            String newEntityName = renameable.getEntityNewName();
            if (!StringUtils.equals(oldEntityName, newEntityName)) {
                // log entity rename details
                AuditLogable logable = new AuditLogableImpl();
                String entityType = renameable.getEntityType();
                logable.addCustomValue("EntityType", entityType);
                logable.addCustomValue("OldEntityName", oldEntityName);
                logable.addCustomValue("NewEntityName", newEntityName);
                if (getCurrentUser() != null) {
                    logable.addCustomValue("UserName", getCurrentUser().getLoginName());
                }
                renameable.setEntityId(logable);
                auditLog(logable, getCurrentUser() != null ?
                        AuditLogType.ENTITY_RENAMED
                        : AuditLogType.ENTITY_RENAMED_INTERNALLY);
            }
        }
    }

    protected void auditLog(AuditLogable logable, AuditLogType logType) {
        auditLogDirector.log(logable, logType);
    }

    private void internalEndWithFailure() {
        log.error("Ending command '{}' with failure.", getClass().getName());
        endWithFailure();
        rollbackQuota();
    }

    private void rollbackQuota() {
        // Quota accounting is done only in the most external Command.
        if (isQuotaChanged()) {
            List<QuotaConsumptionParameter> consumptionParameters = getQuotaConsumptionParameters();
            if (consumptionParameters != null) {
                for (QuotaConsumptionParameter parameter : consumptionParameters) {
                    getQuotaManager().removeQuotaFromCache(getStoragePool().getId(), parameter.getQuotaGuid());
                }
            }
        }
    }

    protected List<QuotaConsumptionParameter> getQuotaConsumptionParameters() {

        // This a double marking mechanism which was created to ensure Quota dependencies would not be inherited
        // by descendants commands. Each Command is both marked by the QuotaDependency and implements the required
        // Interfaces (NONE does not implement any of the two interfaces).
        // The enum markings prevent Quota dependencies unintentional inheritance.
        if (consumptionParameters == null) {
            switch (getActionType().getQuotaDependency()) {
                case NONE:
                    return null;
                case STORAGE:
                    consumptionParameters = getThisQuotaStorageDependent().getQuotaStorageConsumptionParameters();
                    break;
                case CLUSTER:
                    consumptionParameters = getThisQuotaVdsDependent().getQuotaVdsConsumptionParameters();
                    break;
                default:
                    consumptionParameters = getThisQuotaStorageDependent().getQuotaStorageConsumptionParameters();
                    consumptionParameters.addAll(getThisQuotaVdsDependent().getQuotaVdsConsumptionParameters());
                    break;
            }
        }
        return consumptionParameters;
    }

    private QuotaStorageDependent getThisQuotaStorageDependent() {
        return (QuotaStorageDependent) this;
    }

    private QuotaVdsDependent getThisQuotaVdsDependent() {
        return (QuotaVdsDependent) this;
    }

    protected void endWithFailure() {
        setSucceeded(true);
        rollbackQuota();
    }

    private boolean isValidateSupportsTransaction() {
        return getClass().isAnnotationPresent(ValidateSupportsTransaction.class);
    }

    private boolean internalValidate() {
        boolean isValid = false;
        try {
            isValid = isValidateSupportsTransaction() ?
                    internalValidateInTransaction() :
                    TransactionSupport.executeInSuppressed(this::internalValidateInTransaction);

        } catch (DataAccessException dataAccessEx) {
            log.error("Data access error during ValidateFailure.", dataAccessEx);
            addValidationMessage(EngineMessage.CAN_DO_ACTION_DATABASE_CONNECTION_FAILURE);
        } catch (RuntimeException ex) {
            log.error("Error during ValidateFailure.", ex);
            addValidationMessage(EngineMessage.CAN_DO_ACTION_GENERAL_FAILURE);
        } finally {
            if (!isValid) {
                setCommandStatus(CommandStatus.ENDED_WITH_FAILURE);
                freeLock();
            }
        }
        return isValid;
    }

    private boolean internalValidateInTransaction() {
        boolean isValid = isUserAuthorizedToRunAction()
                && validateInputs()
                && acquireLock()
                && validate()
                && internalValidateAndSetQuota();

        if (!isValid && log.isWarnEnabled()
                && getReturnValue().getValidationMessages().size() > 0) {
            log.warn("Validation of action '{}' failed for user {}. Reasons: {}",
                    getActionType(), getUserName(),
                    StringUtils.join(getReturnValue().getValidationMessages(), ','));
        }

        return isValid;
    }

    private boolean internalValidateAndSetQuota() {
        // Quota accounting is done only in the most external Command.
        if (!isQuotaDependant()) {
            return true;
        }

        List<QuotaConsumptionParameter> quotaParams = getQuotaConsumptionParameters();
        if (quotaParams == null) {
            throw new InvalidQuotaParametersException("Command: " + this.getClass().getName()
                    + ". No Quota parameters available.");
        }

        // Some commands are not quotable, given the values of their parameters.
        // e.g AddDisk is storage-quotable but when the disk type is external LUN there is no storage pool to it.
        // scenarios like this must set its QuotaConsumptionParameter to an empty list.
        if (quotaParams.isEmpty()) {
            return true;
        }

        if (getStoragePool() == null) {
            throw new InvalidQuotaParametersException("Command: " + this.getClass().getName()
                    + ". Storage pool is not available for quota calculation. ");
        }

        boolean result = getQuotaManager().consume(this, quotaParams);
        setQuotaChanged(result);
        return result;
    }

    protected boolean isQuotaDependant() {
        if (getActionType().getQuotaDependency() == ActionType.QuotaDependency.NONE) {
            return false;
        }

        if (!isInternalExecution()) {
            return true;
        }

        return getActionType().isQuotaDependentAsInternalCommand();
    }

    /**
     * @return true if all parameters class and its inner members passed
     *         validation
     */
    protected boolean validateInputs() {
        return validateObject(getParameters());
    }

    protected boolean validateObject(Object value) {
        List<String> messages = ValidationUtils.validateInputs(getValidationGroups(), value);
        if (!messages.isEmpty()) {
            getReturnValue().getValidationMessages().addAll(messages);
            return false;
        }
        return true;
    }

    /**
     * Set the parameters for bll messages (such as type and action).
     * The parameters should be initialized through the command that is called,
     * instead set them at the validate()
     */
    protected void setActionMessageParameters() {
        // No-op method for inheritors to implement
    }

    protected List<Class<?>> getValidationGroups() {
        return validationGroups;
    }

    protected List<Class<?>> addValidationGroup(Class<?>... validationGroup) {
        validationGroups.addAll(Arrays.asList(validationGroup));
        return validationGroups;
    }

    /**
     * Checks if the current user is authorized to run the given action on the given object.
     *
     * @param userId
     *            user id to check
     * @param object
     *            the object to check
     * @param type
     *            the type of the object to check
     * @return <code>true</code> if the current user is authorized to run the action, <code>false</code> otherwise
     */
    protected boolean checkUserAuthorization(Guid userId,
            final ActionGroup actionGroup,
            final Guid object,
            final VdcObjectType type) {
        // Grant if there is matching permission in the database:
        final Guid permId = permissionDao.getEntityPermissions(userId, actionGroup, object, type);
        if (permId != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found permission '{}' for user when running '{}', on '{}' with id '{}'",
                        permId,
                        getActionType(),
                        type.getVdcObjectTranslation(),
                        object);
            }
            return true;
        }

        // Deny otherwise:
        if (log.isDebugEnabled()) {
            log.debug("No permission found for user when running action '{}', on object '{}' for action group '{}' with id '{}'.",
                    getActionType(),
                    type.getVdcObjectTranslation(),
                    actionGroup,
                    object);
        }
        return false;
    }

    /**
     * Check if current user is authorized to run current action. Skip check if
     * MLA is off or command is internal.
     *
     * @return <code>true</code> if the user is authorized to run the given action,
     *   <code>false</code> otherwise
     */
    protected boolean isUserAuthorizedToRunAction() {
        // Skip check if this is an internal action:
        if (isInternalExecution()) {
            if (log.isDebugEnabled()) {
                log.debug("Permission check skipped for internal action {}.", getActionType());
            }
            return true;
        }

        // Skip check if multilevel administration is disabled:
        if (!MultiLevelAdministrationHandler.isMultilevelAdministrationOn()) {
            if (log.isDebugEnabled()) {
                log.debug("Permission check for action '{}' skipped because multilevel administration is disabled.",
                        getActionType());
            }
            return true;
        }

        // Deny the permissions if there is no logged in user:
        if (getCurrentUser() == null) {
            addValidationMessage(EngineMessage.USER_IS_NOT_LOGGED_IN);
            return false;
        }

        // Get identifiers and types of the objects whose permissions have to be
        // checked:
        final List<PermissionSubject> permSubjects = getPermissionCheckSubjects();

        if (CollectionUtils.isEmpty(permSubjects) && objectsRequiringPermissionExist()) {
            if (log.isDebugEnabled()) {
                log.debug("The set of objects to check is null or empty for action '{}'.", getActionType());
            }
            addValidationMessage(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);

            return false;
        }

        if (isQuotaDependant()) {
            addQuotaPermissionSubject(permSubjects);
        }

        if (log.isDebugEnabled()) {
            StringBuilder builder = getPermissionSubjectsAsStringBuilder(permSubjects);

            log.debug("Checking whether user '{}' or one of the groups he is member of, have the following permissions: {}",
                    getCurrentUser().getId(),
                    builder.toString());
        }

        // If we are here then we should grant the permission:
        return checkPermissions(permSubjects);
    }

    /**
     * Override this method with false if there are no objects that require permission checking, not because
     * the user does not have the permissions for these objects, but because none currently exist in engine.
     * For example, a cluster to which no hosts have yet been added.
     * @return there are objects requiring permissions that need to be verified against permission subjects
     */
    protected boolean objectsRequiringPermissionExist() {
        return true;
    }

    protected boolean checkPermissions(final List<PermissionSubject> permSubjects) {
        for (PermissionSubject permSubject : permSubjects) {
            if (!checkSinglePermission(permSubject, getReturnValue().getValidationMessages())) {
                logMissingPermission(permSubject);
                return false;
            }
        }
        return true;
    }

    protected void logMissingPermission(PermissionSubject permSubject) {
        log.info("No permission found for user '{}' or one of the groups he is member of,"
                + " when running action '{}', Required permissions are: Action type: '{}' Action group: '{}'"
                + " Object type: '{}'  Object ID: '{}'.",
                getCurrentUser().getId(),
                getActionType(),
                permSubject.getActionGroup().getRoleType().name(),
                permSubject.getActionGroup().name(),
                permSubject.getObjectType().getVdcObjectTranslation(),
                permSubject.getObjectId());
    }

    public final boolean checkSinglePermission(PermissionSubject permSubject, Collection<String> messages) {
        final Guid objectId = permSubject.getObjectId();
        final VdcObjectType objectType = permSubject.getObjectType();
        final ActionGroup objectActionGroup = permSubject.getActionGroup();

        // if objectId is null we can't check permission
        if (objectId == null) {
            if (log.isDebugEnabled()) {
                log.debug("The object to check is null for action '{}'.", getActionType());
            }
            messages.add(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION.name());
            return false;
        }
        // Check that an action group is defined for this action;
        if (objectActionGroup == null) {
            if (log.isDebugEnabled()) {
                log.debug("No action group is defined for action '{}'.", getActionType());
            }
            return false;
        }

        // Check the authorization:
        if (!checkUserAuthorization(getCurrentUser().getId(), objectActionGroup, objectId, objectType)) {
            messages.add(permSubject.getMessage().name());
            return false;
        }
        return true;
    }

    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        // if quota enforcement is not in HARD_ENFORCEMENT the quota may be null.
        if (!isInternalExecution() && getStoragePool() != null
                && getStoragePool().getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED
                && getStoragePool().getQuotaEnforcementType() != QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT) {

            List<QuotaConsumptionParameter> consumptionParameters = getQuotaConsumptionParameters();

            if (consumptionParameters != null) {
                consumptionParameters.stream()
                        .filter(parameter -> parameter.getQuotaGuid() != null)
                        .filter(parameter -> !Guid.Empty.equals(parameter.getQuotaGuid()))
                        .filter(parameter -> QuotaConsumptionParameter.QuotaAction.RELEASE != parameter.getQuotaAction())
                        .map(parameter -> new PermissionSubject(parameter.getQuotaGuid(),
                                VdcObjectType.Quota,
                                ActionGroup.CONSUME_QUOTA,
                                EngineMessage.USER_NOT_AUTHORIZED_TO_CONSUME_QUOTA))
                        .forEach(quotaPermissionList::add);
            }
        }
    }

    /**
     * Validates that the pre-conditions for command execution are met.
     * <p>
     * In general, each command has its own conditions which should met, in order to expect a valid command execution.
     * An attempt to execute a command which failed to meet all the condition will lead to unpredicted result and should
     * be avoided.
     * <p>
     * The violated condition messages are stored by {@link #addValidationMessage(EngineMessage)} and can be reviewed
     * in {@link ActionReturnValue#getValidationMessages()} retrieved by {@link #getReturnValue()}
     *
     * @return {@code true} if the command can be executed, else {@code false}
     *
     * @see ValidateSupportsTransaction
     */
    protected boolean validate() {
        return true;
    }

    protected boolean getSucceeded() {
        return getReturnValue().getSucceeded();
    }

    protected void setSucceeded(boolean value) {
        getReturnValue().setSucceeded(value);
    }

    public boolean getCommandShouldBeLogged() {
        return getParameters().getShouldBeLogged();
    }

    public void setCommandShouldBeLogged(boolean value) {
        getParameters().setShouldBeLogged(value);
    }

    protected void setActionReturnValue(Object value) {
        getReturnValue().setActionReturnValue(value);
    }

    protected <R> R getActionReturnValue() {
        return getReturnValue().getActionReturnValue();
    }

    protected boolean isExecutedAsChildCommand() {
        return getParameters().getParentCommand() != ActionType.Unknown;
    }

    /**
     * Calculates the proper parameters for the task
     * @param parentCommandType parent command type for which the task is created
     * @param parameters parameter of the creating command
     */
    protected ActionParametersBase getParametersForTask(ActionType parentCommandType,
                                                           ActionParametersBase parameters) {
        // If there is no parent command, the command that its type
        // will be stored in the DB for thr task is the one creating the command
        ActionParametersBase parentParameters = parameters.getParentParameters();
        if (parentCommandType == ActionType.Unknown || parentParameters == null) {
            return parameters;
        }

        // The parent parameters are the ones that are kept for the task.
        // In order to make sure that in case of rollback-by-command, the ROLLBACK
        // flow will be called, the execution reason of the child command is set
        // to the one of the parent command (if its REGULAR_FLOW, the execution
        // reason of the parent command remains REGULAR_FLOW).
        parentParameters.setExecutionReason(parameters.getExecutionReason());
        parentParameters.setCommandType(parentCommandType);
        return parentParameters;
    }

    public boolean isSystemSuperUser() {
        return isSystemSuperUserPredicate.test(getCurrentUser());
    }

    private boolean executeWithoutTransaction() {
        boolean functionReturnValue = false;
        boolean exceptionOccurred = true;
        try {
            logRunningCommand();
            executeCommand();
            functionReturnValue = getSucceeded();
            exceptionOccurred = false;
        } catch (EngineException e) {
            log.error("Command '{}' failed: {}",
                    getClass().getName(),
                    e.getMessage());
            log.debug("Exception", e);
            processExceptionToClient(new EngineFault(e, e.getVdsError().getCode()));
        } catch (OpenStackResponseException e) {
            // Adding a message to executeFailedMessages is needed only when the list is empty
            if (returnValue.getExecuteFailedMessages().isEmpty()) {
                processExceptionToClient(new EngineFault(e, EngineError.ENGINE));
            }
            log.error("Command '{}' failed: {}", getClass().getName(), e.getMessage());
            log.error("Exception", e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof EngineException) {
                EngineException ex = (EngineException) e.getCause();
                processExceptionToClient(new EngineFault(ex, ex.getVdsError().getCode()));
            } else {
                processExceptionToClient(new EngineFault(e, EngineError.ENGINE));
            }
            log.error("Command '{}' failed: {}", getClass().getName(), e.getMessage());
            log.error("Exception", e);
        } finally {
            if (!exceptionOccurred) {
                setCommandExecuted();
            }
            // If we failed to execute due to exception or some other reason, we compensate for the failure.
            if (exceptionOccurred || !getSucceeded()) {
                setSucceeded(false);
                compensate();
                if (commandStatus == CommandStatus.ACTIVE) {
                    setCommandStatus(noAsyncOperations() ? CommandStatus.ENDED_WITH_FAILURE :
                            CommandStatus.EXECUTION_FAILED);
                }
            } else {
                // if the command is not an async task and has no custom callback
                // set the status to ENDED_SUCCESSFULLY if the status is ACTIVE
                if (getReturnValue().getVdsmTaskIdList().isEmpty() &&
                        getReturnValue().getInternalVdsmTaskIdList().isEmpty() &&
                        getCallback() == null &&
                        commandStatus == CommandStatus.ACTIVE) {
                    setCommandStatus(CommandStatus.ENDED_SUCCESSFULLY);
                }
                if (getCompensationContext() != null
                        && getCommandCompensationPhase() == CommandCompensationPhase.EXECUTION) {
                    getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
                }
            }
        }
        return functionReturnValue;
    }

    protected TransactionScopeOption getTransactionScopeOption() {
        return getParameters().getTransactionScopeOption();
    }

    private String getCommandParamatersString(T params) {
        StringBuilder buf = new StringBuilder();
        List<String> methodNames = ReflectionUtils.getGetterMethodNames(params);

        methodNames.removeAll(ReflectionUtils.getGetterMethodNames(new ActionParametersBase()));

        for (String methodName : methodNames) {
            Method method = ReflectionUtils.getLoggableMethodWithNoArgs(params, methodName);
            if (method == null) {
                continue;
            }
            Object retVal = ReflectionUtils.invokeMethodWithNoArgs(params, method);
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(getFieldName(methodName));
            buf.append(" = ");
            buf.append(retVal == null ? "null" : retVal.toString());
        }
        return buf.toString();
    }

    private String getFieldName(String methodName) {
        String GET_ROOT = "get";
        String IS_ROOT = "is";
        return methodName.startsWith(GET_ROOT) ? methodName.substring(GET_ROOT.length()) : methodName.substring(IS_ROOT.length());
    }

    /**
     * Log the running command , and log the affected entity id and type (if
     * there are any).
     */
    private void logRunningCommand() {
        // Set start of log for running command.
        StringBuilder logInfo = new StringBuilder("Running command: ")
                .append(getClass().getSimpleName());

        if (log.isDebugEnabled()) {
            logInfo.append(getParameters() != null ? "(" + getCommandParamatersString(getParameters()) + ")" : StringUtils.EMPTY);
        }

        logInfo.append(" internal: ").append(isInternalExecution()).append(".");

        // Get permissions of object ,to get object id.
        List<PermissionSubject> permissionSubjectList = getPermissionCheckSubjects();

        // Log if there is entry in the permission map.
        if (permissionSubjectList != null && !permissionSubjectList.isEmpty()) {
            // Build entities string for entities affected by this operation.
            StringBuilder logEntityIdsInfo = getPermissionSubjectsAsStringBuilder(permissionSubjectList);

            // If found any entities, add the log to the logInfo.
            if (logEntityIdsInfo.length() != 0) {
                // Print all the entities affected.
                logInfo.append(" Entities affected : ").append(
                        logEntityIdsInfo);
            }
        }

        // Log the final appended message to the log.
        log.info("{}", logInfo);
    }

    private StringBuilder getPermissionSubjectsAsStringBuilder(List<PermissionSubject> permissionSubjects) {
        StringBuilder builder = new StringBuilder();

        // Iterate all over the entities , which should be affected.
        permissionSubjects.stream().filter(permSubject -> permSubject.getObjectId() != null).forEach(permSubject -> {
            // Add comma when there are more than one entity affected.
            if (builder.length() != 0) {
                builder.append(", ");
            }
            builder.append(" ID: ").append(permSubject.getObjectId())
                    .append(" Type: ").append(permSubject.getObjectType());
            if (permSubject.getActionGroup() != null) {
                builder.append("Action group ")
                        .append(permSubject.getActionGroup().name())
                        .append(" with role type ")
                        .append(permSubject.getActionGroup().getRoleType().name());
            }
        });
        return builder;
    }

    private void executeActionInTransactionScope() {
        registerRollbackHandler((TransactionRollbackListener)() -> {
            log.error("Transaction rolled-back for command '{}'.", getClass().getName());
            try {
                if (isQuotaDependant()) {
                    rollbackQuota();
                }
            } catch (NullPointerException e) {
                log.error("RollbackQuota: failed (may be because quota is disabled)", e);
            }
            cancelTasks();
        });

        // If we didn't managed to acquire lock for command or the object wasn't managed to execute properly, then
        // rollback the transaction.
        if (!executeWithoutTransaction()) {
            if (TransactionSupport.current() == null) {
                cancelTasks();
            }

            // we don't want to commit transaction here
            TransactionSupport.setRollbackOnly();
        }
    }

    protected void registerRollbackHandler(TransactionCompletionListener transactionCompletionListener) {
        if (TransactionSupport.current() != null) {
            TransactionSupport.registerRollbackHandler(transactionCompletionListener);
        }
    }

    protected boolean parentHasCallback() {
        if (isExecutedAsChildCommand()
                && getParameters().getParentParameters() != null) {
            CommandEntity commandEntity =
                    commandCoordinatorUtil.getCommandEntity(getParameters().getParentParameters().getCommandId());
            return commandEntity != null && commandEntity.isCallbackEnabled();
        }

        return false;
    }

    private void handleCommandStepAndEntities() {
        if (getCommandStep() != null) {
            Step taskStep =
                    executionHandler.addTaskStep(getExecutionContext(),
                            getCommandStep(),
                            ExecutionMessageDirector.resolveStepMessage(getCommandStep(), getJobMessageProperties()),
                            getCommandStepSubjectEntities());
            if (taskStep != null) {
                if (shouldUpdateStepProgress()) {
                    stepDao.updateStepProgress(taskStep.getId(), 0);
                }
                getExecutionContext().setStep(taskStep);
                persistCommandIfNeeded();
            }
        }
    }

    protected final void execute() {
        setCommandStatus(CommandStatus.ACTIVE);
        getReturnValue().setValid(true);
        getReturnValue().setIsSynchronous(true);

        if (shouldPersistCommand()) {
            persistCommandIfNeeded();
            commandCoordinatorUtil.persistCommandAssociatedEntities(getCommandId(), getSubjectEntities());
        }

        executionHandler.addStep(getExecutionContext(), StepEnum.EXECUTING, null);

        handleCommandStepAndEntities();

        try {
            handleTransactivity();
            TransactionSupport.executeInScope(scope, this);
        } catch (TransactionRolledbackLocalException e) {
            log.info("Transaction was aborted in '{}'", this.getClass().getName());
            // Transaction was aborted - we must sure we compensation for all previous applicative stages of the command
            compensate();
        } finally {
            try {
                if (getCommandShouldBeLogged()) {
                    logCommand();
                }
                if (getSucceeded()) {
                    if (getCommandShouldBeLogged()) {
                        logRenamedEntity();
                    }

                    // only after creating all tasks, we can start polling them (we
                    // don't want
                    // to start polling before all tasks were created, otherwise we
                    // might change
                    // the VM/VmTemplate status to 'Down'/'OK' too soon.
                    startPollingAsyncTasks();
                }
            } finally {
                if (noAsyncOperations() && !executionHandler.checkIfJobHasTasks(getExecutionContext())) {
                    executionHandler.endJob(getExecutionContext(), getSucceeded());
                }
            }
        }
    }

    public boolean hasTasks() {
        return !getReturnValue().getVdsmTaskIdList().isEmpty();
    }

    private boolean getForceCompensation() {
        NonTransactiveCommandAttribute annotation = getClass().getAnnotation(NonTransactiveCommandAttribute.class);
        return annotation != null && annotation.forceCompensation();
    }

    private CommandCompensationPhase getCommandCompensationPhase() {
        NonTransactiveCommandAttribute annotation = getClass().getAnnotation(NonTransactiveCommandAttribute.class);
        return annotation != null ?
                annotation.compensationPhase() :
                CommandCompensationPhase.EXECUTION;
    }

    protected abstract void executeCommand();

    private void logCommand() {
        Class<?> type = getClass();
        InternalCommandAttribute annotation = type.getAnnotation(InternalCommandAttribute.class);
        if (annotation == null) {
            log();
        }
    }

    protected void log() {
        TransactionSupport.executeInSuppressed(() -> {
            try {
                auditLogDirector.log(this, getAuditLogTypeValue());
            } catch (final RuntimeException ex) {
                log.error("Error during log command: {}: '{}'",
                        getClass().getName(),
                        ExceptionUtils.getRootCauseMessage(ex));
                log.debug("Exception", ex);
            }
            return null;
        });
    }

    private boolean getTransactive() {
        NonTransactiveCommandAttribute annotation = getClass().getAnnotation(NonTransactiveCommandAttribute.class);
        return annotation == null;
    }

    public Map<String, Serializable> getCommandData() {
        return commandData;
    }

    public void setCommandData(Map<String, Serializable> commandData) {
        this.commandData = commandData;
    }

    @Override
    public T getParameters() {
        return parameters;
    }

    public ActionReturnValue getReturnValue() {
        if (returnValue == null) {
            returnValue = new ActionReturnValue();
        }
        return returnValue;
    }

    public void setReturnValue(ActionReturnValue returnValue) {
        this.returnValue = returnValue;
    }

    public ActionType getActionType() {
        try {
            if (actionType == null) {
                String name = getClass().getName();
                name = name.substring(0, name.length() - 7);
                name = name.substring(name.lastIndexOf('.') + 1);
                actionType = ActionType.valueOf(name);
            }
            return actionType;
        } catch (Exception e) {
            return ActionType.Unknown;
        }
    }

    protected String getDescription() {
        return description;
    }

    protected void setDescription(String value) {
        description = value;
    }

    private void processExceptionToClient(EngineFault fault) {
        fault.setSessionID(getParameters().getSessionId());
        returnValue.getExecuteFailedMessages().add(fault.getError().name());
        returnValue.setFault(fault);
    }

    Map<String, Guid> taskKeyToTaskIdMap = new HashMap<>();

    public Guid persistAsyncTaskPlaceHolder(ActionType parentCommand) {
        return persistAsyncTaskPlaceHolder(parentCommand, DEFAULT_TASK_KEY);
    }

    public Guid persistAsyncTaskPlaceHolder(ActionType parentCommand, final String taskKey) {
        Guid taskId = Guid.Empty;
        try {
            AsyncTaskCreationInfo creationInfo = new AsyncTaskCreationInfo();
            creationInfo.setTaskType(getTaskType());
            final AsyncTask task = createAsyncTask(creationInfo, parentCommand);
            taskId = task.getTaskId();
            TransactionScopeOption scopeOption =
                    getTransactive() ? TransactionScopeOption.RequiresNew : TransactionScopeOption.Required;
            TransactionSupport.executeInScope(scopeOption, () -> {
                saveTaskAndPutInMap(taskKey, task);
                return null;
            });
            addToReturnValueTaskPlaceHolderIdList(taskId);
        } catch (RuntimeException ex) {
            log.error("Error during persistAsyncTaskPlaceHolder for command '{}': {}",
                    getClass().getName(),
                    ex.getMessage());
            log.error("Exception", ex);
        }
        return taskId;
    }

    private void saveTaskAndPutInMap(String taskKey, AsyncTask task) {
        commandCoordinatorUtil.saveAsyncTaskToDb(task);
        taskKeyToTaskIdMap.put(taskKey, task.getTaskId());
    }

    private void addToReturnValueTaskPlaceHolderIdList(Guid taskId) {
        if (!getReturnValue().getTaskPlaceHolderIdList().contains(taskId)) {
            getReturnValue().getTaskPlaceHolderIdList().add(taskId);
        }
    }

    public void deleteAsyncTaskPlaceHolder(String taskKey) {
        Guid taskId = taskKeyToTaskIdMap.remove(taskKey);
        if (!Guid.isNullOrEmpty(taskId)) {
            commandCoordinatorUtil.removeTaskFromDbByTaskId(taskId);
        }
    }

    public Guid getAsyncTaskId() {
        return getAsyncTaskId(DEFAULT_TASK_KEY);
    }

    public Guid getAsyncTaskId(String taskKey) {
        if (!taskKeyToTaskIdMap.containsKey(taskKey)) {
            return Guid.Empty;
        }
        return taskKeyToTaskIdMap.get(taskKey);
    }
    /**
     * Use this method in order to create task in the CommandCoordinatorUtil in a safe way. If you use this method within a
     * certain command, make sure that the command implemented the ConcreteCreateTask method.
     *
     * @param taskId
     *            id of task to create
     * @param asyncTaskCreationInfo
     *            info to send to CommandCoordinatorUtil when creating the task.
     * @param parentCommand
     *            ActionType of the command that its endAction we want to invoke when tasks are finished.
     * @param entitiesMap
     *            maps ID of entity to its type.
     * @return Guid of the created task.
     */
    protected Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            Map<Guid, VdcObjectType> entitiesMap) {
        return createTask(taskId, asyncTaskCreationInfo, parentCommand, null, entitiesMap);
    }

    /**
     * Same as {@link #createTask(Guid, AsyncTaskCreationInfo, ActionType, VdcObjectType, Guid...)}
     * but without suspending the current transaction.
     *
     * Note: it is better to use {@link #createTask(Guid, AsyncTaskCreationInfo, ActionType, VdcObjectType, Guid...)}
     * since it suspend the current transaction, thus the changes are being updated in the
     * DB right away. call this method only you have a good reason for it and
     * the current transaction is short.
     *
     * @see #createTask(Guid, AsyncTaskCreationInfo, ActionType, VdcObjectType, Guid...)
     */
    protected Guid createTaskInCurrentTransaction(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return createTaskImpl(taskId, asyncTaskCreationInfo, parentCommand, null, entityType, entityIds);
    }

    /**
     * Use this method in order to create task in the CommandCoordinatorUtil in a safe way. If you use this method within a
     * certain command, make sure that the command implemented the ConcreteCreateTask method.
     *
     * @param taskId
     *            if of task to create
     * @param asyncTaskCreationInfo
     *            info to send to CommandCoordinatorUtil when creating the task.
     * @param parentCommand
     *            ActionType of the command that its endAction we want to invoke when tasks are finished.
     * @return Guid of the created task.
     */
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, ActionType parentCommand) {
        return createTask(taskId,
                asyncTaskCreationInfo,
                parentCommand,
                null,
                // The reason Collections.emptyMap is not used here as
                // the map should be mutable
                new HashMap<>());
    }

    protected Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            VdcObjectType vdcObjectType,
            Guid... entityIds) {

        return createTask(taskId,
                asyncTaskCreationInfo,
                parentCommand,
                createEntitiesMapForSingleEntityType(vdcObjectType, entityIds));
    }

    protected Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            String description,
            VdcObjectType entityType,
            Guid... entityIds) {

        return createTask(taskId,
                asyncTaskCreationInfo,
                parentCommand,
                description,
                createEntitiesMapForSingleEntityType(entityType, entityIds));
    }

    /**
     * Use this method in order to create task in the CommandCoordinatorUtil in a safe way. If you use this method within a
     * certain command, make sure that the command implemented the ConcreteCreateTask method.
     *
     * @param asyncTaskCreationInfo
     *            info to send to CommandCoordinatorUtil when creating the task.
     * @param parentCommand
     *            ActionType of the command that its endAction we want to invoke when tasks are finished.
     * @param description
     *            A message which describes the task
     * @param entitiesMap - map of entities
     */
    protected Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            String description, Map<Guid, VdcObjectType> entitiesMap) {

        return TransactionSupport.executeInSuppressed(() -> {
            try {
                return createTaskImpl(taskId, asyncTaskCreationInfo, parentCommand, description, entitiesMap);
            } catch (RuntimeException ex) {
                log.error("Error during createTask for command '{}': {}", getClass().getName(), ex.getMessage());
                log.error("Exception", ex);
            }
            return Guid.Empty;
        });
    }

    private Guid createTaskImpl(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, ActionType parentCommand,
            String description, VdcObjectType entityType, Guid... entityIds) {
        return createTaskImpl(taskId,
                asyncTaskCreationInfo,
                parentCommand,
                description,
                createEntitiesMapForSingleEntityType(entityType, entityIds));
    }

    private Map<Guid, VdcObjectType> createEntitiesMapForSingleEntityType(VdcObjectType entityType, Guid... entityIds) {
        Map<Guid, VdcObjectType> entitiesMap = new HashMap<>();
        for (Guid entityId : entityIds) {
            entitiesMap.put(entityId, entityType);
        }
        return entitiesMap;
    }

    private Guid createTaskImpl(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            String description,
            Map<Guid, VdcObjectType> entitiesMap) {
        return commandCoordinatorUtil.createTask(taskId,
                this,
                asyncTaskCreationInfo,
                parentCommand,
                description,
                entitiesMap);
    }

    public StepEnum getCommandStep() {
        return null;
    }

    public List<StepSubjectEntity> getCommandStepSubjectEntities() {
        return Collections.emptyList();
    }

    public ActionParametersBase getParentParameters(ActionType parentCommand) {
        ActionParametersBase parentParameters = getParametersForTask(parentCommand, getParameters());
        if (parentParameters.getParametersCurrentUser() == null && getCurrentUser() != null) {
            parentParameters.setParametersCurrentUser(getCurrentUser());
        }
        return parentParameters;
    }

    private AsyncTask createAsyncTask(
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand) {
        return commandCoordinatorUtil.createAsyncTask(this, asyncTaskCreationInfo, parentCommand);
    }

    /** @return The type of task that should be created for this command.
     * Commands that do not create async tasks return notSupported
     **/
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.notSupported;
    }

    public AsyncTaskType getAsyncTaskType() {
        if (getTaskType() == AsyncTaskType.notSupported) {
            throw new UnsupportedOperationException();
        }
        return getTaskType();
    }

    public void startPollingAsyncTasks(Collection<Guid> taskIds) {
        taskIds.forEach(commandCoordinatorUtil::startPollingTask);
    }

    protected boolean noAsyncOperations() {
        return !hasTasks() && getCallback() == null;
    }

    protected void startPollingAsyncTasks() {
        startPollingAsyncTasks(getReturnValue().getVdsmTaskIdList());
    }

    public ArrayList<Guid> getTaskIdList() {
        return (isExecutedAsChildCommand() && !parentHasCallback()) ?
                getReturnValue().getInternalVdsmTaskIdList() : getReturnValue().getVdsmTaskIdList();
    }

    private void cancelTasks() {
        commandCoordinatorUtil.cancelTasks(this);
    }

    protected void revertTasks() {
        commandCoordinatorUtil.revertTasks(this);
    }

    protected EngineLock getLock() {
        return context.getLock();
    }

    protected void setLock(EngineLock lock) {
        context.withLock(lock);
    }

    /**
     * The default lock property settings for the commands
     */
    protected final LockProperties getLockingPropertiesSettings() {
        return LockProperties.create(Scope.None).withNoWait();
    }

    /**
     * Commands that need exclusive locks will override this method
     * to provide custom locking property settings
     */
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    /**
     * gets the lock properties for the command, sets the properties in the
     * command parameters
     */
    protected LockProperties getLockProperties() {
        LockProperties lockProperties = parameters.getLockProperties();
        if (lockProperties == null) {
            lockProperties = applyLockProperties(getLockingPropertiesSettings());
            parameters.setLockProperties(lockProperties);
        }
        return lockProperties;
    }

    protected boolean acquireLock() {
        LockProperties lockProperties = getLockProperties();
        boolean returnValue = true;
        if (!Scope.None.equals(lockProperties.getScope())) {
            releaseLocksAtEndOfExecute = Scope.Execution.equals(lockProperties.getScope());
            if (lockProperties.isNoWait()) {
                returnValue = acquireLockInternal();
            } else if (lockProperties.isWaitForever()) {
                acquireLockAndWait();
            } else if (lockProperties.isTimeout()) {
                returnValue = acquireLockOrTimeout();
            }
        }
        return returnValue;
    }

    public boolean reacquireLocks() {
        return acquireLockAsyncTask();
    }

    /**
     * The following method should be called after restart of engine during initialization of asynchronous task
     */
    public final boolean acquireLockAsyncTask() {
        LockProperties lockProperties = getLockProperties();
        boolean returnValue = true;
        if (!Scope.None.equals(lockProperties.getScope())) {
            releaseLocksAtEndOfExecute = Scope.Execution.equals(lockProperties.getScope());
            if (!releaseLocksAtEndOfExecute) {
                returnValue = acquireLockInternal();
            }
        }
        return returnValue;
    }

    protected boolean acquireLockInternal() {
        // if commandLock is null then we acquire new lock, otherwise probably we got lock from caller command.
        if (context.getLock() == null) {
            EngineLock lock = buildLock();
            if (lock != null) {
                var lockAcquireResult = lockManager.acquireLock(lock);
                if (lockAcquireResult.isAcquired()) {
                    log.info("Lock Acquired to object '{}'", lock);
                    context.withLock(lock);
                } else {
                    log.info("Failed to Acquire Lock to object '{}'", lock);
                    getReturnValue().getValidationMessages()
                    .addAll(extractVariableDeclarations(lockAcquireResult.getMessages()));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method gets {@link Iterable} of strings that might contain
     * variable declarations inside them, and return a new List in which
     * every variable declaration is extracted to a separate string in
     * order to conform the convention of the can-do-action messages.
     * for example:
     * "ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM$VmName MyVm"
     * will be splited to 2 strings:
     * "ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM" and "$VmName MyVm"
     */
    protected List<String> extractVariableDeclarations(Iterable<String> appendedValidateMsgs) {
        final List<String> result = new ArrayList<>();
        for (String appendedValidateMsg : appendedValidateMsgs) {
            result.addAll(Arrays.asList(appendedValidateMsg.split("(?=\\$)")));
        }
        return result;
    }

    private EngineLock buildLock() {
        EngineLock lock = null;
        Map<String, Pair<String, String>> exclusiveLocks = getExclusiveLocks();
        Map<String, Pair<String, String>> sharedLocks = getSharedLocks();
        if (exclusiveLocks != null || sharedLocks != null) {
            lock = new EngineLock(exclusiveLocks, sharedLocks);
        }
        return lock;
    }

    private EngineLock buildLockFromExclusives() {
        Map<String, Pair<String, String>> exclusiveLocks = getExclusiveLocks();
        return exclusiveLocks != null ? new EngineLock(exclusiveLocks, null) : null;
    }

    private void acquireLockAndWait() {
        // if commandLock is null then we acquire new lock, otherwise probably we got lock from caller command.
        if (context.getLock() == null) {
            Map<String, Pair<String, String>> exclusiveLocks = getExclusiveLocks();
            if (exclusiveLocks != null) {
                EngineLock lock = new EngineLock(exclusiveLocks, null);
                log.info("Before acquiring and wait lock '{}'", lock);
                lockManager.acquireLockWait(lock);
                context.withLock(lock);
                log.info("Lock-wait acquired to object '{}'", lock);
            }
        }
    }

    private boolean acquireLockOrTimeout() {
        // if commandLock is null then we acquire new lock, otherwise probably we got lock from caller command.
        if (context.getLock() == null) {
            EngineLock lock = buildLockFromExclusives();
            if (lock != null) {
                log.info("Before acquiring lock-timeout '{}'", lock);
                LockingResult lockAcquireResult = lockManager.acquireLockWait(
                    lock, getLockProperties().getTimeoutMillis()
                );
                if (lockAcquireResult.isAcquired()) {
                    log.info("Lock-timeout acquired to object '{}'", lock);
                    context.withLock(lock);
                } else {
                    log.info("Failed to acquire lock-timeout to object '{}'", lock);
                    getReturnValue().getValidationMessages()
                        .addAll(extractVariableDeclarations(lockAcquireResult.getMessages()));
                    return false;
                }
            }
        }
        return true;
    }

    private void freeLockExecute() {
        if (releaseLocksAtEndOfExecute || !getSucceeded() ||
                (noAsyncOperations() && !(this instanceof IVdsAsyncCommand))) {
            freeLock();
        }
    }

    /**
     * If the command has more than one task handler, we can reach the end action
     * phase and in that phase execute the next task handler. In that case, we
     * don't want to release the locks, so we ask whether we're not in execute state.
     */
    private void freeLockEndAction() {
        if (getActionState() != CommandActionState.EXECUTE) {
            freeLock();
        }
    }

    protected void freeLock() {
        if (context.getLock() != null) {
            lockManager.releaseLock(context.getLock());
            log.info("Lock freed to object '{}'", context.getLock());
            context.withLock(null);
            // free other locks here to guarantee they will be freed only once
            freeCustomLocks();
        }
    }

    /** hook for subclasses that hold additional custom locks */
    protected void freeCustomLocks() {
    }

    /**
     * The following method should return a map which is represent exclusive lock
     * <p>
     * Lock structure:
     * <pre>
     *     Map {
     *         locked entity identifier (usually id, it follows selected locking group),
     *         Pair {
     *             locking group,
     *             message to be shown when this lock prevents acquisition of another
     *                 lock/ other operation
     *         }
     *     }
     * </pre>
     * </p>
     * Message has form of {@link EngineMessage} constant followed by possible variable
     * replacements as parsed by {@link #extractVariableDeclarations}
     *
     * @see LockMessagesMatchUtil
     */
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return null;
    }

    /**
     * The following method should return a map which is represent shared lock
     *
     * @see #getExclusiveLocks()
     */
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return null;
    }

    @Override
    public Object runInTransaction() {
        if (actionState == CommandActionState.EXECUTE) {
            executeActionInTransactionScope();
        } else {
            endActionInTransactionScope();
        }
        return null;
    }

    /**
     * Use for call chaining of validation commands, so that their result will be validated and kept in the messages if
     * the validation had failed.<br>
     * <br>
     * <b>Example:</b>
     *
     * <pre>
     * boolean isValid = validate(SomeValidator.validateSomething(param1, param2, ...));
     * </pre>
     *
     * @param validationResult
     *            The validation result from the inline call to validate.
     * @return <code>true</code> if the validation was successful, and <code>false</code> if it wasn't.
     */
    protected boolean validate(ValidationResult validationResult) {
        if (!validationResult.isValid()) {
            addValidationMessages(validationResult.getMessages());
            validationResult.getVariableReplacements().forEach(this::addValidationMessage);
        }

        return validationResult.isValid();
    }

    /**
     * Add a message to the {@link CommandBase#validate()}'s return value.
     * This return value will be sent to the client for the detailed information
     * of why the action can't be performed.
     *
     * @param message
     *            The message to add.
     */
    protected void addValidationMessage(EngineMessage message) {
        getReturnValue().getValidationMessages().add(message.name());
    }

    protected void addValidationMessages(List<EngineMessage> messages) {
        getReturnValue().getValidationMessages().addAll(messages.stream().map(m -> m.name()).collect(Collectors.toList()));
    }

    /**
     * Add validation message with variable replacements and return false.
     *
     * @param message   the message to add
     * @param variableReplacements variable replacements
     * @return  false always
     * @see #addValidationMessage(String)
     */
    protected final boolean failValidation(EngineMessage message, String ... variableReplacements) {
        return failValidation(message, Arrays.asList(variableReplacements));
    }

    protected final boolean failValidation(EngineMessage message, Collection<String> variableReplacements) {
        return failValidation(Collections.singletonList(message), variableReplacements);
    }

    protected final boolean failValidation(List<EngineMessage> messages, String ... variableReplacements) {
        return failValidation(messages, Arrays.asList(variableReplacements));
    }

    protected final boolean failValidation(List<EngineMessage> messages, Collection<String> variableReplacements) {
        addValidationMessages(messages);
        for (String variableReplacement : variableReplacements) {
            addValidationMessage(variableReplacement);
        }
        return false;
    }

    /**
     * Add a message to the {@link CommandBase#validate()}'s return value.
     * This return value will be sent to the client for the detailed information of why the action can't be performed.
     *
     * @param message The message to add.
     */
    protected void addValidationMessage(String message) {
        getReturnValue().getValidationMessages().add(message);
    }

    /**
     * Add a variable to the {@link CommandBase#validate()}'s return value.
     * The variable will be formatted as "$varName varValue" and will be used to parse the placeholders defined
     * in the validate message itself
     *
     * @param varName the variable name
     * @param varValue the variable value
     */
    protected void addValidationMessageVariable(String varName, Object varValue) {
        getReturnValue().getValidationMessages().add(ReplacementUtils.createSetVariableString(varName, varValue));
    }

    /**
     * Run the given command in the VDS and return the VDS's response.
     *
     * @param commandType
     *            The command to run.
     * @param parameters
     *            The corresponding parameters for the command.
     * @return The return from the VDS, containing success/failure, async task ids (in case of success), or error data
     *         (in case of failure).
     * @throws org.ovirt.engine.core.common.errors.EngineException
     *             In case of an unhandled exception (Usually more severe than failure of the command, because we don't
     *             know why).
     */
    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters)
            throws EngineException {
        return vdsBroker.runVdsCommand(commandType, parameters);
    }

    /**
     * Permissions are attached to object so every command must declare its
     * object target type and its GUID
     *
     * @return Map of GUIDs to Object types
     */
    public abstract List<PermissionSubject> getPermissionCheckSubjects();

    /**
     * returns a collection of the command subject entities
     */
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.emptyList();
    }

    /**
     * Returns the properties which used to populate the job message. The default properties resolving will use
     * {@link #getPermissionCheckSubjects()} to get the entities associated with the command. The property key is the
     * type of the entity by {@code VdcObjectType.name()} and the value is the name of the entity or the entity
     * {@code Guid} in case non-resolvable entity name.
     *
     * @return A map which contains the data to be used to populate the {@code Job} description.
     */
    public Map<String, String> getJobMessageProperties() {
        jobProperties = new HashMap<>();
        List<PermissionSubject> subjects = getPermissionCheckSubjects();
        if (!subjects.isEmpty()) {
            VdcObjectType entityType;
            Guid entityId;
            String value;
            for (PermissionSubject permSubject : subjects) {
                entityType = permSubject.getObjectType();
                entityId = permSubject.getObjectId();
                if (entityType != null && entityId != null) {

                    value = entityDao.getEntityNameByIdAndType(entityId, entityType);
                    if (value == null) {
                        value = entityId.toString();
                    }
                    jobProperties.put(entityType.name().toLowerCase(), value);
                }
            }
        }
        return jobProperties;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        context.withExecutionContext(executionContext);
    }

    public ExecutionContext getExecutionContext() {
        return context.getExecutionContext();
    }

    public Guid getCommandId() {
        return commandId;
    }

    public CommandContext getContext() {
        return context;
    }

    /**
     * Adds a sub step on the current execution context by providing parent and new step information and step description
     * @param parentStep parent step to add the new sub step on
     * @param newStep step to add
     * @param description  description of step to be added
     */
    protected Step addSubStep(StepEnum parentStep, StepEnum newStep, String description) {
        return executionHandler.addSubStep(getExecutionContext(),
                (getExecutionContext().getJob() != null) ? getExecutionContext().getJob().getStep(parentStep)
                        : getExecutionContext().getStep(),
                newStep,
                description);
    }

    /**
     * Adds a sub step on the current execution context by providing parent and new step information and map that will be resolved to create a text message that describes the new step
     * @param parentStep parent step to add the new sub step on
     * @param newStep step to add
     * @param valuesMap map of values that will be used to compose the description of the step
     */
    protected Step addSubStep(StepEnum parentStep, StepEnum newStep, Map<String, String> valuesMap) {
        return addSubStep(parentStep, newStep, ExecutionMessageDirector.resolveStepMessage(newStep, valuesMap));
    }

    public QuotaManager getQuotaManager() {
        return quotaManager;
    }

    public boolean isQuotaChanged() {
        return quotaChanged;
    }

    public void setQuotaChanged(boolean quotaChanged) {
        this.quotaChanged = quotaChanged;
    }

    @Override
    public void setCorrelationId(String correlationId) {
        // correlation ID thread local variable is set for non multi-action
        if (!parameters.getMultipleAction()) {
            CorrelationIdTracker.setCorrelationId(correlationId);
        }
        super.setCorrelationId(correlationId);
    }

    /**
     * Propagates an internal command failures into the command which invoked it
     *
     * @param internalReturnValue
     *            the return value of the internal command
     */
    protected void propagateFailure(ActionReturnValue internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().addAll(internalReturnValue.getExecuteFailedMessages());
        getReturnValue().setFault(internalReturnValue.getFault());
        getReturnValue().getValidationMessages().addAll(internalReturnValue.getValidationMessages());
        getReturnValue().setValid(internalReturnValue.isValid());
    }

    protected void propagateFailure(QueryReturnValue internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().add(internalReturnValue.getExceptionString());
    }

    protected ActionReturnValue convertToActionReturnValue(final VDSReturnValue vdsReturnValue) {
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(false);
        returnValue.setActionReturnValue(vdsReturnValue.getReturnValue());
        String message = vdsReturnValue.getVdsError().getMessage();
        returnValue.setExecuteFailedMessages(new ArrayList<>(Collections.singleton(message)));
        return returnValue;
    }

    public void persistCommand(ActionType parentCommand) {
        persistCommand(parentCommand, getContext(), getCallback() != null, callbackTriggeredByEvent());
    }

    public void persistCommand(ActionType parentCommand, boolean enableCallback) {
        persistCommand(parentCommand, getContext(), enableCallback, callbackTriggeredByEvent());
    }

    private boolean shouldPersistCommand() {
        return getCallback() != null || parentHasCallback();
    }

    protected void persistCommandIfNeeded() {
        if (shouldPersistCommand()) {
            persistCommand(getParameters().getParentCommand());
        }
    }

    protected void updateCommandIfNeeded() {
        if (shouldPersistCommand() && commandCoordinatorUtil.getCommandEntity(getCommandId()) != null) {
            persistCommand(getParameters().getParentCommand());
        }
    }

    public void persistCommand(ActionType parentCommand,
            CommandContext cmdContext,
            boolean enableCallback,
            boolean callbackWaitingForEvent) {
        TransactionSupport.executeInSuppressed(() -> {
            CommandEntity commandEntity =
                    buildCommandEntity(getParentParameters(parentCommand).getCommandId(), enableCallback);
            commandEntity.setWaitingForEvent(callbackWaitingForEvent);
            commandCoordinatorUtil.persistCommand(commandEntity, cmdContext);
            return null;
        });
    }

    private CommandEntity buildCommandEntity(Guid rootCommandId, boolean enableCallback) {
        return CommandEntity.buildCommandEntity(getUserId(),
                getSessionSeqId(),
                getCommandId(),
                getParameters().getParentParameters() == null ? Guid.Empty : getParameters().getParentParameters().getCommandId(),
                rootCommandId,
                buildPersistedCommandContext(),
                getActionType(),
                getParameters(),
                commandStatus,
                enableCallback,
                getReturnValue(),
                getCommandData());
    }

    private PersistedCommandContext buildPersistedCommandContext() {
        PersistedCommandContext persistedCommandContext = new PersistedCommandContext();
        persistedCommandContext.setJobId(getExecutionContext() == null || getExecutionContext().getJob() == null
                ? null : getExecutionContext().getJob().getId());
        persistedCommandContext.setStepId(getExecutionContext() == null || getExecutionContext().getStep() == null
                ? null : getExecutionContext().getStep().getId());
        persistedCommandContext.setExecutionMethod(getExecutionContext() == null
                ? ExecutionMethod.AsStep
                : getExecutionContext().getExecutionMethod());
        if (getExecutionContext() != null) {
            persistedCommandContext.setCompleted(getExecutionContext().isCompleted());
            persistedCommandContext.setJobRequired(getExecutionContext().isJobRequired());
            persistedCommandContext.setMonitored(getExecutionContext().isMonitored());
            persistedCommandContext.setShouldEndJob(getExecutionContext().shouldEndJob());
            persistedCommandContext.setTasksMonitored(getExecutionContext().isTasksMonitored());
        }
        return persistedCommandContext;
    }

    public long getSessionSeqId() {
        if (sessionSeqId == null) {
            String sessionId = getContext().getEngineContext().getSessionId();
            // The session may not exists for quartz jobs
            sessionSeqId = getSessionDataContainer().isSessionExists(sessionId)
                    ? getSessionDataContainer().getEngineSessionSeqId(sessionId)
                    : SsoSessionUtils.EMPTY_SESSION_SEQ_ID;
        }
        return sessionSeqId;
    }

    public void setCommandStatus(CommandStatus status) {
        setCommandStatus(status, true);
    }

    public void setCommandStatus(CommandStatus status, boolean updateDB) {
        this.commandStatus = status;
        if (updateDB) {
            TransactionSupport.executeInSuppressed(() -> {
                commandCoordinatorUtil.updateCommandStatus(getCommandId(), commandStatus);
                return null;
            });
        }
    }

    public void setCommandExecuted() {
        TransactionSupport.executeInSuppressed(() -> {
            CommandEntity cmdEntity = commandCoordinatorUtil.getCommandEntity(getCommandId());
            if (cmdEntity != null) {
                CommandEntity executedCmdEntity = buildCommandEntity(cmdEntity.getRootCommandId(),
                        cmdEntity.isCallbackEnabled());
                executedCmdEntity
                        .setWaitingForEvent(cmdEntity.isCallbackEnabled() ? callbackTriggeredByEvent() : false);
                commandCoordinatorUtil.persistCommand(executedCmdEntity, getContext());
                commandCoordinatorUtil.updateCommandExecuted(getCommandId());
            }
            return null;
        });
    }

    private boolean callbackTriggeredByEvent() {
        CommandCallback callback = getCallback();
        return callback == null ? false : callback.isTriggeredByEvent();
    }

    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    public CommandCallback getCallback() {
        return null;
    }

    protected ActionReturnValue runInternalAction(ActionType actionType, ActionParametersBase parameters) {
        return backend.runInternalAction(actionType, parameters, context.clone());
    }

    protected ActionReturnValue runInternalAction(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext internalCommandContext) {
        return backend.runInternalAction(actionType,
                parameters,
                internalCommandContext);
    }

    protected List<ActionReturnValue> runInternalMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters) {
        return backend.runInternalMultipleActions(actionType, parameters, context.clone());
    }

    protected List<ActionReturnValue> runInternalMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters,
            ExecutionContext executionContext) {
        return backend.runInternalMultipleActions(actionType,
                parameters,
                context.clone().withExecutionContext(executionContext));
    }

    public ActionReturnValue runInternalActionWithTasksContext(ActionType actionType,
            ActionParametersBase parameters) {
        return runInternalActionWithTasksContext(actionType, parameters, null);
    }

    protected ActionReturnValue runInternalActionWithTasksContext(ActionType actionType,
            ActionParametersBase parameters, EngineLock lock) {
        return runInternalAction(
                actionType,
                parameters,
                ExecutionHandler.createDefaultContextForTasks(getContext(), lock));
    }

    protected QueryReturnValue runInternalQuery(QueryType type, QueryParametersBase queryParams) {
        return backend.runInternalQuery(type, queryParams, context.getEngineContext());
    }

    protected CommandContext cloneContext() {
        return getContext().clone();
    }


    public CommandContext cloneContextAndDetachFromParent() {
        return cloneContext().withoutCompensationContext().withoutExecutionContext().withoutLock();
    }

    /**
     * Clones CommandContext with CompensationContext that is not cleaned when the command successfully finishes.
     * This is useful if the parent command wants to revert a successful child command.
     */
    public CommandContext cloneContextWithNoCleanupCompensation() {
        return cloneContext()
                .withoutExecutionContext()
                .withoutLock()
                .withCompensationContext(new ChildCompensationWrapper(getCompensationContext()));
    }

    protected SessionDataContainer getSessionDataContainer() {
        return sessionDataContainer;
    }

    protected long getEngineSessionSeqId() {
        String sessionId = getParameters().getSessionId();
        if (sessionId == null && getContext() != null) {
            sessionId = getContext().getEngineContext().getSessionId();
        }
        if (sessionId == null) {
            throw new RuntimeException("No sessionId found for command " + getClass().getName());
        }
        return getSessionDataContainer().getEngineSessionSeqId(sessionId);
    }

    /**
     * This method is used to return the parameters that'll determine the command that will
     * be called when the created async tasks will end.
     */
    public ActionParametersBase getParentParameters() {
        // When the parent has callback the current command parameters should always be returned as the callback is
        // responsible to execute the parent endAction() and not the AsyncTaskManager
        if (parentHasCallback()) {
            return getParameters();
        }

        return getParameters().getParentParameters();
    }

    protected <P extends ActionParametersBase> P withRootCommandInfo(P params, ActionType actionType) {
        ActionType parentCommand = isExecutedAsChildCommand() ?
                getParameters().getParentCommand() : actionType;
        params.setParentParameters(getParametersForTask(parentCommand, getParameters()));
        params.setParentCommand(parentCommand);
        return params;
    }

    protected final <P extends ActionParametersBase> P withRootCommandInfo(P params) {
        return withRootCommandInfo(params, getActionType());
    }

    protected CommandActionState getActionState() {
        return actionState;
    }

    protected void compensationStateChanged() {
        if (isCompensationEnabledByCaller()) {
            getCompensationContext().stateChanged();
        }
    }

    /**
     * This method is used in parts of commands that do not support compensation yet.
     *
     * It guards against incorrect reverting of the command in case of a failure.
     */
    protected void throwIfCompensationEnabled() {
        if (isCompensationEnabledByCaller()) {
            throw new RuntimeException(String.format(
                    "Command %s with compensation enabled called a method that does not support compensation.",
                    this.getClass().getName()
            ));
        }
    }
}
