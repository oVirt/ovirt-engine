package org.ovirt.engine.core.bll;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.api.extensions.aaa.Acct;
import org.ovirt.engine.core.aaa.AcctUtils;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.attestationbroker.AttestThread;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.interceptors.ThreadLocalSessionCleanerInterceptor;
import org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryCleanupManager;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.EngineWorkingMode;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.bll.utils.Injector;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.searchbackend.BaseConditionFieldAutoCompleter;
import org.ovirt.engine.core.searchbackend.OsValueAutoCompleter;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ErrorTranslatorImpl;
import org.ovirt.engine.core.utils.OsRepositoryImpl;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.osinfo.OsInfoPreferencesLoader;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

// Here we use a Singleton Bean
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access
// to all the business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state
// of the singleton is synchronized across all clients.
@DependsOn("Scheduler")
@Local({ BackendLocal.class, BackendInternal.class, BackendCommandObjectsHandler.class })
@Interceptors({ ThreadLocalSessionCleanerInterceptor.class })
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Backend implements BackendInternal, BackendCommandObjectsHandler {

    private ITagsHandler mTagsHandler;
    private ErrorTranslator errorsTranslator;
    private ErrorTranslator _vdsErrorsTranslator;
    private DateTime _startedAt;
    private static boolean firstInitialization = true;
    private String poolMonitoringJobId;
    @Inject Injector injector;

    public static BackendInternal getInstance() {
        return EjbUtils.findBean(BeanType.BACKEND, BeanProxyType.LOCAL);
    }

    private void initHandlers() {
        mTagsHandler = HandlersFactory.createTagsHandler();
        BaseConditionFieldAutoCompleter.tagsHandler = mTagsHandler;
        VmHandler.init();
        VdsHandler.init();
        VmTemplateHandler.init();
        log.info("Completed initializing handlers");
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
        try {
            initialize();
        } catch(Exception ex) {
            log.error("Error during initialization", ex);
            throw ex;
        }
    }

    @PreDestroy
    public void shutdown() {
        AcctUtils.reportReason(Acct.ReportReason.SHUTDOWN, "Shutting down engine");
    }

    private static void checkDBConnectivity() {
        boolean dbUp = false;
        long expectedTimeout =
                System.currentTimeMillis()
                        + DbFacade.getInstance().getOnStartConnectionTimeout();
        long waitBetweenInterval = DbFacade.getInstance().getConnectionCheckInterval();
        while (!dbUp && System.currentTimeMillis() < expectedTimeout) {
            try {
                dbUp = DbFacade.getInstance().checkDBConnection();
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
    public void initialize() {
        log.info("Start initializing " + getClass().getSimpleName());
        // initialize configuration utils to use DB
        Config.setConfigUtils(new DBConfigUtils());
        // we need to initialize os-info before the compensations take place because of VmPoolCommandBase#osRepository
        initOsRepository();

        // When getting a proxy to this bean using JBoss embedded, the initialize method is called for each method
        // invocation on the proxy, as it is called by setup method which is @PostConstruct - the initialized flag
        // makes sure that initialization occurs only once per class (which is ok, as this is a @Service)
        if (firstInitialization) {
            // In case of a server termination that had uncompleted compensation-aware related commands
            // we have to get all those commands and call compensate on each
            compensate();
            firstInitialization = false;
        }

        log.info("Running ovirt-engine " + Config.<String>getValue(ConfigValues.ProductRPMVersion));
        _resourceManger = new VDSBrokerFrontendImpl();

        CpuFlagsManagerHandler.InitDictionaries();

        // ResourceManager res = ResourceManager.Instance;
        // Initialize the AuditLogCleanupManager
        AuditLogCleanupManager.getInstance();

        // Initialize the CommandEntityCleanupManager
        CommandEntityCleanupManager.getInstance();

        TagsDirector.getInstance().init();

        IsoDomainListSyncronizer.getInstance();

        initSearchDependencies();
        initHandlers();
        initVmPropertiesUtils();

        final String AppErrorsFileName = "bundles/AppErrors.properties";
        final String VdsErrorsFileName = "bundles/VdsmErrors.properties";
        errorsTranslator = new ErrorTranslatorImpl(AppErrorsFileName, VdsErrorsFileName);

        _vdsErrorsTranslator = new ErrorTranslatorImpl(VdsErrorsFileName);

        // initialize the JobRepository object and finalize non-terminated jobs
        log.infoFormat("Mark incomplete jobs as {0}", JobExecutionStatus.UNKNOWN.name());
        initJobRepository();

        // initializes the JobRepositoryCleanupManager
        JobRepositoryCleanupManager.getInstance().initialize();

        // initialize the AutoRecoveryManager
        AutoRecoveryManager.getInstance().initialize();

        initExecutionMessageDirector();

        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(SessionDataContainer.getInstance(),
                    "cleanExpiredUsersSessions", new Class[] {}, new Object[] {},
                    1,
                    1, TimeUnit.MINUTES);

        // Set start-up time
        _startedAt = DateTime.getNow();

        int vmPoolMonitorIntervalInMinutes = Config.<Integer> getValue(ConfigValues.VmPoolMonitorIntervalInMinutes);
        poolMonitoringJobId =
                SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(new VmPoolMonitor(),
                        "managePrestartedVmsInAllVmPools", new Class[] {}, new Object[] {},
                        vmPoolMonitorIntervalInMinutes,
                        vmPoolMonitorIntervalInMinutes, TimeUnit.MINUTES);

        int autoStartVmsRunnerIntervalInSeconds = Config.<Integer> getValue(ConfigValues.AutoStartVmsRunnerIntervalInSeconds);
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(AutoStartVmsRunner.getInstance(),
                "startFailedAutoStartVms", new Class[] {}, new Object[] {},
                autoStartVmsRunnerIntervalInSeconds,
                autoStartVmsRunnerIntervalInSeconds, TimeUnit.SECONDS);

        int quotaCacheIntervalInMinutes = Config.<Integer> getValue(ConfigValues.QuotaCacheIntervalInMinutes);
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(QuotaManager.getInstance(),
                "updateQuotaCache",  new Class[] {}, new Object[] {},
                1, quotaCacheIntervalInMinutes, TimeUnit.MINUTES);
        //initializes attestation
        initAttestation();
        EngineExtensionsManager.getInstance().engineInitialize();
        AuthenticationProfileRepository.getInstance();
        AcctUtils.reportReason(Acct.ReportReason.STARTUP, "Starting up engine");
    }

    private void initAttestation() {
        List<VDSGroup> vdsGroups = DbFacade.getInstance().getVdsGroupDao().getTrustedClusters();
        List<VDS> trustedVdsList = new ArrayList<>();
        List<String> trustedVdsNames = new ArrayList<>();

        if (vdsGroups == null || vdsGroups.size() == 0) {
            return;
        }
        for (VDSGroup vdsGroup : vdsGroups) {
            List<VDS> vdssInGroup = DbFacade.getInstance().getVdsDao().
                    getAllForVdsGroupWithStatus(vdsGroup.getId(), VDSStatus.Up);
            if (vdssInGroup != null) {
                trustedVdsList.addAll(vdssInGroup);
            }
        }

        for (VDS vds : trustedVdsList) {
            trustedVdsNames.add(vds.getHostName());
            setNonOperational(NonOperationalReason.UNINITIALIZED, vds);
        }

        try {
            AttestThread attestThread = new AttestThread(trustedVdsNames);
            attestThread.start();//start a thread to attest the hosts
        } catch (Exception e) {
            log.error("Failed to initialize attestation cache", e);
        }
    }

    private void setNonOperational(NonOperationalReason reason, VDS vds) {
        vds.setNonOperationalReason(reason);
        vds.setStatus(VDSStatus.NonOperational);
        DbFacade.getInstance().getVdsDynamicDao().update(vds.getDynamicData());
    }

    private void initSearchDependencies() {
        SimpleDependecyInjector.getInstance().bind(new OsValueAutoCompleter(
                SimpleDependecyInjector.getInstance().get(OsRepository.class).getUniqueOsNames()));
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
                    CommandsFactory.createCommand(commandSnapshot.getValue().toString(),
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
    public VdcReturnValueBase runAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        VdcReturnValueBase returnValue = notAllowToRunAction(actionType);
        if (returnValue != null) {
            return returnValue;
        }
        return runActionImpl(actionType, parameters, false, null);
    }

    private VdcReturnValueBase notAllowToRunAction(VdcActionType actionType) {
        // Since reload of configuration values is not fully supported, we have to get this value from DB
        // and can not use the cached configuration.
        String  mode = (DbFacade.getInstance().getVdcOptionDao().getByNameAndVersion(ConfigValues.EngineMode.name(), ConfigCommon.defaultConfigurationVersion)).getoption_value();
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
        VdcReturnValueBase result;
        // If non-monitored command is invoked with JobId or ActionId as parameters, reject this command on can do action.
        if (!actionType.isActionMonitored() && !isActionExternal(actionType) && (parameters.getJobId() != null || parameters.getStepId() != null)) {
            result = new VdcReturnValueBase();
            result.getCanDoActionMessages().add(VdcBllMessages.ACTION_TYPE_NON_MONITORED.toString());
            result.setCanDoAction(false);
            result.setSucceeded(false);
        }
        else {
            CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, context);
            result = runAction(command, runAsInternal);
        }
        return result;
    }

    private boolean isActionExternal(VdcActionType actionType){
        return (actionType == VdcActionType.EndExternalJob || actionType == VdcActionType.EndExternalStep || actionType == VdcActionType.ClearExternalJob);
    }

    protected VdcReturnValueBase runAction(CommandBase<?> command,
            boolean runAsInternal) {
        VdcReturnValueBase returnValue = evaluateCorrelationId(command);
        if (returnValue != null) {
            return returnValue;
        }
        command.setInternalExecution(runAsInternal);
        ExecutionHandler.prepareCommandForMonitoring(command, command.getActionType(), runAsInternal);

        command.insertAsyncTaskPlaceHolders();
        returnValue = command.executeAction();
        returnValue.setCorrelationId(command.getParameters().getCorrelationId());
        returnValue.setJobId(command.getJobId());
        return returnValue;
    }

    protected VdcReturnValueBase evaluateCorrelationId(CommandBase<?> commandBase) {
        VdcReturnValueBase returnValue = null;

        // Evaluate and set the correlationId on the parameters, fails on invalid correlation id
        returnValue = ExecutionHandler.evaluateCorrelationId(commandBase.getParameters());
        if (returnValue != null) {
            log.warnFormat("CanDoAction of action {0} failed. Reasons: {1}", commandBase.getActionType(),
                    StringUtils.join(returnValue.getCanDoActionMessages(), ','));

        }
        // Set the correlation-id on the command
        commandBase.setCorrelationId(commandBase.getParameters().getCorrelationId());
        return returnValue;
    }

    @Override
    public VdcReturnValueBase endAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CommandContext context) {
        return CommandsFactory.createCommand(actionType, parameters, context).endAction();
    }

    @Override
    @ExcludeClassInterceptors
    public VdcQueryReturnValue runInternalQuery(VdcQueryType actionType, VdcQueryParametersBase parameters, EngineContext engineContext) {
        return runQueryImpl(actionType, parameters, false, engineContext);
    }

    @Override
    public VdcQueryReturnValue runQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runQueryImpl(actionType,
                parameters,
                true,
                null);
    }

    protected VdcQueryReturnValue runQueryImpl(VdcQueryType actionType, VdcQueryParametersBase parameters,
            boolean isPerformUserCheck, EngineContext engineContext) {
        if (isPerformUserCheck) {
            String sessionId = parameters.getSessionId();
            if (StringUtils.isEmpty(sessionId)
                    || SessionDataContainer.getInstance().getUser(sessionId, parameters.getRefresh()) == null) {
                return getErrorQueryReturnValue(VdcBllMessages.USER_IS_NOT_LOGGED_IN);
            }
        }
        Class<CommandBase<? extends VdcActionParametersBase>> clazz =
                CommandsFactory.getQueryClass(actionType.name());
        if (clazz.isAnnotationPresent(DisableInMaintenanceMode.class)) {
            String  mode = (DbFacade.getInstance().getVdcOptionDao().getByNameAndVersion
                    (ConfigValues.EngineMode.name(), ConfigCommon.defaultConfigurationVersion)).getoption_value();
            if (EngineWorkingMode.MAINTENANCE.name().equalsIgnoreCase(mode)) {
                return getErrorQueryReturnValue(VdcBllMessages.ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE);
            }
        }
        QueriesCommandBase<?> command = createQueryCommand(actionType, parameters, engineContext);
        command.setInternalExecution(!isPerformUserCheck);
        command.execute();
        return command.getQueryReturnValue();

    }

    protected VdcQueryReturnValue runQueryImpl(VdcQueryType actionType, VdcQueryParametersBase parameters,
            boolean isPerformUserCheck) {
        return runQueryImpl(actionType, parameters, isPerformUserCheck, null);
    }

    @Override
    public ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, boolean isRunOnlyIfAllCanDoPass) {
        return runMultipleActions(actionType, parameters, isRunOnlyIfAllCanDoPass, false);
    }

    @Override
    public ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, boolean isRunOnlyIfAllCanDoPass, boolean waitForResult) {
        VdcReturnValueBase returnValue = notAllowToRunAction(actionType);
        if (returnValue != null) {
            ArrayList<VdcReturnValueBase> list = new ArrayList<VdcReturnValueBase>();
            list.add(returnValue);
            return list;
        } else {
            return runMultipleActionsImpl(actionType, parameters, false, isRunOnlyIfAllCanDoPass, waitForResult, null);
        }
    }

    @Override
    @ExcludeClassInterceptors
    public ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters) {
        return runMultipleActionsImpl(actionType, parameters, true, false, false, null);
    }


    @Override
    @ExcludeClassInterceptors
    public ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, CommandContext commandContext) {
        return runMultipleActionsImpl(actionType, parameters, true, false, false, commandContext);

    }

    private ArrayList<VdcReturnValueBase> runMultipleActionsImpl(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            boolean isInternal,
            boolean isRunOnlyIfAllCanDoPass,
            boolean isWaitForResult,
            CommandContext commandContext) {
        MultipleActionsRunner runner = MultipleActionsRunnersFactory.createMultipleActionsRunner(actionType,
                parameters, isInternal, commandContext);
        runner.setIsRunOnlyIfAllCanDoPass(isRunOnlyIfAllCanDoPass);
        runner.setIsWaitForResult(isWaitForResult);
        return runner.execute();
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
    public VdcReturnValueBase login(LoginUserParameters parameters) {
        switch (parameters.getActionType()) {
        case LoginUser:
        case LoginAdminUser:
            CommandBase<?> command = CommandsFactory.createCommand(parameters.getActionType(), parameters);
            command.insertAsyncTaskPlaceHolders();
            return command.executeAction();
        default:
            return getErrorCommandReturnValue(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
        }
    }

    @Override
    public VdcReturnValueBase logoff(LogoutUserParameters parameters) {
        return runAction(VdcActionType.LogoutUser, parameters);
    }

    @Override
    public VdcQueryReturnValue runPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        parameters.setRefresh(false);
        switch (actionType) {
        case GetAAAProfileList:
        case RegisterVds:
        case CheckDBConnection:
        case GetValueBySession:
        case ValidateSession:
            return runQueryImpl(actionType, parameters, false);
        case GetConfigurationValue:
            GetConfigurationValueParameters configParameters = (GetConfigurationValueParameters) parameters;
            if (configParameters.getConfigValue() == ConfigurationValues.VdcVersion ||
                    configParameters.getConfigValue() == ConfigurationValues.ProductRPMVersion ||
                    configParameters.getConfigValue() == ConfigurationValues.ApplicationMode ||
                    configParameters.getConfigValue() == ConfigurationValues.UserMessageOfTheDay ||
                    configParameters.getConfigValue() == ConfigurationValues.UserSessionTimeOutInterval) {
                return runQueryImpl(actionType, parameters, false);
            }

            return getErrorQueryReturnValue(VdcBllMessages.USER_CANNOT_RUN_QUERY_NOT_PUBLIC);
        default:
            return getErrorQueryReturnValue(VdcBllMessages.USER_CANNOT_RUN_QUERY_NOT_PUBLIC);
        }
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

    protected QueriesCommandBase<?> createQueryCommand(VdcQueryType actionType, VdcQueryParametersBase parameters, EngineContext engineContext) {
        return CommandsFactory.createQueryCommand(actionType, parameters, engineContext);
    }

    @Override
    @ExcludeClassInterceptors
    public void triggerPoolMonitoringJob() {
        SchedulerUtilQuartzImpl.getInstance().triggerJob(poolMonitoringJobId);
    }

    private void initVmPropertiesUtils() {
        VmPropertiesUtils vmPropertiesUtils = VmPropertiesUtils.getInstance();
        SimpleDependecyInjector.getInstance().bind(VmPropertiesUtils.class, vmPropertiesUtils);
    }

    private void initOsRepository() {
        OsInfoPreferencesLoader.INSTANCE.init(FileSystems.getDefault().getPath(EngineLocalConfig.getInstance().getEtcDir().getAbsolutePath(), Config.<String>getValue(ConfigValues.OsRepositoryConfDir)));
        OsRepositoryImpl.INSTANCE.init(OsInfoPreferencesLoader.INSTANCE.getPreferences());
        OsRepository osRepository = OsRepositoryImpl.INSTANCE;
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
        DbFacade.getInstance().populateDwhOsInfo(osRepository.getOsNames());
    }

   @Override
    public CommandBase<?> createAction(VdcActionType actionType, VdcActionParametersBase parameters, CommandContext context) {
        return CommandsFactory.createCommand(actionType, parameters, context);
    }

    @Override
    public VdcReturnValueBase runAction(CommandBase<?> action, ExecutionContext executionContext) {
        ExecutionHandler.setExecutionContextForTasks(action.getContext(),
                executionContext, null);
        return runAction(action, true);
    }

    private static final Log log = LogFactory.getLog(Backend.class);

    @Override
    public VdcQueryReturnValue runInternalQuery(VdcQueryType queryType, VdcQueryParametersBase queryParameters) {
        return runInternalQuery(queryType, queryParameters, null);
    }
}
