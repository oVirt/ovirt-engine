package org.ovirt.engine.core.bll;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionRolledbackLocalException;
import javax.inject.Inject;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.context.DefaultCompensationContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolManagerStrategy;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolPerDcSingleton;
import org.ovirt.engine.core.bll.quota.InvalidQuotaParametersException;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParametersWrapper;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.Command;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.CommandExecutionReason;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ActionVersionMap;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.EntityStatusSnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDao;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.StatusAwareDao;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.Deserializer;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.transaction.NoOpTransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import com.woorea.openstack.base.client.OpenStackResponseException;

public abstract class CommandBase<T extends VdcActionParametersBase>
        extends AuditLogableBase
        implements TransactionMethod<Object>, Command<T> {

    /* Multiplier used to convert GB to bytes or vice versa. */
    protected static final long BYTES_IN_GB = 1024 * 1024 * 1024;
    private static final String DEFAULT_TASK_KEY = "DEFAULT_TASK_KEY";
    private T _parameters;
    private VdcReturnValueBase _returnValue;
    private CommandActionState _actionState = CommandActionState.EXECUTE;
    private VdcActionType actionType;
    private final List<Class<?>> validationGroups = new ArrayList<>();
    private final Guid commandId;
    private boolean quotaChanged = false;
    private String _description = "";
    private TransactionScopeOption scope;
    private TransactionScopeOption endActionScope;
    private List<QuotaConsumptionParameter> consumptionParameters;

    @Inject
    private QuotaManager quotaManager;
    /** Indicates whether the acquired locks should be released after the execute method or not */
    private boolean releaseLocksAtEndOfExecute = true;
    /** Object which is representing a lock that some commands will acquire */

    protected Logger log = LoggerFactory.getLogger(getClass());

    /** The context defines how to monitor the command and handle its compensation */
    private final CommandContext context;

    /** A map contains the properties for describing the job */
    protected Map<String, String> jobProperties;

    /** Handlers for performing the logical parts of the command */
    private List<SPMAsyncTaskHandler> taskHandlers;

    private CommandStatus commandStatus = CommandStatus.NOT_STARTED;

    protected boolean isDataCenterWithSpm() {
        return isDataCenterWithSpm(getStoragePool());
    }

    protected boolean isDataCenterWithSpm(StoragePool storagePool) {
         return !FeatureSupported.dataCenterWithoutSpm(storagePool.getCompatibilityVersion());
     }

     protected boolean isDataCenterWithoutSpm() {
         return FeatureSupported.dataCenterWithoutSpm(getStoragePool().getCompatibilityVersion());
     }

    protected CommandActionState getActionState() {
        return _actionState;
    }

    protected CommandBase() {
        this(Guid.newGuid());
    }

    protected CommandBase(T parameters) {
        this(parameters, null);
    }

    protected CommandBase(T parameters, CommandContext cmdContext) {
        if (cmdContext == null) {
            cmdContext =
                    new CommandContext(new EngineContext().withSessionId(parameters.getSessionId()))
                            .withExecutionContext(new ExecutionContext());
        } else if (cmdContext.getEngineContext().getSessionId() == null) {
            // Needed for SEAT mechanism - session ID is available only on parameters
            // upon command re-instantiation (when moving between task handlers).
            cmdContext.getEngineContext().withSessionId(parameters.getSessionId());
        }
        this.context = cmdContext;
        _parameters = parameters;
        DbUser user =
                SessionDataContainer.getInstance().getUser(cmdContext.getEngineContext().getSessionId(), true);
        if (user != null) {
            setCurrentUser(user);
        }
        if (SessionDataContainer.getInstance().getPrincipalName(cmdContext.getEngineContext().getSessionId()) == null) {
            // command was most probably executed from Quartz job, so session doesn't contain any user info
            // we need to set username to fake internal user so audit logs will not contain "null@N/A" as username
            setUserName("SYSTEM");
        } else {
            setUserName(SessionDataContainer.getInstance().getUserName(cmdContext.getEngineContext().getSessionId()));
        }
        ExecutionContext executionContext = cmdContext.getExecutionContext();
        if (executionContext.getJob() != null) {
            setJobId(executionContext.getJob().getId());
        } else if (executionContext.getStep() != null) {
            setJobId(executionContext.getStep().getJobId());
        }

        Guid commandIdFromParameters = parameters.getCommandId();
        if (commandIdFromParameters == null) {
            commandIdFromParameters = Guid.newGuid();
            getParameters().setCommandId(commandIdFromParameters);
        }

        commandId = commandIdFromParameters;
        taskHandlers = initTaskHandlers();
        setCorrelationId(parameters.getCorrelationId());
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected CommandBase(Guid commandId) {
        this.context = new CommandContext(new EngineContext());
        this.commandId = commandId;
    }

    /**
     * @see PostConstruct
     */
    @PostConstruct
    protected final void postConstruct() {
        if (!isCompensationContext()) {
            init();
        }
    }

    private boolean isCompensationContext() {
        return getParameters() == null;
    }

    /**
     * Implement this method whenever you need extra initialization of the command after the
     * constructor. All DB calls or other interaction with the command dependencies for initialization
     * should be done here. It is ensured that all injected dependencies were injected at the time calling.
     */
    protected void init() {
    }

    protected List<SPMAsyncTaskHandler> initTaskHandlers() {
        return null;
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
    protected VdcReturnValueBase attemptRollback(VdcActionType commandType,
            VdcActionParametersBase params, CommandContext context) {
        if (canPerformRollbackUsingCommand(commandType, params)) {
            params.setExecutionReason(CommandExecutionReason.ROLLBACK_FLOW);
            params.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            return getBackend().runInternalAction(commandType, params, context);
        }
        return new VdcReturnValueBase();
    }

    protected BackendInternal getBackend() {
        return Injector.get(BackendInternal.class);
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
    protected VdcReturnValueBase checkAndPerformRollbackUsingCommand(VdcActionType commandType,
            VdcActionParametersBase params, CommandContext context) {
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
            (VdcActionType commandType,
                    VdcActionParametersBase params) {
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

        return createDefaultCompensationContext(commandId);
    }

    protected DefaultCompensationContext createDefaultCompensationContext(Guid commandId) {
        DefaultCompensationContext defaultContext = new DefaultCompensationContext();
        defaultContext.setCommandId(commandId);
        defaultContext.setCommandType(getClass().getName());
        defaultContext.setBusinessEntitySnapshotDao(getBusinessEntitySnapshotDao());
        defaultContext.setSnapshotSerializer(
                SerializationFactory.getSerializer());
        return defaultContext;
    }

    protected BusinessEntitySnapshotDao getBusinessEntitySnapshotDao() {
        return DbFacade.getInstance().getBusinessEntitySnapshotDao();
    }

    protected VdsSpmIdMapDao getVdsSpmIdMapDao() {
        return DbFacade.getInstance().getVdsSpmIdMapDao();
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

    public VdcReturnValueBase canDoActionOnly() {
        setActionMessageParameters();
        getReturnValue().setCanDoAction(internalCanDoAction());
        String tempVar = getDescription();
        getReturnValue().setDescription((tempVar != null) ? tempVar : getReturnValue().getDescription());
        return _returnValue;
    }

    public VdcReturnValueBase executeAction() {
        determineExecutionReason();
        _actionState = CommandActionState.EXECUTE;
        String tempVar = getDescription();
        getReturnValue().setDescription((tempVar != null) ? tempVar : getReturnValue().getDescription());
        setActionMessageParameters();
        Step validatingStep=null;
        boolean actionAllowed;
        boolean isExternal = this.getParameters().getJobId() != null || this.getParameters().getStepId() != null;
        if (!isExternal) {
            validatingStep = ExecutionHandler.addStep(getExecutionContext(), StepEnum.VALIDATING, null);
        }

        try {
            actionAllowed = getReturnValue().getCanDoAction() || internalCanDoAction();
            if (!isExternal) {
                ExecutionHandler.endStep(getExecutionContext(), validatingStep, actionAllowed);
            }

            if (actionAllowed) {
                execute();
            } else {
                getReturnValue().setCanDoAction(false);
            }
        } finally {
            freeLockExecute();
            clearAsyncTasksWithOutVdsmId();
        }
        return getReturnValue();
    }

    private void clearAsyncTasksWithOutVdsmId() {
        if (!getReturnValue().getTaskPlaceHolderIdList().isEmpty()) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    for (Guid asyncTaskId : getReturnValue().getTaskPlaceHolderIdList()) {
                        AsyncTask task = CommandCoordinatorUtil.getAsyncTaskFromDb(asyncTaskId);
                        if (task != null && Guid.isNullOrEmpty(task.getVdsmTaskId())) {
                            CommandCoordinatorUtil.removeTaskFromDbByTaskId(task.getTaskId());
                        }

                    }
                    return null;
                }
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
    protected void compensate() {
        if (hasTaskHandlers()) {
            getParameters().setExecutionReason(CommandExecutionReason.ROLLBACK_FLOW);
            getCurrentTaskHandler().compensate();
            revertPreviousHandlers();
        } else {
            internalCompensate();
        }
    }

    @SuppressWarnings({ "unchecked", "synthetic-access" })
    protected final void internalCompensate() {
        try {
            if (isQuotaDependant()) {
                rollbackQuota();
            }
        } catch (NullPointerException e) {
            log.debug("RollbackQuota: failed (may be because quota is disabled)", e);
        }
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                Deserializer deserializer =
                        SerializationFactory.getDeserializer();
                List<BusinessEntitySnapshot> entitySnapshots =
                        getBusinessEntitySnapshotDao().getAllForCommandId(commandId);
                log.debug("Command [id={}]: {} compensation data.", commandId,
                        entitySnapshots.isEmpty() ? "No" : "Going over");
                for (BusinessEntitySnapshot snapshot : entitySnapshots) {
                    Class<Serializable> snapshotClass =
                            (Class<Serializable>) ReflectionUtils.getClassFor(snapshot.getSnapshotClass());
                    Serializable snapshotData = deserializer.deserialize(snapshot.getEntitySnapshot(), snapshotClass);
                    log.info("Command [id={}]: Compensating {} of {}; snapshot: {}.",
                            commandId,
                            snapshot.getSnapshotType(),
                            snapshot.getEntityType(),
                            (snapshot.getSnapshotType() == SnapshotType.DELETED_OR_UPDATED_ENTITY ? "id=" + snapshot.getEntityId()
                                    : snapshotData.toString()));
                    Class<BusinessEntity<Serializable>> entityClass =
                            (Class<BusinessEntity<Serializable>>) ReflectionUtils.getClassFor(snapshot.getEntityType());
                    GenericDao<BusinessEntity<Serializable>, Serializable> daoForEntity =
                            DbFacade.getInstance().getDaoForEntity(entityClass);

                    switch (snapshot.getSnapshotType()) {
                    case CHANGED_STATUS_ONLY:
                        EntityStatusSnapshot entityStatusSnapshot = (EntityStatusSnapshot) snapshotData;
                        ((StatusAwareDao<Serializable, Enum<?>>) daoForEntity).updateStatus(
                                entityStatusSnapshot.getId(), entityStatusSnapshot.getStatus());
                        break;
                    case DELETED_OR_UPDATED_ENTITY:
                        BusinessEntity<Serializable> entitySnapshot = (BusinessEntity<Serializable>) snapshotData;
                        if (daoForEntity.get(entitySnapshot.getId()) == null) {
                            daoForEntity.save(entitySnapshot);
                        } else {
                            daoForEntity.update(entitySnapshot);
                        }
                        break;
                    case UPDATED_ONLY_ENTITY:
                        daoForEntity.update((BusinessEntity<Serializable>)snapshotData);
                        break;
                    case NEW_ENTITY_ID:
                        daoForEntity.remove(snapshotData);
                        break;
                    }
                }

                cleanUpCompensationData();
                return null;
            }

        });
    }

    /**
     * Delete the compensation data, so that we don't accidentaly try to compensate it at a later time.
     */
    private void cleanUpCompensationData() {
        if (!(getCompensationContext() instanceof NoOpCompensationContext)) {
            getBusinessEntitySnapshotDao().removeAllForCommandId(commandId);
        }
    }

    protected void startFinalizingStep() {
        ExecutionHandler.startFinalizingStep(getExecutionContext());
    }

    public VdcReturnValueBase endAction() {
        if (!hasTaskHandlers() || getExecutionIndex() == getTaskHandlers().size() - 1) {
            startFinalizingStep();
        }

        try {
            initiateLockEndAction();
            setActionState();
            handleTransactivity();
            TransactionSupport.executeInScope(endActionScope, this);
        } catch (TransactionRolledbackLocalException e) {
            log.info("endAction: Transaction was aborted in {}", this.getClass().getName());
        } finally {
            freeLockEndAction();
            if (getCommandShouldBeLogged()) {
                logCommand();
            }
        }

        return getReturnValue();
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
            _actionState = CommandActionState.END_SUCCESS;
        } else {
            _actionState = CommandActionState.END_FAILURE;
        }
    }

    protected boolean isEndSuccessfully() {
        return getParameters().getTaskGroupSuccess()
                && getParameters().getExecutionReason() == CommandExecutionReason.REGULAR_FLOW;
    }

    public void endActionInTransactionScope() {
        boolean exceptionOccurred = false;
        try {
            if (isEndSuccessfully()) {
                if (getCallback() == null) {
                    setCommandStatus(CommandStatus.SUCCEEDED);
                }
                internalEndSuccessfully();
            } else {
                if (getCallback() == null) {
                    setCommandStatus(CommandStatus.FAILED);
                }
                internalEndWithFailure();
            }
        } catch (RuntimeException e) {
            exceptionOccurred = true;
            throw e;
        } finally {
            freeLockEndAction();
            if (TransactionSupport.current() == null) {

                // In the unusual case that we have no current transaction, try to cleanup after yourself and if the
                // cleanup fails (probably since the transaction is aborted) then try to compensate.
                try {
                    cleanUpCompensationData();
                } catch (RuntimeException e) {
                    logExceptionAndCompensate(e);
                }
            } else {
                try {
                    if (!exceptionOccurred && TransactionSupport.current().getStatus() == Status.STATUS_ACTIVE) {
                        cleanUpCompensationData();
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
        if (hasTaskHandlers()) {
            getCurrentTaskHandler().endSuccessfully();
            getParameters().incrementExecutionIndex();
            if (getExecutionIndex() < getTaskHandlers().size()) {
                _actionState = CommandActionState.EXECUTE;
                execute();
            }
        } else {
            endSuccessfully();
        }
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
                AuditLogableBase logable = new AuditLogableBase();
                String entityType = renameable.getEntityType();
                logable.addCustomValue("EntityType", entityType);
                logable.addCustomValue("OldEntityName", oldEntityName);
                logable.addCustomValue("NewEntityName", newEntityName);
                logable.addCustomValue("UserName", getCurrentUser().getLoginName());
                renameable.setEntityId(logable);
                auditLog(logable, AuditLogType.ENTITY_RENAMED);
            }
        }
    }

    void auditLog(AuditLogableBase logable, AuditLogType logType) {
        auditLogDirector.log(logable, logType);
    }

    private void internalEndWithFailure() {
        log.error("Ending command '{}' with failure.", getClass().getName());
        if (hasTaskHandlers()) {
            if (hasStepsToRevert()) {
                getCurrentTaskHandler().endWithFailure();
                revertPreviousHandlers();
            } else {
                // since no handlers have been run we don't have to retry endAction
                getReturnValue().setEndActionTryAgain(false);
            }
            startPollingAsyncTasks();
        } else {
            endWithFailure();
        }
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
                case VDS_GROUP:
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

    private void revertPreviousHandlers() {
        getParameters().decrementExecutionIndex();
        if (hasStepsToRevert()) {
            logRollbackedTask();
            getParameters().setExecutionReason(CommandExecutionReason.ROLLBACK_FLOW);
            getCurrentTaskHandler().compensate();

            if (!hasRevertTask()) {
                // If there is no task to take us onwards, just run the previous handler's revert
                revertPreviousHandlers();
            }
        }
        else {
            setSucceeded(true);
        }
    }

    protected void logRollbackedTask() {
        String type = (getCurrentTaskHandler().getRevertTaskType() != null ? getCurrentTaskHandler().getRevertTaskType().name() : AsyncTaskType.unknown.name());
        log.error("Reverting task '{}', handler '{}'", type, getCurrentTaskHandler().getClass().getName());
    }

    private boolean hasRevertTask() {
        return getCurrentTaskHandler().getRevertTaskType() != null;
    }

    protected void endWithFailure() {
        setSucceeded(true);
        rollbackQuota();
    }

    protected boolean hasParentCommand() {
        return getParameters().getParentCommand() != VdcActionType.Unknown;
    }

    private boolean isCanDoActionSupportsTransaction() {
        return getClass().isAnnotationPresent(CanDoActionSupportsTransaction.class);
    }

    private boolean internalCanDoAction() {
        boolean returnValue = false;
        try {
            Transaction transaction = null;
            if (!isCanDoActionSupportsTransaction()) {
                transaction = TransactionSupport.suspend();
            }
            try {
                returnValue =
                        isUserAuthorizedToRunAction() && isBackwardsCompatible() && validateInputs() && acquireLock()
                                && canDoAction()
                                && internalValidateAndSetQuota();
                if (!returnValue && getReturnValue().getCanDoActionMessages().size() > 0) {
                    log.warn("CanDoAction of action '{}' failed for user {}. Reasons: {}",
                            getActionType(), getUserName(),
                            StringUtils.join(getReturnValue().getCanDoActionMessages(), ','));
                }
            } finally {
                if (transaction != null) {
                    TransactionSupport.resume(transaction);
                }
            }
        } catch (DataAccessException dataAccessEx) {
            log.error("Data access error during CanDoActionFailure.", dataAccessEx);
            addCanDoActionMessage(EngineMessage.CAN_DO_ACTION_DATABASE_CONNECTION_FAILURE);
        } catch (RuntimeException ex) {
            log.error("Error during CanDoActionFailure.", ex);
            addCanDoActionMessage(EngineMessage.CAN_DO_ACTION_GENERAL_FAILURE);
        } finally {
            if (!returnValue) {
                freeLock();
            }
        }
        return returnValue;
    }

    private boolean internalValidateAndSetQuota() {
        // Quota accounting is done only in the most external Command.
        if (!isQuotaDependant()) {
            return true;
        }

        QuotaConsumptionParametersWrapper quotaConsumptionParametersWrapper = new QuotaConsumptionParametersWrapper(this,
                getReturnValue().getCanDoActionMessages());
        quotaConsumptionParametersWrapper.setParameters(getQuotaConsumptionParameters());

        List<QuotaConsumptionParameter> quotaParams = quotaConsumptionParametersWrapper.getParameters();
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

        boolean result = getQuotaManager().consume(quotaConsumptionParametersWrapper);
        setQuotaChanged(result);
        return result;
    }

    protected boolean isQuotaDependant() {
        boolean result;
        if (getActionType().getQuotaDependency() == VdcActionType.QuotaDependency.NONE)
            result = false;
        else if (!isInternalExecution())
            result = true;
        else if (getActionType().isQuotaDependentAsInternalCommand())
            result = true;
        else
            result = false;

        return result;
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
            getReturnValue().getCanDoActionMessages().addAll(messages);
            return false;
        }
        return true;
    }

    /**
     * Set the parameters for bll messages (such as type and action).
     * The parameters should be initialized through the command that is called,
     * instead set them at the canDoAction()
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

    protected boolean isBackwardsCompatible() {
        boolean result = true;
        ActionVersionMap actionVersionMap = DbFacade.getInstance()
                .getActionGroupDao().getActionVersionMapByActionType(getActionType());
        // if actionVersionMap not null check cluster level
        // cluster level ok check storage_pool level
        if (actionVersionMap != null
                && ((getVdsGroup() != null && getVdsGroup().getCompatibilityVersion().compareTo(
                        new Version(actionVersionMap.getcluster_minimal_version())) < 0) ||
                (!"*".equals(actionVersionMap.getstorage_pool_minimal_version()) && getStoragePool() != null && getStoragePool()
                        .getCompatibilityVersion().compareTo(
                                new Version(actionVersionMap.getstorage_pool_minimal_version())) < 0))) {
            result = false;
            addCanDoActionMessage(EngineMessage.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }
        return result;
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
        final Guid permId =
                getDbFacade().getPermissionDao().getEntityPermissions(userId, actionGroup, object, type);
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
     * Checks if the input user and groups is authorized to run the given action on the given object.
     *
     * @param userId
     *            the user to check
     * @param groupIds
     *            the groups to check
     * @param actionGroup
     *            the action group to check
     * @param object
     *            the object to check
     * @param type
     *            the type of the object to check
     * @param ignoreEveryone
     *            if true, the "everyone" will not be considered
     * @return <code>true</code> if the current user is authorized to run the action, <code>false</code> otherwise
     */
    protected boolean checkUserAndGroupsAuthorization(Guid userId,
            Collection<Guid> groupIds,
            final ActionGroup actionGroup,
            final Guid object,
            final VdcObjectType type,
            final boolean ignoreEveryone) {
        // Grant if there is matching permission in the database:
        if (log.isDebugEnabled()) {
            log.debug("Checking whether user '{}' or groups '{}' have action group '{}' on object type '{}'",
                    userId,
                    groupIds,
                    actionGroup,
                    object,
                    type.name());
        }
        final Guid permId =
                getPermissionDao().getEntityPermissionsForUserAndGroups(userId, StringUtils.join(groupIds, ","), actionGroup, object, type, ignoreEveryone);
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
            addCanDoActionMessage(EngineMessage.USER_IS_NOT_LOGGED_IN);
            return false;
        }

        // Get identifiers and types of the objects whose permissions have to be
        // checked:
        final List<PermissionSubject> permSubjects = getPermissionCheckSubjects();

        if (permSubjects == null || permSubjects.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("The set of objects to check is null or empty for action '{}'.", getActionType());
            }
            addCanDoActionMessage(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);

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

    protected boolean checkPermissions(final List<PermissionSubject> permSubjects) {
        for (PermissionSubject permSubject : permSubjects) {
            if (!checkSinglePermission(permSubject, getReturnValue().getCanDoActionMessages())) {
                log.info("No permission found for user '{}' or one of the groups he is member of,"
                        + " when running action '{}', Required permissions are: Action type: '{}' Action group: '{}'"
                        + " Object type: '{}'  Object ID: '{}'.",
                        getCurrentUser().getId(),
                        getActionType(),
                        permSubject.getActionGroup().getRoleType().name(),
                        permSubject.getActionGroup().name(),
                        permSubject.getObjectType().getVdcObjectTranslation(),
                        permSubject.getObjectId());
                return false;
            }
        }
        return true;
    }

    final public boolean checkSinglePermission(PermissionSubject permSubject, Collection<String> messages) {
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
                for (QuotaConsumptionParameter parameter : consumptionParameters) {
                    if (parameter.getQuotaGuid() != null && !Guid.Empty.equals(parameter.getQuotaGuid())
                            && !QuotaConsumptionParameter.QuotaAction.RELEASE.equals(parameter.getQuotaAction())) {
                        quotaPermissionList.add(new PermissionSubject(parameter.getQuotaGuid(),
                                VdcObjectType.Quota,
                                ActionGroup.CONSUME_QUOTA,
                                EngineMessage.USER_NOT_AUTHORIZED_TO_CONSUME_QUOTA));
                    }
                }
            }
        }
    }

    protected boolean canDoAction() {
        return true;
    }

    /**
     * Factory to determine the type of the ReturnValue field
     */
    protected VdcReturnValueBase createReturnValue() {
        return new VdcReturnValueBase();
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

    protected Object getActionReturnValue() {
        return getReturnValue().getActionReturnValue();
    }

    protected boolean isExecutedAsChildCommand() {
        return getParameters().getParentCommand() != VdcActionType.Unknown;
    }

    /**
     * Calculates the proper parameters for the task
     * @param actionType parent command type for which the task is created
     * @param parameters parameter of the creating command
     */
    protected VdcActionParametersBase getParametersForTask(VdcActionType parentCommandType,
                                                           VdcActionParametersBase parameters) {
        // If there is no parent command, the command that its type
        // will be stored in the DB for thr task is the one creating the command
        VdcActionParametersBase parentParameters = parameters.getParentParameters();
        if (parentCommandType == VdcActionType.Unknown || parentParameters == null) {
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

    private boolean executeWithoutTransaction() {
        boolean functionReturnValue = false;
        boolean exceptionOccurred = true;
        try {
            logRunningCommand();
            if (hasTaskHandlers()) {
                getCurrentTaskHandler().execute();
            } else {
                executeCommand();
            }
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
            if (_returnValue.getExecuteFailedMessages().isEmpty()) {
                processExceptionToClient(new EngineFault(e, EngineError.ENGINE));
            }
            log.error("Command '{}' failed: {}", getClass().getName(), e.getMessage());
            log.error("Exception", e);
        } catch (RuntimeException e) {
            processExceptionToClient(new EngineFault(e, EngineError.ENGINE));
            log.error("Command '{}' failed: {}", getClass().getName(), e.getMessage());
            log.error("Exception", e);
        } finally {
            if (!exceptionOccurred) {
                setCommandExecuted();
            }
            // If we failed to execute due to exception or some other reason, we compensate for the failure.
            if (exceptionOccurred || !getSucceeded()) {
                setCommandStatus(CommandStatus.FAILED);
                setSucceeded(false);
                compensate();
            } else {
                // if the command is not an async task and has no custom callback
                // set the status to SUCCEEDED if the status is ACTIVE
                if (getReturnValue().getVdsmTaskIdList().isEmpty() &&
                        getReturnValue().getInternalVdsmTaskIdList().isEmpty() &&
                        getCallback() == null &&
                        commandStatus == CommandStatus.ACTIVE) {
                    setCommandStatus(CommandStatus.SUCCEEDED);
                }
                cleanUpCompensationData();
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

        methodNames.removeAll(ReflectionUtils.getGetterMethodNames(new VdcActionParametersBase()));

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

        if (hasTaskHandlers()) {
            logInfo.append(" Task handler: ").append(getCurrentTaskHandler().getClass().getSimpleName());
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
        for (PermissionSubject permSubject : permissionSubjects) {
            if (permSubject.getObjectId() != null) {
                // Add comma when there are more than one entity
                // affected.
                if (builder.length() != 0) {
                    builder.append(", ");
                }
                builder.append(" ID: ").append(permSubject.getObjectId())
                        .append(" Type: ").append(permSubject.getObjectType());
                if (permSubject.getActionGroup() != null) {
                    builder.append("Action group " + permSubject.getActionGroup().name() + " with role type " + permSubject.getActionGroup().getRoleType().name());
                }
            }
        }
        return builder;
    }

    private void executeActionInTransactionScope() {
        registerRollbackHandler(new DefaultCommandTransactionCompletionListener());

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
        if (getParameters().getParentCommand() != VdcActionType.Unknown
                && getParameters().getParentParameters() != null) {
            CommandEntity commandEntity =
                    CommandCoordinatorUtil.getCommandEntity(getParameters().getParentParameters().getCommandId());
            return commandEntity != null && commandEntity.isCallbackEnabled();
        }

        return false;
    }

    protected final void execute() {
        setCommandStatus(CommandStatus.ACTIVE);

        getReturnValue().setCanDoAction(true);
        getReturnValue().setIsSyncronious(true);

        if (getCallback() != null || parentHasCallback()) {
            persistCommand(getParameters().getParentCommand(), getCallback() != null);
        }

        if (!hasTaskHandlers() || getExecutionIndex() == 0) {
            ExecutionHandler.addStep(getExecutionContext(), StepEnum.EXECUTING, null);
        }

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
                if (noAsyncOperations() && !ExecutionHandler.checkIfJobHasTasks(getExecutionContext())) {
                    ExecutionHandler.endJob(getExecutionContext(), getSucceeded());
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

    protected abstract void executeCommand();

    /**
     * provides the information on child commands
     */
    protected void buildChildCommandInfos() {
    }

    /**
     * calls execute action the child command.
     */
    protected VdcReturnValueBase runCommand(CommandBase<?> command) {
        VdcReturnValueBase returnValue = command.executeAction();
        returnValue.setCorrelationId(command.getParameters().getCorrelationId());
        returnValue.setJobId(command.getJobId());
        return returnValue;
    }

    private void logCommand() {
        Class<?> type = getClass();
        InternalCommandAttribute annotation = type.getAnnotation(InternalCommandAttribute.class);
        if (annotation == null) {
            log();
        }
    }

    private boolean getTransactive() {
        NonTransactiveCommandAttribute annotation = getClass().getAnnotation(NonTransactiveCommandAttribute.class);
        return annotation == null;
    }

    public T getParameters() {
        return _parameters;
    }

    public VdcReturnValueBase getReturnValue() {
        if (_returnValue == null) {
            _returnValue = createReturnValue();
        }
        return _returnValue;
    }

    public void setReturnValue(VdcReturnValueBase returnValue) {
        _returnValue = returnValue;
    }

    public VdcActionType getActionType() {
        try {
            if (actionType == null) {
                String name = getClass().getName();
                name = name.substring(0, name.length() - 7);
                name = name.substring(name.lastIndexOf('.') + 1);
                actionType = VdcActionType.valueOf(name);
            }
            return actionType;
        } catch (Exception e) {
            return VdcActionType.Unknown;
        }
    }

    protected String getDescription() {
        return _description;
    }

    protected void setDescription(String value) {
        _description = value;
    }

    private void processExceptionToClient(EngineFault fault) {
        fault.setSessionID(getParameters().getSessionId());
        _returnValue.getExecuteFailedMessages().add(fault.getError().name());
        _returnValue.setFault(fault);
    }

    Map<String, Guid> taskKeyToTaskIdMap = new HashMap<>();

    public Guid persistAsyncTaskPlaceHolder(VdcActionType parentCommand) {
        return persistAsyncTaskPlaceHolder(parentCommand, DEFAULT_TASK_KEY);
    }

    public Guid persistAsyncTaskPlaceHolder(VdcActionType parentCommand, final String taskKey) {
        Guid taskId = Guid.Empty;
        try {
            AsyncTaskCreationInfo creationInfo = new AsyncTaskCreationInfo();
            if (!hasTaskHandlers()) {
                creationInfo.setTaskType(getTaskType());
            } else {
                creationInfo.setTaskType(getCurrentTaskHandler().getTaskType());
            }
            final AsyncTask task = createAsyncTask(creationInfo, parentCommand);
            taskId = task.getTaskId();
            TransactionScopeOption scopeOption =
                    getTransactive() ? TransactionScopeOption.RequiresNew : TransactionScopeOption.Required;
            TransactionSupport.executeInScope(scopeOption, new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    saveTaskAndPutInMap(taskKey, task);
                    return null;
                }

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
        CommandCoordinatorUtil.saveAsyncTaskToDb(task);
        taskKeyToTaskIdMap.put(taskKey, task.getTaskId());
    }

    private void addToReturnValueTaskPlaceHolderIdList(Guid taskId) {
        if (!getReturnValue().getTaskPlaceHolderIdList().contains(taskId)) {
            getReturnValue().getTaskPlaceHolderIdList().add(taskId);
        }
    }

    public void deleteAsyncTaskPlaceHolder() {
        deleteAsyncTaskPlaceHolder(DEFAULT_TASK_KEY);
    }

    public void deleteAsyncTaskPlaceHolder(String taskKey) {
        Guid taskId = taskKeyToTaskIdMap.remove(taskKey);
        if (!Guid.isNullOrEmpty(taskId)) {
            CommandCoordinatorUtil.removeTaskFromDbByTaskId(taskId);
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
     *            VdcActionType of the command that its endAction we want to invoke when tasks are finished.
     * @param entitiesMap
     *            maps ID of entity to its type.
     * @return Guid of the created task.
     */
    protected Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            Map<Guid, VdcObjectType> entitiesMap) {
        return createTask(taskId, asyncTaskCreationInfo, parentCommand, null, entitiesMap);
    }

    /**
     * Same as {@link #createTask(Guid, AsyncTaskCreationInfo, VdcActionType, VdcObjectType, Guid...)}
     * but without suspending the current transaction.
     *
     * Note: it is better to use {@link #createTask(Guid, AsyncTaskCreationInfo, VdcActionType, VdcObjectType, Guid...)}
     * since it suspend the current transaction, thus the changes are being updated in the
     * DB right away. call this method only you have a good reason for it and
     * the current transaction is short.
     *
     * @see {@link #createTask(Guid, AsyncTaskCreationInfo, VdcActionType, VdcObjectType, Guid...)}
     */
    protected Guid createTaskInCurrentTransaction(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
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
     *            VdcActionType of the command that its endAction we want to invoke when tasks are finished.
     * @return Guid of the created task.
     */
    protected Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return createTask(taskId,
                asyncTaskCreationInfo,
                parentCommand,
                null,
                // The reason Collections.emptyMap is not used here as
                // the map should be mutable
                new HashMap<Guid, VdcObjectType>());
    }

    protected Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType vdcObjectType,
            Guid... entityIds) {

        return createTask(taskId,
                asyncTaskCreationInfo,
                parentCommand,
                createEntitiesMapForSingleEntityType(vdcObjectType, entityIds));
    }

    protected Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
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
     *            VdcActionType of the command that its endAction we want to invoke when tasks are finished.
     * @param description
     *            A message which describes the task
     * @param entitiesMap - map of entities
     */
    protected Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            String description, Map<Guid, VdcObjectType> entitiesMap) {

        Transaction transaction = TransactionSupport.suspend();

        try {
            return createTaskImpl(taskId, asyncTaskCreationInfo, parentCommand, description, entitiesMap);
        } catch (RuntimeException ex) {
            log.error("Error during createTask for command '{}': {}", getClass().getName(), ex.getMessage());
            log.error("Exception", ex);
        } finally {
            TransactionSupport.resume(transaction);
        }

        return Guid.Empty;
    }

    private Guid createTaskImpl(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand,
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
            VdcActionType parentCommand,
            String description,
            Map<Guid, VdcObjectType> entitiesMap) {
        return CommandCoordinatorUtil.createTask(taskId, this, asyncTaskCreationInfo, parentCommand, description, entitiesMap);
    }

    /**
     * Create the {@link SPMATask} object to be run
     * @param taskId the id of the async task place holder in the database
     * @param asyncTaskCreationInfo Info on how to create the task
     * @param parentCommand The type of command issuing the task
     * @return An {@link SPMTask} object representing the task to be run
     */
    public SPMTask concreteCreateTask(
            Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return CommandCoordinatorUtil.concreteCreateTask(taskId, this, asyncTaskCreationInfo, parentCommand);
    }

    public VdcActionParametersBase getParentParameters(VdcActionType parentCommand) {
        VdcActionParametersBase parentParameters = getParametersForTask(parentCommand, getParameters());
        if (parentParameters.getParametersCurrentUser() == null && getCurrentUser() != null) {
            parentParameters.setParametersCurrentUser(getCurrentUser());
        }
        return parentParameters;
    }

    private AsyncTask createAsyncTask(
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return CommandCoordinatorUtil.createAsyncTask(this, asyncTaskCreationInfo, parentCommand);
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

    protected void startPollingAsyncTasks(Collection<Guid> taskIds) {
        for (Guid taskID : taskIds) {
            CommandCoordinatorUtil.startPollingTask(taskID);
        }
    }

    protected boolean noAsyncOperations() {
        return !hasTasks() && getCallback() == null;
    }

    protected void startPollingAsyncTasks() {
        startPollingAsyncTasks(getReturnValue().getVdsmTaskIdList());
    }

    protected ArrayList<Guid> getTaskIdList() {
        return getParameters().getParentCommand() != VdcActionType.Unknown ? getReturnValue().getInternalVdsmTaskIdList()
                : getReturnValue().getVdsmTaskIdList();
    }

    private void cancelTasks() {
        CommandCoordinatorUtil.cancelTasks(this);
    }

    protected void revertTasks() {
        CommandCoordinatorUtil.revertTasks(this);
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
        return LockProperties.create(Scope.None).withWait(false);
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
        LockProperties lockProperties = _parameters.getLockProperties();
        if (lockProperties == null) {
            lockProperties = applyLockProperties(getLockingPropertiesSettings());
            _parameters.setLockProperties(lockProperties);
        }
        return lockProperties;
    }

    protected boolean acquireLock() {
        LockProperties lockProperties = getLockProperties();
        boolean returnValue = true;
        if (!Scope.None.equals(lockProperties.getScope())) {
            releaseLocksAtEndOfExecute = Scope.Execution.equals(lockProperties.getScope());
            if (!lockProperties.isWait()) {
                returnValue = acquireLockInternal();
            } else {
                acquireLockAndWait();
            }
        }
        return returnValue;
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
                Pair<Boolean, Set<String>> lockAcquireResult = getLockManager().acquireLock(lock);
                if (lockAcquireResult.getFirst()) {
                    log.info("Lock Acquired to object '{}'", lock);
                    context.withLock(lock);
                } else {
                    log.info("Failed to Acquire Lock to object '{}'", lock);
                    getReturnValue().getCanDoActionMessages()
                    .addAll(extractVariableDeclarations(lockAcquireResult.getSecond()));
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
    protected List<String> extractVariableDeclarations(Iterable<String> appendedCanDoMsgs) {
        final List<String> result = new ArrayList<>();
        Iterator<String> iter = appendedCanDoMsgs.iterator();
        while(iter.hasNext()) {
            result.addAll(Arrays.asList(iter.next().split("(?=\\$)")));
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

    private void acquireLockAndWait() {
        // if commandLock is null then we acquire new lock, otherwise probably we got lock from caller command.
        if (context.getLock() == null) {
            Map<String, Pair<String, String>> exclusiveLocks = getExclusiveLocks();
            if (exclusiveLocks != null) {
                EngineLock lock = new EngineLock(exclusiveLocks, null);
                getLockManager().acquireLockWait(lock);
                context.withLock(lock);
            }
        }
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
            getLockManager().releaseLock(context.getLock());
            log.info("Lock freed to object '{}'", context.getLock());
            context.withLock(null);
            // free other locks here to guarantee they will be freed only once
            freeCustomLocks();
        }
    }

    /** hook for subclasses that hold additional custom locks */
    protected void freeCustomLocks() {
    }

    protected LockManager getLockManager() {
        return LockManagerFactory.getLockManager();
    }

    /**
     * The following method should return a map which is represent exclusive lock
     */
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return null;
    }

    /**
     * The following method should return a map which is represent shared lock
     */
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return null;
    }

    @Override
    public Object runInTransaction() {
        if (_actionState == CommandActionState.EXECUTE) {
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
            addCanDoActionMessage(validationResult.getMessage());

            for (String variableReplacement : validationResult.getVariableReplacements()) {
                addCanDoActionMessage(variableReplacement);
            }
        }

        return validationResult.isValid();
    }

    /**
     * Add a message to the {@link CommandBase#canDoAction()}'s return value.
     * This return value will be sent to the client for the detailed information
     * of why the action can't be performed.
     *
     * @param message
     *            The message to add.
     */
    protected void addCanDoActionMessage(EngineMessage message) {
        getReturnValue().getCanDoActionMessages().add(message.name());
    }

    /**
     * Adds one or more messages to the {@link CommandBase#canDoAction()}'s return value. This return value will be sent
     * to the client for the detailed information of why the action can't be performed.
     *
     * @param messages
     *            The messages to add.
     */

    protected final void addCanDoActionMessages(EngineMessage... messages) {
        for (EngineMessage msg : messages) {
            addCanDoActionMessage(msg);
        }
    }

    /**
     * Add validation message with variable replacements and return false.
     *
     * @param message   the message to add
     * @param variableReplacements variable replacements
     * @return  false always
     * @see {@link #addCanDoActionMessage(String)}
     */
    protected final boolean failCanDoAction(EngineMessage message, String ... variableReplacements) {
        addCanDoActionMessage(message);
        for (String variableReplacement : variableReplacements) {
            addCanDoActionMessage(variableReplacement);
        }
        return false;
    }

    /**
     * Add a message to the {@link CommandBase#canDoAction()}'s return value.
     * This return value will be sent to the client for the detailed information of why the action can't be performed.
     *
     * @param message The message to add.
     */
    protected void addCanDoActionMessage(String message) {
        getReturnValue().getCanDoActionMessages().add(message);
    }

    /**
     * Add a variable to the {@link CommandBase#canDoAction()}'s return value.
     * The variable will be formatted as "$varName varValue" and will be used to parse the placeholders defined
     * in the canDo message itself
     *
     * @param varName the variable name
     * @param varValue the variable value
     */
    protected void addCanDoActionMessageVariable(String varName, Object varValue) {
        getReturnValue().getCanDoActionMessages().add(String.format("$%s %s", varName, varValue));
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
        return getBackend().getResourceManager().RunVdsCommand(commandType, parameters);
    }

    /**
     * Permissions are attached to object so every command must declare its
     * object target type and its GUID
     *
     * @return Map of GUIDs to Object types
     */
    public abstract List<PermissionSubject> getPermissionCheckSubjects();

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
                    value = DbFacade.getInstance().getEntityNameByIdAndType(entityId, entityType);
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
        return ExecutionHandler.addSubStep(getExecutionContext(),
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

    protected QuotaManager getQuotaManager() {
        return quotaManager;
    }

    protected List<SPMAsyncTaskHandler> getTaskHandlers() {
        return taskHandlers;
    }

    protected void clearTaskHandlers() {
        // For use when combining TaskHandler and CommandExecutor-driven child commands
        taskHandlers = null;
    }

    public boolean hasTaskHandlers() {
        return getTaskHandlers() != null;
    }

    public SPMAsyncTaskHandler getCurrentTaskHandler() {
        return getTaskHandlers().get(getExecutionIndex());
    }

    private int getExecutionIndex() {
        return getParameters().getExecutionIndex();
    }

    private boolean hasStepsToRevert() {
        return getExecutionIndex() >= 0;
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
        if (!_parameters.getMultipleAction()) {
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
    protected void propagateFailure(VdcReturnValueBase internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().addAll(internalReturnValue.getExecuteFailedMessages());
        getReturnValue().setFault(internalReturnValue.getFault());
        getReturnValue().getCanDoActionMessages().addAll(internalReturnValue.getCanDoActionMessages());
        getReturnValue().setCanDoAction(internalReturnValue.getCanDoAction());
    }

    protected void propagateFailure(VdcQueryReturnValue internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().add(internalReturnValue.getExceptionString());
    }

    protected VdcReturnValueBase convertToVdcReturnValueBase(final VDSReturnValue vdsReturnValue) {
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setSucceeded(false);
        returnValue.setActionReturnValue(vdsReturnValue.getReturnValue());
        returnValue.setExecuteFailedMessages(new ArrayList<String>() {
            {
                add(vdsReturnValue.getVdsError().getMessage());
            }
        });
        return returnValue;
    }

    public void persistCommand(VdcActionType parentCommand) {
        persistCommand(parentCommand, getContext(), false);
    }

    public void persistCommand(VdcActionType parentCommand, boolean enableCallback) {
        persistCommand(parentCommand, getContext(), enableCallback);
    }

    protected void persistCommandIfNeeded() {
        if (getCallback() != null || parentHasCallback()) {
            persistCommand(getParameters().getParentCommand());
        }
    }

    public void persistCommand(VdcActionType parentCommand, CommandContext cmdContext, boolean enableCallback) {
        Transaction transaction = TransactionSupport.suspend();
        try {
            CommandEntity commandEntity =
                    buildCommandEntity(getParentParameters(parentCommand).getCommandId(), enableCallback);
            CommandCoordinatorUtil.persistCommand(commandEntity, cmdContext);
        } finally {
            if (transaction != null) {
                TransactionSupport.resume(transaction);
            }
        }
    }

    private CommandEntity buildCommandEntity(Guid rootCommandId, boolean enableCallback) {
        return CommandEntity.buildCommandEntity(getUserId(),
                getCommandId(),
                getParameters().getParentParameters() == null ? Guid.Empty : getParameters().getParentParameters().getCommandId(),
                rootCommandId,
                getExecutionContext() == null || getExecutionContext().getJob() == null ? Guid.Empty : getExecutionContext().getJob().getId(),
                getExecutionContext() == null || getExecutionContext().getStep() == null ? Guid.Empty : getExecutionContext().getStep().getId(),
                getActionType(),
                getParameters(),
                commandStatus,
                enableCallback,
                getReturnValue());
    }

    protected void removeCommand() {
        Transaction transaction = TransactionSupport.suspend();
        try {
            CommandCoordinatorUtil.removeCommand(getCommandId());
        } finally {
            if (transaction != null) {
                TransactionSupport.resume(transaction);
            }
        }
    }

    public void setCommandStatus(CommandStatus status) {
        setCommandStatus(status, true);
    }

    public void setCommandStatus(CommandStatus status, boolean updateDB) {
        this.commandStatus = status;
        if (updateDB) {
            Transaction transaction = TransactionSupport.suspend();
            try {
                CommandCoordinatorUtil.updateCommandStatus(getCommandId(), commandStatus);
            } finally {
                if (transaction != null) {
                    TransactionSupport.resume(transaction);
                }
            }
        }
    }

    public void setCommandExecuted() {
        Transaction transaction = TransactionSupport.suspend();
        try {
            CommandEntity cmdEntity = CommandCoordinatorUtil.getCommandEntity(getCommandId());
            if (cmdEntity != null) {
                CommandCoordinatorUtil.persistCommand(buildCommandEntity(cmdEntity.getRootCommandId(),
                        cmdEntity.isCallbackEnabled()),
                        getContext());
                CommandCoordinatorUtil.updateCommandExecuted(getCommandId());
            }
        } finally {
            if (transaction != null) {
                TransactionSupport.resume(transaction);
            }
        }
    }

    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    public CommandCallback getCallback() {
        return null;
    }

    protected VdcReturnValueBase runInternalAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return getBackend().runInternalAction(actionType, parameters, context.clone());
    }

    protected VdcReturnValueBase runInternalAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CommandContext internalCommandContext) {
        return getBackend().runInternalAction(actionType,
                parameters,
                internalCommandContext);
    }

    protected ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters) {
        return getBackend().runInternalMultipleActions(actionType, parameters, context.clone());
    }

    protected ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            ExecutionContext executionContext) {
        return getBackend().runInternalMultipleActions(actionType,
                parameters,
                context.clone().withExecutionContext(executionContext));
    }

    protected VdcReturnValueBase runInternalActionWithTasksContext(VdcActionType actionType,
            VdcActionParametersBase parameters) {
        return runInternalActionWithTasksContext(actionType, parameters, null);
    }

    protected VdcReturnValueBase runInternalActionWithTasksContext(VdcActionType actionType,
            VdcActionParametersBase parameters, EngineLock lock) {
        return runInternalAction(
                actionType,
                parameters,
                ExecutionHandler.createDefaultContextForTasks(getContext(), lock));
    }

    protected VdcQueryReturnValue runInternalQuery(VdcQueryType type, VdcQueryParametersBase queryParams) {
        return getBackend().runInternalQuery(type, queryParams, context.getEngineContext());
    }

    protected CommandContext cloneContext() {
        return getContext().clone();
    }


    public CommandContext cloneContextAndDetachFromParent() {
        return cloneContext().withoutCompensationContext().withoutExecutionContext().withoutLock();
    }

    protected MacPoolManagerStrategy getMacPool() {
        return MacPoolPerDcSingleton.getInstance().poolForDataCenter(getStoragePoolId());
    }

    protected long getEngineSessionSeqId() {
        String sessionId = getParameters().getSessionId();
        if (sessionId == null && getContext() != null) {
            sessionId = getContext().getEngineContext().getSessionId();
        }
        if (sessionId == null) {
            throw new RuntimeException("No sessionId found for command " + getClass().getName());
        }
        return SessionDataContainer.getInstance().getEngineSessionSeqId(sessionId);
    }

    public VdcActionParametersBase getParentParameters() {
        VdcActionParametersBase parentParameters = getParameters();
        if (parentHasCallback()) {
            if (!getParameters().getShouldBeEndedByParent() || getTaskType() == AsyncTaskType.notSupported) {
                parentParameters = getParameters().getParentParameters();
            }
        } else if (getParameters().getShouldBeEndedByParent()) {
            parentParameters = getParameters().getParentParameters();
        }
        return parentParameters;
    }

    protected <P extends VdcActionParametersBase> P withRootCommandInfo(P params, VdcActionType actionType) {
        VdcActionType parentCommand = getParameters().getParentCommand() != VdcActionType.Unknown ?
                getParameters().getParentCommand() : actionType;
        params.setParentParameters(getParametersForTask(parentCommand, getParameters()));
        params.setParentCommand(parentCommand);
        return params;
    }

    private class DefaultCommandTransactionCompletionListener extends NoOpTransactionCompletionListener {

        @Override
        public void onRollback() {
            log.error("Transaction rolled-back for command '{}'.", CommandBase.this.getClass().getName());
            try {
                if (isQuotaDependant()) {
                    rollbackQuota();
                }
            } catch (NullPointerException e) {
                log.error("RollbackQuota: failed (may be because quota is disabled)", e);
            }
            cancelTasks();
        }

    }
}
