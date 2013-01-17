package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryCleanupManager;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.EngineWorkingMode;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.searchbackend.BaseConditionFieldAutoCompleter;
import org.ovirt.engine.core.utils.ErrorTranslatorImpl;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.ThreadLocalSessionCleanerInterceptor;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

// Here we use a Singleton Bean
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access
// to all the business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state
// of the singleton is synchronized across all clients.
@DependsOn("Scheduler")
@Local({ BackendLocal.class, BackendInternal.class })
@Interceptors({ ThreadLocalSessionCleanerInterceptor.class })
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Backend implements BackendInternal {

    private ITagsHandler mTagsHandler;
    private ErrorTranslator errorsTranslator;
    private ErrorTranslator _vdsErrorsTranslator;
    private DateTime _startedAt;
    private static boolean firstInitialization = true;

    public static BackendInternal getInstance() {
        return EjbUtils.findBean(BeanType.BACKEND, BeanProxyType.LOCAL);
    }

    private void InitHandlers() {
        mTagsHandler = HandlersFactory.createTagsHandler();
        BaseConditionFieldAutoCompleter.TagsHandler = mTagsHandler;
        VmHandler.Init();
        VdsHandler.Init();
        VmTemplateHandler.Init();
    }

    private VDSBrokerFrontend _resourceManger;

    @Override
    @ExcludeClassInterceptors
    public VDSBrokerFrontend getResourceManager() {
        return _resourceManger;
    }

    /**
     * This method is called upon the bean creation as part of the management Service bean lifecycle.
     */
    @PostConstruct
    public void create() {
        checkDBConnectivity();
        Initialize();
    }

    private static void checkDBConnectivity() {
        boolean dbUp = false;
        long expectedTimeout =
                System.currentTimeMillis()
                        + DbFacade.getInstance().getOnStartConnectionTimeout();
        long waitBetweenInterval = DbFacade.getInstance().getConnectionCheckInterval();
        while (!dbUp && System.currentTimeMillis() < expectedTimeout) {
            try {
                dbUp = DbFacade.getInstance().CheckDBConnection();
            } catch (RuntimeException ex) {
                log.error("Error in getting DB connection. The database is inaccessible. " +
                        "Original exception is: " + ExceptionUtils.getMessage(ex));
                try {
                    Thread.sleep(waitBetweenInterval);
                } catch (InterruptedException e) {
                    log.warn("Failed to wait between connection polling attempts. " +
                            "Original exception is: " + ExceptionUtils.getMessage(e));
                }
            }

        }
        if (!dbUp) {
            throw new IllegalStateException("Could not obtain connection to the database." +
                    " Please make sure that DB is up and accepting connections, and " +
                    "restart the application.");
        }
    }

    @Override
    public DateTime getStartedAt() {
        return _startedAt;
    }

    /**
     * Initializes internal data
     * <exception>VdcBLL.VdcBLLException
     */
    @Override
    public void Initialize() {
        log.infoFormat("Start time: {0}", new Date());
        // When getting a proxy to this bean using JBoss embedded, the initialize method is called for each method
        // invocation on the proxy, as it is called by setup method which is @PostConstruct - the initialized flag
        // makes sure that initialization occurs only once per class (which is ok, as this is a @Service)
        if (firstInitialization) {
            // In case of a server termination that had uncompleted compensation-aware related commands
            // we have to get all those commands and call compensate on each
            compensate();
            firstInitialization = false;
        }
        // initialize configuration utils to use DB
        Config.setConfigUtils(new DBConfigUtils());

        _resourceManger = new VDSBrokerFrontendImpl();

        log.infoFormat("VDSBrokerFrontend: {0}", new Date());
        CpuFlagsManagerHandler.InitDictionaries();
        log.infoFormat("CpuFlagsManager: {0}", new Date());
        // ResourceManager res = ResourceManager.Instance;
        // Initialize the AuditLogCleanupManager
        AuditLogCleanupManager.getInstance();
        log.infoFormat("AuditLogCleanupManager: {0}", new Date());

        TagsDirector.getInstance().init();
        log.infoFormat("TagsDirector: {0}", new Date());
        IsoDomainListSyncronizer.getInstance();
        log.infoFormat("IsoDomainListSyncronizer: {0}", new Date());
        InitHandlers();
        log.infoFormat("InitHandlers: {0}", new Date());

        final String AppErrorsFileName = "bundles/AppErrors.properties";
        final String VdsErrorsFileName = "bundles/VdsmErrors.properties";
        errorsTranslator = new ErrorTranslatorImpl(AppErrorsFileName, VdsErrorsFileName);
        log.infoFormat("ErrorTranslator: {0}", new Date());
        _vdsErrorsTranslator = new ErrorTranslatorImpl(VdsErrorsFileName);
        log.infoFormat("VdsErrorTranslator: {0}", new Date());

        // initialize the JobRepository object and finalize non-terminated jobs
        log.infoFormat("Mark uncompleted jobs as {0}: {1}", JobExecutionStatus.UNKNOWN.name(), new Date());
        initJobRepository();

        // initializes the JobRepositoryCleanupManager
        log.infoFormat("JobRepositoryCleanupManager: {0}", new Date());
        JobRepositoryCleanupManager.getInstance().initialize();

        // initialize the AutoRecoveryManager
        log.infoFormat("AutoRecoveryManager: {0}", new Date());
        AutoRecoveryManager.getInstance().initialize();

        log.infoFormat("ExecutionMessageDirector: {0}", new Date());
        initExecutionMessageDirector();

        Integer sessionTimoutInterval = Config.<Integer> GetValue(ConfigValues.UserSessionTimeOutInterval);
        // negative value means session should never expire, therefore no need to clean sessions.
        if (sessionTimoutInterval > 0) {
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(SessionDataContainer.getInstance(),
                    "cleanExpiredUsersSessions", new Class[] {}, new Object[] {},
                    sessionTimoutInterval,
                    sessionTimoutInterval, TimeUnit.MINUTES);
        }
        // Set start-up time
        _startedAt = DateTime.getNow();

        int vmPoolMonitorIntervalInMinutes = Config.<Integer> GetValue(ConfigValues.VmPoolMonitorIntervalInMinutes);
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(new VmPoolMonitor(),
                "managePrestartedVmsInAllVmPools", new Class[] {}, new Object[] {},
                vmPoolMonitorIntervalInMinutes,
                vmPoolMonitorIntervalInMinutes, TimeUnit.MINUTES);

        int quotaCacheIntervalInMinutes = Config.<Integer> GetValue(ConfigValues.QuotaCacheIntervalInMinutes);
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(QuotaManager.getInstance(),
                "updateQuotaCache",  new Class[] {}, new Object[] {},
                1, quotaCacheIntervalInMinutes, TimeUnit.MINUTES);

        try {
            File fLock = new File(Config.<String> GetValue(ConfigValues.SignLockFile));
            if (fLock.exists()) {
                if (!fLock.delete()) {
                    log.error("Cleanup lockfile failed to delete the locking file.");
                }
            }
        } catch (SecurityException se) {
            log.error("Cleanup lockfile failed!", se);
        }
    }

    private void initJobRepository() {
        try {
            JobRepositoryFactory.getJobRepository().finalizeJobs();
        } catch (Exception e) {
            log.error("Failed to finalize running Jobs", e);
        }
    }

    private void initExecutionMessageDirector() {
        try {
            ExecutionMessageDirector.getInstance().initialize(ExecutionMessageDirector.EXECUTION_MESSAGES_FILE_PATH);
        } catch (RuntimeException e) {
            log.error("Failed to initialize ExecutionMessageDirector", e);
        }
    }

    /**
     * Handles compensation in case of uncompleted compensation-aware commands resulted from server failure.
     */
    private static void compensate() {
        // get all command snapshot entries
        List<KeyValue> commandSnapshots =
                DbFacade.getInstance().getBusinessEntitySnapshotDao().getAllCommands();
        for (KeyValue commandSnapshot : commandSnapshots) {
            // create an instance of the related command by its class name and command id
            CommandBase<?> cmd =
                    CommandsFactory.CreateCommand(commandSnapshot.getValue().toString(),
                            (Guid) commandSnapshot.getKey());
            if (cmd != null) {
                try {
                    cmd.compensate();
                } catch (RuntimeException e) {
                    log.errorFormat(
                            "Failed to run compensation on startup for Command {0}, Command Id : {1}, due to: {2}",
                            commandSnapshot.getValue(), commandSnapshot.getKey(), ExceptionUtils.getMessage(e), e);
                }
                log.infoFormat("Running compensation on startup for Command : {0} , Command Id : {1}",
                        commandSnapshot.getValue(), commandSnapshot.getKey());
            } else {
                log.errorFormat("Failed to run compensation on startup for Command {0} , Command Id : {1}",
                        commandSnapshot.getValue(), commandSnapshot.getKey());
            }
        }
    }

    @Override
    @ExcludeClassInterceptors
    public VdcReturnValueBase runInternalAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return runActionImpl(actionType, parameters, true, null);
    }

    @Override
    public VdcReturnValueBase RunAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        VdcReturnValueBase returnValue = notAllowToRunAction(actionType);
        if (returnValue != null) {
            return returnValue;
        }
        return runActionImpl(actionType, parameters, false, null);
    }

    private VdcReturnValueBase notAllowToRunAction(VdcActionType actionType) {
        // Since reload of configuration values is not fully supported, we have to get this value from DB
        // and can not use the cached configuration.
        String  mode = (DbFacade.getInstance().getVdcOptionDao().getByNameAndVersion(ConfigValues.EngineMode.name(),ConfigCommon.defaultConfigurationVersion)).getoption_value();
        if (EngineWorkingMode.MAINTENANCE.name().equalsIgnoreCase(mode)) {
            return getErrorCommandReturnValue(VdcBllMessages.ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE);
        }
        else if (EngineWorkingMode.PREPARE.name().equalsIgnoreCase(mode)) {
            return notAllowedInPrepForMaintMode(actionType);
        }
        return null;
    }

    /**
     * Executes an action according to the provided arguments.
     * @param actionType
     *            The type which define the action. Correlated to a concrete {@code CommandBase} instance.
     * @param parameters
     *            The parameters which are used to create the command.
     * @param runAsInternal
     *            Indicates if the command should be executed as an internal action or not.
     * @param context
     *            The required information for running the command.
     * @return The result of executing the action
     */
    private VdcReturnValueBase runActionImpl(VdcActionType actionType,
            VdcActionParametersBase parameters,
            boolean runAsInternal,
            CommandContext context) {

        VdcReturnValueBase returnValue = null;
        switch (actionType) {
        case AutoLogin:
            return getErrorCommandReturnValue(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);

        default:
            // Evaluate and set the correlationId on the parameters, fails on invalid correlation id
            boolean hasCorrelationId =
                    parameters == null ? false : StringUtils.isNotEmpty(parameters.getCorrelationId());
            returnValue = ExecutionHandler.evaluateCorrelationId(parameters);
            if (returnValue != null) {
                log.warnFormat("CanDoAction of action {0} failed. Reasons: {1}", actionType,
                        StringUtils.join(returnValue.getCanDoActionMessages(), ','));
                return returnValue;
            }

            CommandBase<?> command = CommandsFactory.CreateCommand(actionType, parameters);
            command.setInternalExecution(runAsInternal);
            command.setContext(context);
            ExecutionHandler.prepareCommandForMonitoring(command, actionType, runAsInternal, hasCorrelationId);

            returnValue = command.executeAction();
            returnValue.setCorrelationId(parameters.getCorrelationId());
            returnValue.setJobId(command.getJobId());
            return returnValue;
        }
    }

    @Override
    public VdcReturnValueBase EndAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return endAction(actionType, parameters, null);
    }

    @Override
    public VdcReturnValueBase endAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CommandContext context) {
        CommandBase<?> command = CommandsFactory.CreateCommand(actionType, parameters);
        command.setContext(context);
        return command.endAction();
    }

    @Override
    @ExcludeClassInterceptors
    public VdcQueryReturnValue runInternalQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runQueryImpl(actionType, parameters, false);
    }

    @Override
    public VdcQueryReturnValue RunQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runQueryImpl(actionType, parameters, true);
    }

    protected VdcQueryReturnValue runQueryImpl(VdcQueryType actionType, VdcQueryParametersBase parameters,
            boolean isPerformUserCheck) {
        if (isPerformUserCheck) {
            String sessionId = addSessionToContext(parameters);
            if (StringUtils.isEmpty(sessionId)
                    || SessionDataContainer.getInstance().getUser(sessionId, parameters.getRefresh()) == null) {
                return getErrorQueryReturnValue(VdcBllMessages.USER_IS_NOT_LOGGED_IN);
            }
        }
        Class<CommandBase<? extends VdcActionParametersBase>> clazz =
                CommandsFactory.getQueryClass(actionType.name());
        if (clazz.isAnnotationPresent(DisableInMaintenanceMode.class)) {
            String  mode = (DbFacade.getInstance().getVdcOptionDao().getByNameAndVersion
                    (ConfigValues.EngineMode.name(),ConfigCommon.defaultConfigurationVersion)).getoption_value();
            if (EngineWorkingMode.MAINTENANCE.name().equalsIgnoreCase(mode)) {
                return getErrorQueryReturnValue(VdcBllMessages.ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE);
            }
        }
        QueriesCommandBase<?> command = createQueryCommand(actionType, parameters);
        command.setInternalExecution(!isPerformUserCheck);
        command.Execute();
        return command.getQueryReturnValue();

    }

    private static String addSessionToContext(VdcQueryParametersBase parameters) {
        String sessionId = parameters.getHttpSessionId();
        boolean isAddToContext = true;
        if (StringUtils.isEmpty(sessionId)) {
            sessionId = parameters.getSessionId();
        }
        // This is a workaround for front end
        // Where no session, try to get Id of session which was attached to
        // request
        if (StringUtils.isEmpty(sessionId)) {
            sessionId = ThreadLocalParamsContainer.getHttpSessionId();
            isAddToContext = false;
        }
        if (!StringUtils.isEmpty(sessionId) && isAddToContext) {
            ThreadLocalParamsContainer.setHttpSessionId(sessionId);
        }
        return sessionId;
    }

    @Override
    public ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, boolean isRunOnlyIfAllCanDoPass) {
        VdcReturnValueBase returnValue = notAllowToRunAction(actionType);
        if (returnValue != null) {
            ArrayList<VdcReturnValueBase> list = new ArrayList<VdcReturnValueBase>();
            list.add(returnValue);
            return list;
        } else {
            return runMultipleActionsImpl(actionType, parameters, false, isRunOnlyIfAllCanDoPass);
        }
    }

    @Override
    @ExcludeClassInterceptors
    public ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters) {
        return runMultipleActionsImpl(actionType, parameters, true, false);
    }

    public ArrayList<VdcReturnValueBase> runMultipleActionsImpl(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            boolean isInternal,
            boolean isRunOnlyIfAllCanDoPass,
            ExecutionContext executionContext) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (!StringUtils.isEmpty(sessionId)) {
            for (VdcActionParametersBase parameter : parameters) {
                if (StringUtils.isEmpty(parameter.getSessionId())) {
                    parameter.setSessionId(sessionId);
                }
            }
        }
        MultipleActionsRunner runner = MultipleActionsRunnersFactory.CreateMultipleActionsRunner(actionType,
                parameters, isInternal);
        runner.setExecutionContext(executionContext);
        runner.setIsRunOnlyIfAllCanDoPass(isRunOnlyIfAllCanDoPass);
        return runner.Execute();
    }

    @Override
    @ExcludeClassInterceptors
    public ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            ExecutionContext executionContext) {
        return runMultipleActionsImpl(actionType, parameters, true, false, executionContext);
    }

    private ArrayList<VdcReturnValueBase> runMultipleActionsImpl(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            boolean isInternal,
            boolean isRunOnlyIfAllCanDoPass) {
        return runMultipleActionsImpl(actionType, parameters, isInternal, isRunOnlyIfAllCanDoPass, null);
    }

    @Override
    @ExcludeClassInterceptors
    public ErrorTranslator getErrorsTranslator() {
        return errorsTranslator;
    }

    @Override
    @ExcludeClassInterceptors
    public ErrorTranslator getVdsErrorsTranslator() {
        return _vdsErrorsTranslator;
    }

    /**
     * Login in to the system
     * @param parameters
     *            The parameters.
     * @return user if success, else null // //
     */
    @Override
    public VdcReturnValueBase Login(LoginUserParameters parameters) {
        switch (parameters.getActionType()) {
        case AutoLogin:
        case LoginUser:
        case LoginAdminUser:
            CommandBase<?> command = CommandsFactory.CreateCommand(parameters.getActionType(), parameters);
            return command.executeAction();
        default:
            return getErrorCommandReturnValue(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
        }
    }

    @Override
    public VdcReturnValueBase Logoff(LogoutUserParameters parameters) {
        return RunAction(VdcActionType.LogoutUser, parameters);
    }

    @Override
    public VdcQueryReturnValue RunPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        switch (actionType) {
        case GetDomainList:
        case GetLicenseProperties:
        case RegisterVds:
        case CheckDBConnection:
        case ValidateSession:
            return runQueryImpl(actionType, parameters, false);
        case GetConfigurationValue:
            GetConfigurationValueParameters configParameters = (GetConfigurationValueParameters) parameters;
            if (configParameters.getConfigValue() == ConfigurationValues.VdcVersion ||
                    configParameters.getConfigValue() == ConfigurationValues.ProductRPMVersion ||
                    configParameters.getConfigValue() == ConfigurationValues.ApplicationMode) {
                return runQueryImpl(actionType, parameters, false);
            }

            return getErrorQueryReturnValue(VdcBllMessages.USER_CANNOT_RUN_QUERY_NOT_PUBLIC);
        default:
            return getErrorQueryReturnValue(VdcBllMessages.USER_CANNOT_RUN_QUERY_NOT_PUBLIC);
        }
    }

    @Override
    public VdcReturnValueBase RunAutoAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return RunAction(actionType, parameters);
    }

    @Override
    public VdcQueryReturnValue RunAutoQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runInternalQuery(actionType, parameters);
    }

    @Override
    @ExcludeClassInterceptors
    public VdcReturnValueBase runInternalAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CommandContext context) {
        return runActionImpl(actionType, parameters, true, context);
    }

    private VdcReturnValueBase getErrorCommandReturnValue(VdcBllMessages message) {
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(false);
        returnValue.getCanDoActionMessages().add(message.toString());
        return returnValue;
    }

    private VdcReturnValueBase notAllowedInPrepForMaintMode(VdcActionType action) {
        Class<CommandBase<? extends VdcActionParametersBase>> clazz =
                CommandsFactory.getCommandClass(action.name());
        if (clazz.isAnnotationPresent(DisableInPrepareMode.class)) {
            return getErrorCommandReturnValue(VdcBllMessages.ENGINE_IS_RUNNING_IN_PREPARE_MODE);
        }
        return null;
    }

    private VdcQueryReturnValue getErrorQueryReturnValue(VdcBllMessages errorMessage) {
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(false);
        returnValue.setExceptionString(errorMessage.toString());
        return returnValue;
    }

    protected QueriesCommandBase<?> createQueryCommand(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return CommandsFactory.CreateQueryCommand(actionType, parameters);
    }

    private static final Log log = LogFactory.getLog(Backend.class);
}
