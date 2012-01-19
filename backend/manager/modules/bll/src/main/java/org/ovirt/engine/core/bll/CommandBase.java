package org.ovirt.engine.core.bll;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.context.DefaultCompensationContext;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.EntityStatusSnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.action_version_map;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.interfaces.IBackendCallBackServer;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.dbbroker.generic.RepositoryException;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.StatusAwareDao;
import org.ovirt.engine.core.utils.Deserializer;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.RollbackHandler;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.springframework.dao.DataAccessException;

public abstract class CommandBase<T extends VdcActionParametersBase> extends AuditLogableBase implements
        RollbackHandler, TransactionMethod<Object> {
    /**
     * Multiplier used to convert GB to bytes or vice versa.
     */
    protected static final long BYTES_IN_GB = 1024 * 1024 * 1024;
    private T _parameters;
    private VdcReturnValueBase _returnValue;
    private final IBackendCallBackServer _backendCallBack = CallbackServer.Instance;
    private CommandActionState _actionState = CommandActionState.forValue(0);
    private boolean isInternalExecution = false;
    private VdcActionType actionType;
    /**
     * According to hibernate validator documentation it is safe to assume it is
     * thread-safe.So holding one is encouraged.
     */
    private final static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final List<Class<?>> validationGroups =
            new ArrayList<Class<?>>(Arrays.asList(new Class<?>[] { Default.class }));
    private CompensationContext compensationContext;
    private Guid commandId = Guid.NewGuid();

    protected LogCompat log = LogFactoryCompat.getLog(getClass());

    protected CommandActionState getActionState() {
        return _actionState;
    }

    protected CommandBase() {
    }

    protected CommandBase(T parameters) {
        _parameters = parameters;
        setCurrentUser(addUserToThredContext(parameters.getSessionId()));
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected CommandBase(Guid commandId) {
        this.commandId = commandId;
    }

    /**
     * Create an appropriate compensation context. The default is one that does nothing for command that don't run in a
     * transaction, and a real one for commands that run in a transaction.
     *
     * @param transactionScopeOption
     *            The transaction scope.
     * @param forceCompensation
     * @return The compensation context to use.
     */
    private CompensationContext createCompensationContext(TransactionScopeOption transactionScopeOption,
            boolean forceCompensation) {
        if (transactionScopeOption == TransactionScopeOption.Suppress && !forceCompensation) {
            return new NoOpCompensationContext();
        }

        DefaultCompensationContext context = new DefaultCompensationContext();
        context.setCommandId(commandId);
        context.setCommandType(getClass().getName());
        context.setBusinessEntitySnapshotDAO(getBusinessEntitySnapshotDAO());
        context.setSnapshotSerializer(
                SerializationFactory.getFactory().createSerializer());
        return context;
    }

    protected BusinessEntitySnapshotDAO getBusinessEntitySnapshotDAO() {
        return DbFacade.getInstance().getBusinessEntitySnapshotDAO();
    }

    /**
     * @return the compensationContext
     */
    protected CompensationContext getCompensationContext() {
        return compensationContext;
    }

    /**
     * @param compensationContext the compensationContext to set
     */
    public void setCompensationContext(CompensationContext compensationContext) {
        this.compensationContext = compensationContext;
    }

    /**
     * This method will add a user to thread local, at case that user is not
     * already added to context. If session is null or empty will try to get
     * session from thread local
     *
     * @param sessionId
     *            -id of session
     */
    private IVdcUser addUserToThredContext(String sessionId) {
        IVdcUser vdcUser = ThreadLocalParamsContainer.getVdcUser();
        if (vdcUser == null) {
            if (!StringHelper.isNullOrEmpty(sessionId)) {
                vdcUser = (IVdcUser) SessionDataContainer.getInstance().GetData(sessionId, "VdcUser");
                ThreadLocalParamsContainer.setHttpSessionId(sessionId);
            } else {
                vdcUser = (IVdcUser) SessionDataContainer.getInstance().GetData("VdcUser");
            }
            ThreadLocalParamsContainer.setVdcUser(vdcUser);
        }
        return vdcUser;
    }

    private String _description = "";
    private TransactionScopeOption scope;
    private TransactionScopeOption endActionScope;

    public VdcReturnValueBase CanDoActionOnly() {
        setActionMessageParameters();
        getReturnValue().setCanDoAction(InternalCanDoAction());
        String tempVar = getDescription();
        getReturnValue().setDescription((tempVar != null) ? tempVar : getReturnValue().getDescription());
        return _returnValue;
    }

    public VdcReturnValueBase ExecuteAction() {
        _actionState = CommandActionState.EXECUTE;

        String tempVar = getDescription();
        getReturnValue().setDescription((tempVar != null) ? tempVar : getReturnValue().getDescription());
        setActionMessageParameters();
        try {
            if (acquireLock() && (getReturnValue().getCanDoAction() || InternalCanDoAction())) {
                getReturnValue().setCanDoAction(true);
                getReturnValue().setIsSyncronious(true);
                getParameters().setTaskStartTime(System.currentTimeMillis());
                Execute();
            } else {
                getReturnValue().setCanDoAction(false);
            }
        } finally {
            freeLock();
        }
        return getReturnValue();
    }

    /**
     * Run the default compensation logic (inside a new transaction):<br>
     * <ol>
     * <li>Get all the entity snapshots that this command has created.</li>
     * <li>For each snapshot:</li>
     * <ol>
     * <li>Deserialize the entity.</li>
     * <li>Using the entity DAO:</li>
     * <ul>
     * <li>If the entity was added by the command, remove it.</li>
     * <li>Otherwise, If the entity is not in DB anymore, restore it.</li>
     * <li>Otherwise, update it.</li>
     * </ul>
     * </ol>
     * <li>Remove all the snapshots for this command, since we handled them.</li> </ol>
     */
    @SuppressWarnings("unchecked")
    protected void compensate() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                Deserializer deserializer =
                        SerializationFactory.getFactory().createDeserializer();
                List<BusinessEntitySnapshot> entitySnapshots =
                        getBusinessEntitySnapshotDAO().getAllForCommandId(commandId);
                log.debugFormat("Command [id={0}]: {1} compensation data.", commandId,
                        entitySnapshots.isEmpty() ? "No" : "Going over");
                for (BusinessEntitySnapshot snapshot : entitySnapshots) {
                    Class<Serializable> snapshotClass = (Class<Serializable>)ReflectionUtils.getClassFor(snapshot.getSnapshotClass());
                    Serializable snapshotData = deserializer.deserialize(snapshot.getEntitySnapshot(), snapshotClass);
                    log.infoFormat("Command [id={0}]: Compensating {1} of {2}; snapshot: {3}.",
                            commandId,
                            snapshot.getSnapshotType(),
                            snapshot.getEntityType(),
                            (snapshot.getSnapshotType() == SnapshotType.CHANGED_ENTITY ? "id=" + snapshot.getEntityId()
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
                    case CHANGED_ENTITY:
                        BusinessEntity<Serializable> entitySnapshot = (BusinessEntity<Serializable>) snapshotData;
                        if (daoForEntity.get(entitySnapshot.getId()) == null) {
                            daoForEntity.save(entitySnapshot);
                        } else {
                            daoForEntity.update(entitySnapshot);
                        }
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
        getBusinessEntitySnapshotDAO().removeAllForCommandId(commandId);
    }

    public VdcReturnValueBase EndAction() {
        try {
            SetActionState();
            handleTransactivity();
            TransactionSupport.executeInScope(endActionScope, this);
        } catch (TransactionRolledbackLocalException e) {
            log.infoFormat("EndAction: Transaction was aborted in {0}", this.getClass().getName());
        } finally {
            if (getCommandShouldBeLogged()) {
                LogCommand();
            }
        }

        return getReturnValue();
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

        if (compensationContext == null) {
            compensationContext = createCompensationContext(scope, forceCompensation);
        }
    }

    private void SetActionState() {
        if (getParameters().getTaskGroupSuccess()) {
            _actionState = CommandActionState.END_SUCCESS;
        } else {
            _actionState = CommandActionState.END_FAILURE;
        }
    }

    public void endActionInTransactionScope() {
        try {
            if (getParameters().getTaskGroupSuccess()) {
                InternalEndSuccessfully();
            } else {
                InternalEndWithFailure();
            }
        } finally {
            if (TransactionSupport.current() == null) {
                cleanUpCompensationData();
            } else {
                try {
                    if (TransactionSupport.current().getStatus() == Status.STATUS_ACTIVE) {
                        cleanUpCompensationData();
                    } else {
                        compensate();
                    }
                } catch (SystemException e) {
                    log.errorFormat("Exception while wrapping-up compensation in endAction: {0}.",
                            ExceptionUtils.getMessage(e), e);
                    compensate();
                }
            }
        }
    }

    private void InternalEndSuccessfully() {
        log.infoFormat("Ending command successfully: {0}", getClass().getName());
        EndSuccessfully();
    }

    protected void EndSuccessfully() {
        setSucceeded(true);
    }

    private void InternalEndWithFailure() {
        log.errorFormat("Ending command with failure: {0}", getClass().getName());
        EndWithFailure();
    }

    protected void EndWithFailure() {
        setSucceeded(true);
    }

    private boolean InternalCanDoAction() {
        try {
            boolean returnValue;
            Transaction transaction = TransactionSupport.suspend();
            try {
                returnValue =
                        IsUserAutorizedToRunAction() && IsBackwardsCompatible() && validateInputs() && canDoAction();
                if (!returnValue && getReturnValue().getCanDoActionMessages().size() > 0) {
                    log.warnFormat("CanDoAction of action {0} failed. Reasons:{1}", getActionType(),
                            StringHelper.aggregate(getReturnValue().getCanDoActionMessages(), ','));
                }
            } finally {
                TransactionSupport.resume(transaction);
            }
            return returnValue;
        } catch (DataAccessException dataAccessEx) {
            log.error("Data access error during CanDoActionFailure.", dataAccessEx);
            addCanDoActionMessage(VdcBllMessages.CAN_DO_ACTION_DATABASE_CONNECTION_FAILURE);
            return false;
        }
        catch (RuntimeException ex) {
            log.error("Error during CanDoActionFailure.", ex);
            addCanDoActionMessage(VdcBllMessages.CAN_DO_ACTION_GENERAL_FAILURE);
            return false;
        }

    }

    /**
     * @return true if all parameters class and its inner members passed
     *         validation
     */
    protected boolean validateInputs() {
        List<Class<?>> validationGroupList = getValidationGroups();
        Set<ConstraintViolation<T>> violations =
                validator.validate(getParameters(),
                        ((Class<?>[]) validationGroupList.toArray(new Class<?>[validationGroupList.size()])));
        if (!violations.isEmpty()) {
            ArrayList<String> msgs = getReturnValue().getCanDoActionMessages();
            for (ConstraintViolation<T> constraintViolation : violations) {
                msgs.add(constraintViolation.getMessage());
            }
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
    }

    protected List<Class<?>> getValidationGroups() {
        return validationGroups;
    };

    protected List<Class<?>> addValidationGroup(Class<?>... validationGroup) {
        validationGroups.addAll(Arrays.asList(validationGroup));
        return validationGroups;
    }

    protected boolean IsBackwardsCompatible() {
        boolean result = true;
        action_version_map actionVersionMap = DbFacade.getInstance()
                .getActionGroupDAO().getActionVersionMapByActionType(getActionType());
        // if actionVersionMap not null check cluster level
        // cluster level ok check storage_pool level
        if (actionVersionMap != null
                && ((getVdsGroup() != null && getVdsGroup().getcompatibility_version().compareTo(
                        new Version(actionVersionMap.getcluster_minimal_version())) < 0) || (!StringHelper.EqOp(
                        actionVersionMap.getstorage_pool_minimal_version(), "*") && getStoragePool() != null && getStoragePool()
                        .getcompatibility_version().compareTo(
                                new Version(actionVersionMap.getstorage_pool_minimal_version())) < 0))) {
            result = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }
        return result;
    }

    /**
     * Check if current user is authorized to run current action. skip check if
     * MLA is off or command is internal
     *
     * @return
     */
    protected boolean IsUserAutorizedToRunAction() {
        boolean returnValue = true;
        // skip internal actions and MLA is off
        if (isInternalExecution || !Config.<Boolean> GetValue(ConfigValues.IsMultilevelAdministrationOn)) {
            if (log.isDebugEnabled()) {
                log.debugFormat(
                        "IsUserAutorizedToRunAction: Internal action or MLA is off - permission check skipped for action {0}",
                        getActionType());
            }
        } // check ActionGroup defined for this action
        else if (getActionType().getActionGroup() == null) {
            if (log.isDebugEnabled()) {
                returnValue = false;
                log.debugFormat(
                        "IsUserAutorizedToRunAction: No ActionGroup defiend for action {0} - check cannot proceed",
                        getActionType().toString());
            }
        } else if (getCurrentUser() != null) {
            // get subjects to check permissions on
            Map<Guid, VdcObjectType> permSubjects = getPermissionCheckSubjects();
            if (permSubjects == null || permSubjects.isEmpty()) {
                returnValue = false;
                if (log.isDebugEnabled()) {
                    log.debugFormat(
                            "IsUserAutorizedToRunAction: PermissionCheckSubjects is null or empty for action {0}",
                            getActionType());
                }
            } else {
                for (Map.Entry<Guid, VdcObjectType> entry : permSubjects.entrySet()) {
                    // if objectId is null we can't check permission
                    if (entry.getKey() == null) {
                        returnValue = false;
                        if (log.isDebugEnabled()) {
                            log.debugFormat(
                                    "IsUserAutorizedToRunAction: Object from PermissionCheckSubjects is null for action {0} permission check failed.",
                                    getActionType());
                        }
                        break;
                    }
                    NGuid permId = DbFacade.getInstance().getEntityPermissions(getCurrentUser().getUserId(),
                            getActionType().getActionGroup(), entry.getKey(), entry.getValue());
                    if (permId != null) {
                        if (log.isDebugEnabled()) {
                            log.debugFormat(
                                    "IsUserAutorizedToRunAction: found permission {0} for user when running {1}, on {2} with id {3}",
                                    permId,
                                    getActionType(),
                                    entry.getValue().getVdcObjectTranslation(),
                                    entry.getKey());
                        }
                    } else {
                        returnValue = false;
                        if (log.isDebugEnabled()) {
                            log.debugFormat(
                                    "IsUserAutorizedToRunAction: no permission found for user when running {0}, on {1} with id {2}",
                                    getActionType(),
                                    entry.getValue().getVdcObjectTranslation(),
                                    entry.getKey());
                        }
                        break;
                    }
                }
            }
        } else {
            addCanDoActionMessage(VdcBllMessages.USER_IS_NOT_LOGGED_IN);
            return false;
        }
        if (returnValue == false) {
            addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
        }
        return returnValue;
    }

    protected List<tags> GetTagsAttachedToObject() {
        // tags_permissions_map
        return new java.util.ArrayList<tags>();
    }

    protected boolean canDoAction() {
        return true;
    }

    /**
     * Factory to determine the type of the ReturnValue field
     *
     * @return
     */
    protected VdcReturnValueBase CreateReturnValue() {
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

    public TimeSpan getTransactionTimeout() {
        return new TimeSpan(1, 1, 0);
    }

    /**
     * Calculates the proper parameters for the task
     * @param parentCommandType parent command type for which the task is created
     * @param parameters parameter of the creating command
     * @return
     */
    protected VdcActionParametersBase getParametersForTask(VdcActionType parentCommandType, VdcActionParametersBase parameters) {
        //If there is no parent command, the command that its type
        //will be stored in the DB for thr task is the one creating the command
        if (parentCommandType == VdcActionType.Unknown) {
            return parameters;
        }
        VdcActionParametersBase parentParameters = parameters.getParentParameters();
        if (parentParameters == null) {
            String msg = "No parameters exist for " + parentCommandType;
            log.error(msg);
            throw new VdcBLLException(VdcBllErrors.NO_PARAMETERS_FOR_TASK,msg);
        }
        return parentParameters;
    }

    private boolean ExecuteWithoutTransaction() {
        boolean functionReturnValue = false;
        boolean exceptionOccurred = true;
        try {
            logRunningCommand();
            executeCommand();
            functionReturnValue = getSucceeded();
            exceptionOccurred = false;
        } catch (RepositoryException e) {
            log.error(String.format("Command %1$s throw Database exception", getClass().getName()), e);
            ProcessExceptionToClient(new VdcFault(e, VdcBllErrors.DB));
        }
        // catch (LicenseException e)
        // {
        // log.error(
        // string.Format("Command {0} throw License exception", GetType().Name),
        // e);
        // ProcessExceptionToClient(new VdcFault(e, VdcBllErrors.LICENSE));

        // }
        catch (VdcBLLException e) {
            log.error(String.format("Command %1$s throw Vdc Bll exception. With error message %2$s",
                    getClass().getName(),
                    e.getMessage()));
            if (log.isDebugEnabled()) {
                log.debug(String.format("Command %1$s throw Vdc Bll exception", getClass().getName()), e);
            }
            ProcessExceptionToClient(new VdcFault(e, e.getVdsError().getCode()));
        } catch (RuntimeException e) {
            ProcessExceptionToClient(new VdcFault(e, VdcBllErrors.ENGINE));
            log.error(String.format("Command %1$s throw exception", getClass().getName()), e);
        } finally {
            // If we failed to execute due to exception or some other reason, we compensate for the failure.
            if (exceptionOccurred || !getSucceeded()) {
                compensate();
            } else {
                cleanUpCompensationData();
            }
        }
        return functionReturnValue;
    }

    protected TransactionScopeOption getTransactionScopeOption() {
        return getParameters().getTransactionScopeOption();
    }

    /**
     * Log the running command , and log the affected entity id and type (if
     * there are any).
     */
    private void logRunningCommand() {
        // Set start of log for running command.
        StringBuilder logInfo = new StringBuilder("Running command: ")
                .append(getClass().getSimpleName()).append(" internal: ")
                .append(isInternalExecution).append(".");

        // Get permissions of object ,to get object id.
        Map<Guid, VdcObjectType> permSubjects = getPermissionCheckSubjects();

        // Log if there is entry in the permission map.
        if (permSubjects != null && !permSubjects.isEmpty()) {
            // Build entities string for entities affected by this operation.
            StringBuilder logEntityIdsInfo = new StringBuilder();

            // Iterate all over the entities , which should be affected.
            for (Map.Entry<Guid, VdcObjectType> entry : permSubjects
                    .entrySet()) {
                if (entry.getKey() != null) {
                    // Add comma when there are more then one entity
                    // affected.
                    if (logEntityIdsInfo.length() != 0) {
                        logEntityIdsInfo.append(", ");
                    }
                    logEntityIdsInfo.append(" ID: ").append(entry.getKey())
                            .append(" Type: ").append(entry.getValue());
                }
            }

            // If found any entities, add the log to the logInfo.
            if (logEntityIdsInfo.length() != 0) {
                // Print all the entities affected.
                logInfo.append(" Entities affected : ").append(
                        logEntityIdsInfo);
            }
        }

        // Log the final appended message to the log.
        log.info(logInfo);
    }

    private void executeActionInTransactionScope() {
        if (TransactionSupport.current() != null) {
            TransactionSupport.registerRollbackHandler(CommandBase.this);
        }

        // If we didn't managed to acquire lock for command or the object wasn't managed to execute properly, then
        // rollback the transaction.
        if (!ExecuteWithoutTransaction()) {
            if (TransactionSupport.current() == null) {
                cancelTasks();
            }

            // we don't want to commit transaction here
            TransactionSupport.setRollbackOnly();
        }
    }

    private void Execute() {
        try {
            handleTransactivity();
            TransactionSupport.executeInScope(scope, this);
        } catch (TransactionRolledbackLocalException e) {
            log.infoFormat("Transaction was aborted in {0}", this.getClass().getName());
            //Transaction was aborted - we must sure we compensation for all previous applicative stages of the command
            compensate();
        } finally {
            freeLock();
            if (getCommandShouldBeLogged()) {
                LogCommand();
            }
            if (getSucceeded()) {
                // only after creating all tasks, we can start polling them (we
                // don't want
                // to start polling before all tasks were created, otherwise we
                // might change
                // the VM/VmTemplate status to 'Down'/'OK' too soon.
                UpdateTasksWithActionParameters();

                StartPollingAsyncTasks();
            }
        }
    }

    private boolean getForceCompensation() {
        NonTransactiveCommandAttribute annotation = getClass().getAnnotation(NonTransactiveCommandAttribute.class);
        return annotation != null && annotation.forceCompensation();
    }

    protected abstract void executeCommand();

    private void LogCommand() {
        Class<?> type = getClass();
        // Object[] attributes = new Object[] {}; // FIXED
        // type.GetCustomAttributes(InternalCommandAttribute.class, false);
        InternalCommandAttribute annotation = type.getAnnotation(InternalCommandAttribute.class);
        if (annotation == null) {
            log();
        }
    }

    private boolean getTransactive() {
        // Object[] attributes = new Object[] {}; // FIXED
        // getClass().GetCustomAttributes(NonTransactiveCommandAttribute.class,
        // true);
        NonTransactiveCommandAttribute annotation = getClass().getAnnotation(NonTransactiveCommandAttribute.class);
        return annotation == null;
    }

    protected static java.util.Date getNow() {
        long nowMiliSeconds = System.currentTimeMillis();
        return new java.util.Date(nowMiliSeconds);
    }

    protected T getParameters() {
        return _parameters;
    }

    public VdcReturnValueBase getReturnValue() {
        if (_returnValue == null) {
            _returnValue = CreateReturnValue();
        }
        return _returnValue;
    }

    protected VdcActionType getActionType() {
        try {
            if (actionType == null) {
                String name = getClass().getName();
                name = name.substring(0, name.length() - 7);
                name = name.substring(name.lastIndexOf('.') + 1);
                actionType = VdcActionType.valueOf(name);
            }
            return actionType;
        } catch (java.lang.Exception e) {
            return VdcActionType.Unknown;
        }
    }

    protected String getDescription() {
        return _description;
    }

    protected void setDescription(String value) {
        _description = value;
    }

    private void ProcessExceptionToClient(VdcFault fault) {
        fault.setSessionID(getParameters().getSessionId());

        if (getParameters().getMultipleAction()) {
            if (_backendCallBack != null) {
                try {
                    Guid FaultQueryId = _backendCallBack.BackendException(getActionType(), fault);
                    BackendCallBacksDirector.getInstance().RegisterFaultQuery(FaultQueryId, fault.getSessionID());
                } catch (RuntimeException ex) {
                    log.errorFormat("{0}", ex);
                }
            }
        } else {
            _returnValue.setFault(fault);
        }
    }

    /**
     * Use this method in order to create task in the AsyncTaskManager in a safe
     * way. If you use this method within a certain command, make sure that the
     * command implemented the ConcreteCreateTask method.
     *
     * @param asyncTaskCreationInfo
     *            info to send to AsyncTaskManager when creating the task.
     * @param parentCommand
     *            VdcActionType of the command that its EndAction we want to
     *            invoke when tasks are finished.
     * @return Guid of the created task.
     */
    protected Guid CreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        Guid retValue = Guid.Empty;

        Transaction transaction = TransactionSupport.suspend();
        try {
            try {
                retValue = ConcreteCreateTask(asyncTaskCreationInfo, parentCommand);
            } catch (RuntimeException ex) {
                log.errorFormat("Error during CreateTask for command: {0}. Exception {1}", getClass().getName(), ex);
            }
        } finally {
            TransactionSupport.resume(transaction);
        }

        return retValue;
    }

    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        throw new NotImplementedException();
    }

    protected void UpdateTasksWithActionParameters() {
        for (Guid taskID : getReturnValue().getTaskIdList()) {
            AsyncTaskManager.getInstance().UpdateTaskWithActionParameters(taskID, getParameters());
        }
    }

    private void StartPollingAsyncTasks() {

        for (Guid taskID : getReturnValue().getTaskIdList()) {
            AsyncTaskManager.getInstance().StartPollingTask(taskID);
        }
    }

    protected java.util.ArrayList<Guid> getTaskIdList() {
        return getParameters().getParentCommand() != VdcActionType.Unknown ? getReturnValue().getInternalTaskIdList()
                : getReturnValue().getTaskIdList();
    }

    @Override
    public void Rollback() {
        log.errorFormat("Transaction rolled-back for command: {0}.", CommandBase.this.getClass().getName());
        cancelTasks();
    }

    private void cancelTasks() {
        if (!getReturnValue().getTaskIdList().isEmpty()) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    String threadName = Thread.currentThread().getName();
                    Thread.currentThread().setName("Rollback-" + threadName);
                    TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            try {
                                AsyncTaskManager.getInstance().CancelTasks(getReturnValue().getTaskIdList());
                            } catch (java.lang.Exception e) {
                                log.errorFormat("Failed to cancel tasks for command: {0}.",
                                        CommandBase.this.getClass().getName());
                            }
                            return null;
                        }
                    });
                }
            });
        }
    }

    protected void RevertTasks() {
        if (getParameters().getTaskIds() != null) {
            // list to send to the PollTasks mathod
            java.util.ArrayList<Guid> taskIdAsList = new java.util.ArrayList<Guid>();
            for (Guid taskId : getParameters().getTaskIds()) {
                taskIdAsList.add(taskId);
                java.util.ArrayList<AsyncTaskStatus> tasksStatuses = AsyncTaskManager.getInstance().PollTasks(
                        taskIdAsList);
                // call revert task only if ended succeesfully
                if (tasksStatuses.get(0).getTaskEndedSuccessfully()) {
                    SPMTaskGuidBaseVDSCommandParameters tempVar = new SPMTaskGuidBaseVDSCommandParameters(
                            getStoragePool().getId(), taskId);
                    tempVar.setCompatibilityVersion(getStoragePool().getcompatibility_version().toString());
                    Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.SPMRevertTask, tempVar);
                }
                taskIdAsList.clear();
            }
        }
    }

    protected Guid getObjectLockingId() {
        Guid returnValue = Guid.Empty;
        Class<?> type = getClass();
        LockIdNameAttribute annotation = type.getAnnotation(LockIdNameAttribute.class);
        if (annotation != null) {
            PropertyInfo propertyInfo = TypeCompat.GetProperty(type, annotation.fieldName());
            if (propertyInfo != null) {
                returnValue = (Guid) propertyInfo.GetValue(this, Guid.Empty);

            }

        }
        return returnValue;
    }

    /**
     * Object which is representing a lock that some commands will acquire
     */
    private EngineLock commandLock = null;

    protected boolean acquireLock() {
        Guid objectLockingId = getObjectLockingId();
        boolean returnValue = true;
        if (!Guid.Empty.equals(objectLockingId)) {
            EngineLock lock = new EngineLock();
            String currType = getClass().getName();
            Map<String, Guid> exclusiveLock = Collections.singletonMap(currType, objectLockingId);
            lock.setExclusiveLocks(exclusiveLock);
            if (LockManagerFactory.getLockManager().acquireLock(lock)) {
                log.infoFormat("Lock Acquired to object {0}", lock);
                commandLock = lock;
            } else {
                log.infoFormat("Failed to Acquire Lock to object {0}", lock);
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED);
                returnValue = false;
            }
        }
        return returnValue;

    }

    protected void freeLock() {
        if (commandLock != null) {
            LockManagerFactory.getLockManager().releaseLock(commandLock);
            log.infoFormat("Lock freed to object {0}", commandLock);
            commandLock = null;
        }
    }

    @Override
    public Object runInTransaction() {
        if (_actionState == CommandActionState.EXECUTE) {
            executeActionInTransactionScope();
            return null;
        } else {
            endActionInTransactionScope();
            return null;
        }
    }

    protected boolean isInternalExecution() {
        return isInternalExecution;
    }

    public void setInternalExecution(boolean isInternalExecution) {
        this.isInternalExecution = isInternalExecution;
    }

    /**
     * Add a message to the {@link CommandBase#canDoAction()}'s return value.
     * This return value will be sent to the client for the detailed information
     * of why the action can't be performed.
     *
     * @param message
     *            The message to add.
     */
    protected void addCanDoActionMessage(VdcBllMessages message) {
        getReturnValue().getCanDoActionMessages().add(message.name());
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
     * Run the given command in the VDS and return the VDS's response.
     *
     * @param commandType
     *            The command to run.
     * @param parameters
     *            The corresponding parameters for the command.
     * @return The return from the VDS, containing success/failure, async task ids (in case of success), or error data
     *         (in case of failure).
     * @throws VdcBLLException
     *             In case of an unhandled exception (Usually more severe than failure of the command, because we don't
     *             know why).
     */
    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters)
            throws VdcBLLException {
        return Backend.getInstance().getResourceManager().RunVdsCommand(commandType, parameters);
    }

    /**
     * Permissions are attached to object so every command must declare its
     * object target type and its GUID
     *
     * @return Map of Guids to Object types
     */
    public abstract Map<Guid, VdcObjectType> getPermissionCheckSubjects();

}
