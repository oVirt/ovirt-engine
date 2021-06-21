package org.ovirt.engine.core.bll;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.aaa.Acct;
import org.ovirt.engine.core.aaa.AcctUtils;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.attestationbroker.AttestThread;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.executor.BackendActionExecutor;
import org.ovirt.engine.core.bll.executor.BackendQueryExecutor;
import org.ovirt.engine.core.bll.hostedengine.PreviousHostedEngineHost;
import org.ovirt.engine.core.bll.interceptors.CorrelationIdTrackerInterceptor;
import org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.job.JobRepositoryCleanupManager;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.storage.backup.DbEntityCleanupManager;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.utils.ThreadPoolMonitoringService;
import org.ovirt.engine.core.common.EngineWorkingMode;
import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.di.interceptor.InvocationLogger;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.EngineThreadPools;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.dbbroker.DbConnectionUtil;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dal.utils.CacheManager;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdcOptionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.dao.dwh.OsInfoDao;
import org.ovirt.engine.core.searchbackend.BaseConditionFieldAutoCompleter;
import org.ovirt.engine.core.searchbackend.OsValueAutoCompleter;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ErrorTranslatorImpl;
import org.ovirt.engine.core.utils.OsRepositoryImpl;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;
import org.ovirt.engine.core.utils.osinfo.OsInfoPreferencesLoader;
import org.ovirt.engine.core.utils.timezone.TimeZoneReader;
import org.ovirt.engine.core.vdsbroker.monitoring.VmMigrationProgressMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Here we use a Singleton Bean
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access
// to all the business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state
// of the singleton is synchronized across all clients.
@DependsOn("LockManager")
@Local({ BackendLocal.class, BackendInternal.class, BackendCommandObjectsHandler.class })
@Interceptors(CorrelationIdTrackerInterceptor.class)
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Backend implements BackendInternal, BackendCommandObjectsHandler {
    private static final Logger log = LoggerFactory.getLogger(Backend.class);

    private ErrorTranslator errorsTranslator;
    private ErrorTranslator vdsErrorsTranslator;
    private DateTime _startedAt;
    private static boolean firstInitialization = true;

    @Inject
    private ServiceLoader serviceLoader;
    @Inject
    private SessionDataContainer sessionDataContainer;
    @Inject
    private VDSBrokerFrontend resourceManger;
    @Inject
    private DbConnectionUtil dbConnectionUtil;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VmIconDao vmIconDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VdcOptionDao vdcOptionDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private OsInfoDao osInfoDao;
    @Inject
    private JobRepository jobRepository;
    @Inject
    private ExecutionHandler executionHandler;
    @Inject
    private TagsDirector tagsDirector;
    @Inject
    private MacPoolPerCluster macPoolPerCluster;

    @Inject
    private Instance<BackendActionExecutor> actionExecutor;

    @Inject
    private Instance<BackendQueryExecutor> queryExecutor;

    @Inject
    private MultipleActionsRunnersFactory multipleActionsRunnersFactory;

    @Inject
    private CommandCompensator compensator;

    @Inject
    private DBConfigUtils dbConfigUtils;

    private void initHandlers() {
        BaseConditionFieldAutoCompleter.tagsHandler = tagsDirector;
        serviceLoader.load(VmHandler.class);
        serviceLoader.load(VdsHandler.class);
        serviceLoader.load(VmTemplateHandler.class);
        log.info("Completed initializing handlers");
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

    private void checkDBConnectivity() {
        boolean dbUp = false;
        long expectedTimeout = System.currentTimeMillis() + dbConnectionUtil.getOnStartConnectionTimeout();
        long waitBetweenInterval = dbConnectionUtil.getConnectionCheckInterval();
        while (!dbUp && System.currentTimeMillis() < expectedTimeout) {
            try {
                dbUp = dbConnectionUtil.checkDBConnection();
            } catch (RuntimeException ex) {
                log.error("Error in getting DB connection, database is inaccessible: {}",
                        ex.getMessage());
                log.debug("Exception", ex);
                try {
                    Thread.sleep(waitBetweenInterval);
                } catch (InterruptedException e) {
                    log.warn("Failed to wait between connection polling attempts", e);
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
     */
    private void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());

        // Load the thread pools
        serviceLoader.load(EngineThreadPools.class);

        // Load the thread monitoring service
        serviceLoader.load(ThreadPoolMonitoringService.class);

        // save host that HE VM was running on prior to engine startup
        serviceLoader.load(PreviousHostedEngineHost.class);

        // initialize CDI services
        serviceLoader.load(CacheManager.class);
        // initialize configuration utils to use DB
        Config.setConfigUtils(dbConfigUtils);

        // we need to initialize os-info before the compensations take place because of VmPoolCommandBase#osRepository
        initOsRepository();

        initTimeZones();

        // When getting a proxy to this bean using JBoss embedded, the initialize method is called for each method
        // invocation on the proxy, as it is called by setup method which is @PostConstruct - the initialized flag
        // makes sure that initialization occurs only once per class (which is ok, as this is a @Service)
        if (firstInitialization) {
            macPoolPerCluster.logFreeMacs();
            // In case of a server termination that had uncompleted compensation-aware related commands
            // we have to get all those commands and call compensate on each
            compensator.compensate();
            firstInitialization = false;
        }

        log.info("Running ovirt-engine {}", Config.<String>getValue(ConfigValues.ProductRPMVersion));

        serviceLoader.load(CpuFlagsManagerHandler.class);
        serviceLoader.load(AuditLogCleanupManager.class);
        serviceLoader.load(ClusterUpgradeRunningCleanupManager.class);

        serviceLoader.load(TagsDirector.class);

        serviceLoader.load(IsoDomainListSynchronizer.class);

        initSearchDependencies();
        initHandlers();
        initVmPropertiesUtils();

        final String AppErrorsFileName = "bundles/AppErrors.properties";
        final String VdsErrorsFileName = "bundles/VdsmErrors.properties";
        errorsTranslator = new ErrorTranslatorImpl(AppErrorsFileName, VdsErrorsFileName);

        vdsErrorsTranslator = new ErrorTranslatorImpl(VdsErrorsFileName);

        // initialize the JobRepository object and finalize non-terminated jobs
        log.info("Mark incomplete jobs as {}", JobExecutionStatus.UNKNOWN.name());
        initJobRepository();

        serviceLoader.load(JobRepositoryCleanupManager.class);

        serviceLoader.load(DbEntityCleanupManager.class);

        serviceLoader.load(AutoRecoveryManager.class);

        initExecutionMessageDirector();

        // Set start-up time
        _startedAt = DateTime.getNow();

        serviceLoader.load(VmPoolHandler.class);
        serviceLoader.load(VmPoolMonitor.class);
        serviceLoader.load(HaAutoStartVmsRunner.class);
        serviceLoader.load(QuotaManager.class);
        serviceLoader.load(VmMigrationProgressMonitoring.class);

        //initializes attestation
        initAttestation();
        updatePredefinedIcons();
        iconCleanup();
        EngineExtensionsManager.getInstance().engineInitialize();
        AuthenticationProfileRepository.getInstance();
        AcctUtils.reportReason(Acct.ReportReason.STARTUP, "Starting up engine");
    }

    /**
     * It removes unused vm icons that remains in DB due to potential errors in commands.
     */
    private void iconCleanup() {
        vmIconDao.removeAllUnusedIcons();
    }

    private void updatePredefinedIcons() {
        serviceLoader.load(IconLoader.class);
    }

    private void initAttestation() {
        List<Cluster> clusters = clusterDao.getTrustedClusters();
        List<VDS> trustedVdsList = new ArrayList<>();
        List<String> trustedVdsNames = new ArrayList<>();

        if (clusters == null || clusters.size() == 0) {
            return;
        }
        for (Cluster cluster : clusters) {
            List<VDS> hostsInCluster = vdsDao.getAllForClusterWithStatus(cluster.getId(), VDSStatus.Up);
            if (hostsInCluster != null) {
                trustedVdsList.addAll(hostsInCluster);
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
        vdsDynamicDao.update(vds.getDynamicData());
    }

    private void initSearchDependencies() {
        SimpleDependencyInjector.getInstance().bind(new OsValueAutoCompleter(
                SimpleDependencyInjector.getInstance().get(OsRepository.class).getUniqueOsNames()));
    }

    private void initJobRepository() {
        try {
            jobRepository.finalizeJobs();
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

    @Override
    @ExcludeClassInterceptors
    public ActionReturnValue runInternalAction(ActionType actionType, ActionParametersBase parameters) {
        return runActionImpl(actionType, parameters, true, null);
    }

    @Override
    @InvocationLogger
    public ActionReturnValue runAction(ActionType actionType, ActionParametersBase parameters) {
        ActionReturnValue returnValue = notAllowToRunAction(actionType);
        if (returnValue != null) {
            return returnValue;
        }
        return runActionImpl(actionType, parameters, false, null);
    }

    private ActionReturnValue notAllowToRunAction(ActionType actionType) {
        // Since reload of configuration values is not fully supported, we have to get this value from DB
        // and can not use the cached configuration.
        String mode =
                vdcOptionDao.getByNameAndVersion(ConfigValues.EngineMode.name(),
                        ConfigCommon.defaultConfigurationVersion).getOptionValue();
        if (EngineWorkingMode.MAINTENANCE.name().equalsIgnoreCase(mode)) {
            return getErrorCommandReturnValue(EngineMessage.ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE);
        } else if (EngineWorkingMode.PREPARE.name().equalsIgnoreCase(mode)) {
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
    private ActionReturnValue runActionImpl(ActionType actionType,
            ActionParametersBase parameters,
            boolean runAsInternal,
            CommandContext context) {
        ActionReturnValue result;
        // If non-monitored command is invoked with JobId or ActionId as parameters, reject this command on can do action.
        if (!actionType.isActionMonitored() && !isActionExternal(actionType) && (parameters.getJobId() != null || parameters.getStepId() != null)) {
            result = new ActionReturnValue();
            result.getValidationMessages().add(EngineMessage.ACTION_TYPE_NON_MONITORED.toString());
            result.setValid(false);
            result.setSucceeded(false);
        } else {
            if (!runAsInternal) {
                logExecution(parameters.getSessionId(), String.format("command %s", actionType));
            }
            CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, context);
            result = runAction(command, runAsInternal);
        }
        return result;
    }

    private boolean isActionExternal(ActionType actionType){
        return actionType == ActionType.EndExternalJob || actionType == ActionType.EndExternalStep || actionType == ActionType.ClearExternalJob;
    }

    protected ActionReturnValue runAction(CommandBase<?> command,
            boolean runAsInternal) {
        ActionReturnValue returnValue = evaluateCorrelationId(command);
        if (returnValue != null) {
            return returnValue;
        }
        command.setInternalExecution(runAsInternal);
        executionHandler.prepareCommandForMonitoring(command, command.getActionType(), runAsInternal);

        returnValue = actionExecutor.get().execute(command);
        returnValue.setCorrelationId(command.getParameters().getCorrelationId());
        returnValue.setJobId(command.getJobId());
        return returnValue;
    }

    protected ActionReturnValue evaluateCorrelationId(CommandBase<?> commandBase) {
        ActionParametersBase cmdParams = commandBase.getParameters();
        if (cmdParams.getCorrelationId() == null && cmdParams.getParentParameters() != null) {
            cmdParams.setCorrelationId(cmdParams.getParentParameters().getCorrelationId());
        }
        // Evaluate and set the correlationId on the parameters, fails on invalid correlation id
        ActionReturnValue returnValue = ExecutionHandler.evaluateCorrelationId(cmdParams);
        if (returnValue != null) {
            log.warn("Validation of action '{}' failed. Reasons: {}", commandBase.getActionType(),
                    StringUtils.join(returnValue.getValidationMessages(), ','));

        }
        // Set the correlation-id on the command
        commandBase.setCorrelationId(cmdParams.getCorrelationId());
        return returnValue;
    }

    @Override
    public ActionReturnValue endAction(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext context) {
        return CommandsFactory.createCommand(actionType, parameters, context).endAction();
    }

    @Override
    @ExcludeClassInterceptors
    public QueryReturnValue runInternalQuery(QueryType actionType, QueryParametersBase parameters, EngineContext engineContext) {
        return runQueryImpl(actionType, parameters, false, engineContext);
    }

    @InvocationLogger
    @Override
    public QueryReturnValue runQuery(QueryType actionType, QueryParametersBase parameters) {
        return runQueryImpl(actionType,
                parameters,
                true,
                null);
    }

    protected QueryReturnValue runQueryImpl(QueryType actionType, QueryParametersBase parameters,
            boolean isPerformUserCheck, EngineContext engineContext) {
        if (isPerformUserCheck) {
            String sessionId = parameters.getSessionId();
            if (StringUtils.isEmpty(sessionId)
                    || sessionDataContainer.getUser(sessionId, parameters.getRefresh()) == null) {
                log.debug("Unable to execute query {} as no user session was found", actionType);
                return getErrorQueryReturnValue(EngineMessage.USER_IS_NOT_LOGGED_IN);
            }
            logExecution(sessionId,
                    String.format("query %s with isFiltered : %s", actionType, parameters.isFiltered()));
        }
        Class<?> clazz = CommandsFactory.getQueryClass(actionType.name());
        if (clazz.isAnnotationPresent(DisableInMaintenanceMode.class)) {
            String mode = vdcOptionDao.getByNameAndVersion
                    (ConfigValues.EngineMode.name(), ConfigCommon.defaultConfigurationVersion).getOptionValue();
            if (EngineWorkingMode.MAINTENANCE.name().equalsIgnoreCase(mode)) {
                return getErrorQueryReturnValue(EngineMessage.ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE);
            }
        }
        if (parameters.getCorrelationId() == null) {
            parameters.setCorrelationId(CorrelationIdTracker.getCorrelationId());
        }
        QueriesCommandBase<?> command = createQueryCommand(actionType, parameters, engineContext);
        command.setInternalExecution(!isPerformUserCheck);
        QueryReturnValue returnValue = queryExecutor.get().execute(command, actionType);
        if (returnValue.getCorrelationId() == null) {
            returnValue.setCorrelationId(parameters.getCorrelationId());
        }
        CorrelationIdTracker.setCorrelationId(parameters.getCorrelationId());
        return returnValue;
    }

    protected QueryReturnValue runQueryImpl(QueryType actionType, QueryParametersBase parameters,
            boolean isPerformUserCheck) {
        return runQueryImpl(actionType, parameters, isPerformUserCheck, null);
    }

    @Override
    public List<ActionReturnValue> runMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters, boolean isRunOnlyIfAllValidationPass) {
        return runMultipleActions(actionType, parameters, isRunOnlyIfAllValidationPass, false);
    }

    @Override
    public List<ActionReturnValue> runMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters, boolean isRunOnlyIfAllValidationPass, boolean waitForResult) {
        ActionReturnValue returnValue = notAllowToRunAction(actionType);
        if (returnValue != null) {
            List<ActionReturnValue> list = new ArrayList<>();
            list.add(returnValue);
            return list;
        } else {
            return runMultipleActionsImpl(actionType, parameters, false, isRunOnlyIfAllValidationPass, waitForResult, null);
        }
    }

    @Override
    @ExcludeClassInterceptors
    public List<ActionReturnValue> runInternalMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters) {
        return runMultipleActionsImpl(actionType, parameters, true, false, false, null);
    }


    @Override
    @ExcludeClassInterceptors
    public List<ActionReturnValue> runInternalMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters, CommandContext commandContext) {
        return runMultipleActionsImpl(actionType, parameters, true, false, false, commandContext);

    }

    private List<ActionReturnValue> runMultipleActionsImpl(ActionType actionType,
            List<ActionParametersBase> parameters,
            boolean isInternal,
            boolean isRunOnlyIfAllValidationPass,
            boolean isWaitForResult,
            CommandContext commandContext) {
        MultipleActionsRunner runner = multipleActionsRunnersFactory.createMultipleActionsRunner(actionType,
                parameters, isInternal, commandContext);
        runner.setIsRunOnlyIfAllValidatePass(isRunOnlyIfAllValidationPass);
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
        return vdsErrorsTranslator;
    }

    @Override
    public ActionReturnValue logoff(ActionParametersBase parameters) {
        return runAction(ActionType.LogoutSession, parameters);
    }

    @Override
    public QueryReturnValue runPublicQuery(QueryType actionType, QueryParametersBase parameters) {
        parameters.setRefresh(false);
        switch (actionType) {
        case GetAAAProfileList:
        case RegisterVds:
        case CheckDBConnection:
        case GetDbUserBySession:
        case GetEngineSessionIdForSsoToken:
        case ValidateSession:
        case GetDefaultAllowedOrigins:
            return runQueryImpl(actionType, parameters, false);
        case GetConfigurationValue:
            GetConfigurationValueParameters configParameters = (GetConfigurationValueParameters) parameters;
            switch (configParameters.getConfigValue()) {
            case VdcVersion:
            case ProductRPMVersion:
            case ApplicationMode:
            case UserSessionTimeOutInterval:
            case CORSSupport:
            case CORSAllowedOrigins:
            case CORSAllowDefaultOrigins:
            case CORSDefaultOriginSuffixes:
                return runQueryImpl(actionType, parameters, false);
            default:
                break;
            }

        default:
            break;
        }

        return getErrorQueryReturnValue(EngineMessage.USER_CANNOT_RUN_QUERY_NOT_PUBLIC);
    }

    @Override
    @ExcludeClassInterceptors
    public ActionReturnValue runInternalAction(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext context) {
        return runActionImpl(actionType, parameters, true, context);
    }

    private ActionReturnValue getErrorCommandReturnValue(EngineMessage message) {
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setValid(false);
        returnValue.getValidationMessages().add(message.toString());
        return returnValue;
    }

    private ActionReturnValue notAllowedInPrepForMaintMode(ActionType action) {
        Class<?> clazz = CommandsFactory.getCommandClass(action.name());
        if (clazz.isAnnotationPresent(DisableInPrepareMode.class)) {
            return getErrorCommandReturnValue(EngineMessage.ENGINE_IS_RUNNING_IN_PREPARE_MODE);
        }
        return null;
    }

    private QueryReturnValue getErrorQueryReturnValue(EngineMessage errorMessage) {
        QueryReturnValue returnValue = new QueryReturnValue();
        returnValue.setSucceeded(false);
        returnValue.setExceptionString(errorMessage.toString());
        return returnValue;
    }

    protected QueriesCommandBase<?> createQueryCommand(QueryType actionType, QueryParametersBase parameters, EngineContext engineContext) {
        return CommandsFactory.createQueryCommand(actionType, parameters, engineContext);
    }

    private void initVmPropertiesUtils() {
        VmPropertiesUtils vmPropertiesUtils = VmPropertiesUtils.getInstance();
        SimpleDependencyInjector.getInstance().bind(VmPropertiesUtils.class, vmPropertiesUtils);
    }

    private void initOsRepository() {
        OsInfoPreferencesLoader.INSTANCE.init(FileSystems.getDefault().getPath(EngineLocalConfig.getInstance().getEtcDir().getAbsolutePath(), Config.<String>getValue(ConfigValues.OsRepositoryConfDir)));
        OsRepositoryImpl.INSTANCE.init(OsInfoPreferencesLoader.INSTANCE.getPreferences());
        OsRepository osRepository = OsRepositoryImpl.INSTANCE;
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        osInfoDao.populateDwhOsInfo(osRepository.getOsNames());
    }

    private void initTimeZones() {
        TimeZoneReader.INSTANCE.init(FileSystems.getDefault()
                .getPath(EngineLocalConfig.getInstance().getEtcDir().getAbsolutePath(),
                        TimeZoneReader.INSTANCE.DIR_NAME));
        TimeZoneType.WINDOWS_TIMEZONE.init(TimeZoneReader.INSTANCE.getWindowsTimezones());
        TimeZoneType.GENERAL_TIMEZONE.init(TimeZoneReader.INSTANCE.getGeneralTimezones());
        WindowsJavaTimezoneMapping.init(TimeZoneReader.INSTANCE.getWindowsToJavaTimezones());
    }

    private void logExecution(String sessionId, String details) {
        DbUser user = sessionDataContainer.getUser(sessionId, false);
        log.debug("Executing {}{}",
                details,
                user == null ? "." : String.format(" for user %s@%s.", user.getLoginName(), user.getDomain()));
    }

   @Override
    public CommandBase<?> createAction(ActionType actionType, ActionParametersBase parameters, CommandContext context) {
        return CommandsFactory.createCommand(actionType, parameters, context);
    }

    @Override
    public ActionReturnValue runAction(CommandBase<?> action, ExecutionContext executionContext) {
        ExecutionHandler.setExecutionContextForTasks(action.getContext(),
                executionContext, action.getContext().getLock());
        return runAction(action, true);
    }

    @Override
    public QueryReturnValue runInternalQuery(QueryType queryType, QueryParametersBase queryParameters) {
        return runInternalQuery(queryType, queryParameters, null);
    }
}
