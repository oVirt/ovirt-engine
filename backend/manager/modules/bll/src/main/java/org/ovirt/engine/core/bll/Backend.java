package org.ovirt.engine.core.bll;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.aaa.Acct;
import org.ovirt.engine.core.aaa.AcctUtils;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.attestationbroker.AttestThread;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.hostedengine.PreviousHostedEngineHost;
import org.ovirt.engine.core.bll.interceptors.CorrelationIdTrackerInterceptor;
import org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryCleanupManager;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSyncronizer;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.EngineWorkingMode;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dal.utils.CacheManager;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.searchbackend.BaseConditionFieldAutoCompleter;
import org.ovirt.engine.core.searchbackend.OsValueAutoCompleter;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ErrorTranslatorImpl;
import org.ovirt.engine.core.utils.OsRepositoryImpl;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;
import org.ovirt.engine.core.utils.osinfo.OsInfoPreferencesLoader;
import org.ovirt.engine.core.utils.ovf.OvfVmIconDefaultsProvider;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.vdsbroker.monitoring.VmDevicesMonitoring;
import org.ovirt.engine.core.vdsbroker.monitoring.VmsMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Here we use a Singleton Bean
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access
// to all the business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state
// of the singleton is synchronized across all clients.
@DependsOn({"LockManager"})
@Local({ BackendLocal.class, BackendInternal.class, BackendCommandObjectsHandler.class })
@Interceptors({ CorrelationIdTrackerInterceptor.class })
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Backend implements BackendInternal, BackendCommandObjectsHandler {
    private static final Logger log = LoggerFactory.getLogger(Backend.class);

    private ITagsHandler tagsHandler;
    private ErrorTranslator errorsTranslator;
    private ErrorTranslator vdsErrorsTranslator;
    private DateTime _startedAt;
    private static boolean firstInitialization = true;
    @Inject
    Injector injector;
    @Inject
    private DbFacade dbFacade;
    @Inject @Any
    private Instance<SchedulerUtil> taskSchedulers;
    @Inject @Any
    private Instance<BackendService> services;
    @Inject
    private SessionDataContainer sessionDataContainer;
    @Inject
    private VDSBrokerFrontend resourceManger;

    public static BackendInternal getInstance() {
        return Injector.get(BackendInternal.class);
    }

    private void initHandlers() {
        tagsHandler = HandlersFactory.createTagsHandler();
        BaseConditionFieldAutoCompleter.tagsHandler = tagsHandler;
        VmHandler.init();
        VdsHandler.init();
        VmTemplateHandler.init();
        log.info("Completed initializing handlers");
    }

    /**
     * TODO remove this after moving all places to use CDI.
     * kept for backward compatibility.
     */
    @Deprecated
    @Override
    @ExcludeClassInterceptors
    public VDSBrokerFrontend getResourceManager() {
        return resourceManger;
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
        long expectedTimeout =
                System.currentTimeMillis()
                        + dbFacade.getOnStartConnectionTimeout();
        long waitBetweenInterval = dbFacade.getConnectionCheckInterval();
        while (!dbUp && System.currentTimeMillis() < expectedTimeout) {
            try {
                dbUp = dbFacade.checkDBConnection();
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
    @Override
    public void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());

        // save host that HE VM was running on prior to engine startup
        loadService(PreviousHostedEngineHost.class);

        // start task schedulers
        for (SchedulerUtil taskScheduler : taskSchedulers) {
            log.info("Started task scheduler {}", taskScheduler);
        }
        // initialize CDI services
        loadService(CacheManager.class);
        // initialize configuration utils to use DB
        Config.setConfigUtils(new DBConfigUtils());
        // we need to initialize os-info before the compensations take place because of VmPoolCommandBase#osRepository
        initOsRepository();

        //TODO temporal solution DbFacade in Utils
        SimpleDependencyInjector.getInstance().bind(dbFacade);

        // When getting a proxy to this bean using JBoss embedded, the initialize method is called for each method
        // invocation on the proxy, as it is called by setup method which is @PostConstruct - the initialized flag
        // makes sure that initialization occurs only once per class (which is ok, as this is a @Service)
        if (firstInitialization) {
            // In case of a server termination that had uncompleted compensation-aware related commands
            // we have to get all those commands and call compensate on each
            compensate();
            firstInitialization = false;
        }

        log.info("Running ovirt-engine {}", Config.<String>getValue(ConfigValues.ProductRPMVersion));

        loadService(CpuFlagsManagerHandler.class);

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

        vdsErrorsTranslator = new ErrorTranslatorImpl(VdsErrorsFileName);

        // initialize the JobRepository object and finalize non-terminated jobs
        log.info("Mark incomplete jobs as {}", JobExecutionStatus.UNKNOWN.name());
        initJobRepository();

        // initializes the JobRepositoryCleanupManager
        JobRepositoryCleanupManager.getInstance().initialize();

        // initialize the AutoRecoveryManager
        AutoRecoveryManager.getInstance().initialize();

        initExecutionMessageDirector();

        taskSchedulers.select(SchedulerUtilQuartzImpl.class).get()
                .scheduleAFixedDelayJob(sessionDataContainer,
                "cleanExpiredUsersSessions", new Class[] {}, new Object[] {},
                1,
                1, TimeUnit.MINUTES);

        // Set start-up time
        _startedAt = DateTime.getNow();

        loadService(VmsMonitoring.class);
        loadService(VmDevicesMonitoring.class);
        loadService(VmPoolMonitor.class);
        loadService(HaAutoStartVmsRunner.class);
        loadService(QuotaManager.class);

        //initializes attestation
        initAttestation();
        updatePredefinedIcons();
        iconCleanup();
        registerIconDefaultsProvider();
        EngineExtensionsManager.getInstance().engineInitialize();
        AuthenticationProfileRepository.getInstance();
        AcctUtils.reportReason(Acct.ReportReason.STARTUP, "Starting up engine");
    }

    private void loadService(Class<? extends BackendService> service) {
        log.info("Start {} ", services.select(service).get());
    }

    /**
     * It removes unused vm icons that remains in DB due to potential errors in commands.
     */
    private void iconCleanup() {
        dbFacade.getVmIconDao().removeAllUnusedIcons();
    }

    private void updatePredefinedIcons() {
        IconLoader.load();
    }

    private void registerIconDefaultsProvider() {
        final OvfVmIconDefaultsProvider ovfVmIconDefaultsProvider = new OvfVmIconDefaultsProvider() {

            @Override public Map<Integer, VmIconIdSizePair> getVmIconDefaults() {
                final VdcQueryReturnValue queryReturnValue =
                        runInternalQuery(VdcQueryType.GetVmIconDefaults, new VdcQueryParametersBase());
                return queryReturnValue.getReturnValue();
            }
        };
        SimpleDependencyInjector.getInstance().bind(OvfVmIconDefaultsProvider.class, ovfVmIconDefaultsProvider);
    }

    private void initAttestation() {
        List<Cluster> clusters = dbFacade.getClusterDao().getTrustedClusters();
        List<VDS> trustedVdsList = new ArrayList<>();
        List<String> trustedVdsNames = new ArrayList<>();

        if (clusters == null || clusters.size() == 0) {
            return;
        }
        for (Cluster cluster : clusters) {
            List<VDS> hostsInCluster = dbFacade.getVdsDao().
                    getAllForClusterWithStatus(cluster.getId(), VDSStatus.Up);
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
        dbFacade.getVdsDynamicDao().update(vds.getDynamicData());
    }

    private void initSearchDependencies() {
        SimpleDependencyInjector.getInstance().bind(new OsValueAutoCompleter(
                SimpleDependencyInjector.getInstance().get(OsRepository.class).getUniqueOsNames()));
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
                    log.error(
                            "Failed to run compensation on startup for Command '{}', Command Id '{}': {}",
                            commandSnapshot.getValue(),
                            commandSnapshot.getKey(),
                            e.getMessage());
                    log.error("Exception", e);
                }
                log.info("Running compensation on startup for Command '{}', Command Id '{}'",
                        commandSnapshot.getValue(), commandSnapshot.getKey());
            } else {
                log.error("Failed to run compensation on startup for Command '{}', Command Id '{}'",
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
        String mode =
                dbFacade.getVdcOptionDao().getByNameAndVersion(ConfigValues.EngineMode.name(),
                        ConfigCommon.defaultConfigurationVersion).getOptionValue();
        if (EngineWorkingMode.MAINTENANCE.name().equalsIgnoreCase(mode)) {
            return getErrorCommandReturnValue(EngineMessage.ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE);
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
            result.getValidationMessages().add(EngineMessage.ACTION_TYPE_NON_MONITORED.toString());
            result.setValid(false);
            result.setSucceeded(false);
        }
        else {
            if (!runAsInternal) {
                logExecution(parameters.getSessionId(), String.format("command %s", actionType));
            }
            CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, context);
            result = runAction(command, runAsInternal);
        }
        return result;
    }

    private boolean isActionExternal(VdcActionType actionType){
        return actionType == VdcActionType.EndExternalJob || actionType == VdcActionType.EndExternalStep || actionType == VdcActionType.ClearExternalJob;
    }

    protected VdcReturnValueBase runAction(CommandBase<?> command,
            boolean runAsInternal) {
        VdcReturnValueBase returnValue = evaluateCorrelationId(command);
        if (returnValue != null) {
            return returnValue;
        }
        command.setInternalExecution(runAsInternal);
        ExecutionHandler.prepareCommandForMonitoring(command, command.getActionType(), runAsInternal);

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
            log.warn("Validation of action '{}' failed. Reasons: {}", commandBase.getActionType(),
                    StringUtils.join(returnValue.getValidationMessages(), ','));

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
                    || sessionDataContainer.getUser(sessionId, parameters.getRefresh()) == null) {
                log.debug("Unable to execute query {} as no user session was found", actionType);
                return getErrorQueryReturnValue(EngineMessage.USER_IS_NOT_LOGGED_IN);
            }
            logExecution(sessionId,
                    String.format("query %s with isFiltered : %s", actionType, parameters.isFiltered()));
        }
        Class<CommandBase<? extends VdcActionParametersBase>> clazz =
                CommandsFactory.getQueryClass(actionType.name());
        if (clazz.isAnnotationPresent(DisableInMaintenanceMode.class)) {
            String mode = dbFacade.getVdcOptionDao().getByNameAndVersion
                    (ConfigValues.EngineMode.name(), ConfigCommon.defaultConfigurationVersion).getOptionValue();
            if (EngineWorkingMode.MAINTENANCE.name().equalsIgnoreCase(mode)) {
                return getErrorQueryReturnValue(EngineMessage.ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE);
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
            ArrayList<VdcActionParametersBase> parameters, boolean isRunOnlyIfAllValidationPass) {
        return runMultipleActions(actionType, parameters, isRunOnlyIfAllValidationPass, false);
    }

    @Override
    public ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, boolean isRunOnlyIfAllValidationPass, boolean waitForResult) {
        VdcReturnValueBase returnValue = notAllowToRunAction(actionType);
        if (returnValue != null) {
            ArrayList<VdcReturnValueBase> list = new ArrayList<>();
            list.add(returnValue);
            return list;
        } else {
            return runMultipleActionsImpl(actionType, parameters, false, isRunOnlyIfAllValidationPass, waitForResult, null);
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
            boolean isRunOnlyIfAllValidationPass,
            boolean isWaitForResult,
            CommandContext commandContext) {
        MultipleActionsRunner runner = MultipleActionsRunnersFactory.createMultipleActionsRunner(actionType,
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
    public VdcReturnValueBase logoff(VdcActionParametersBase parameters) {
        return runAction(VdcActionType.LogoutSession, parameters);
    }

    @Override
    public VdcQueryReturnValue runPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        parameters.setRefresh(false);
        switch (actionType) {
        case GetAAAProfileList:
        case RegisterVds:
        case CheckDBConnection:
        case GetDbUserBySession:
        case GetEngineSessionIdForSsoToken:
        case ValidateSession:
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
    public VdcReturnValueBase runInternalAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CommandContext context) {
        return runActionImpl(actionType, parameters, true, context);
    }

    private VdcReturnValueBase getErrorCommandReturnValue(EngineMessage message) {
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setValid(false);
        returnValue.getValidationMessages().add(message.toString());
        return returnValue;
    }

    private VdcReturnValueBase notAllowedInPrepForMaintMode(VdcActionType action) {
        Class<CommandBase<? extends VdcActionParametersBase>> clazz =
                CommandsFactory.getCommandClass(action.name());
        if (clazz.isAnnotationPresent(DisableInPrepareMode.class)) {
            return getErrorCommandReturnValue(EngineMessage.ENGINE_IS_RUNNING_IN_PREPARE_MODE);
        }
        return null;
    }

    private VdcQueryReturnValue getErrorQueryReturnValue(EngineMessage errorMessage) {
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(false);
        returnValue.setExceptionString(errorMessage.toString());
        return returnValue;
    }

    protected QueriesCommandBase<?> createQueryCommand(VdcQueryType actionType, VdcQueryParametersBase parameters, EngineContext engineContext) {
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
        dbFacade.populateDwhOsInfo(osRepository.getOsNames());
    }

    private void logExecution(String sessionId, String details) {
        DbUser user = sessionDataContainer.getUser(sessionId, false);
        log.debug("Executing {}{}",
                details,
                user == null ? "." : String.format(" for user %s@%s.", user.getLoginName(), user.getDomain()));
    }

   @Override
    public CommandBase<?> createAction(VdcActionType actionType, VdcActionParametersBase parameters, CommandContext context) {
        return CommandsFactory.createCommand(actionType, parameters, context);
    }

    @Override
    public VdcReturnValueBase runAction(CommandBase<?> action, ExecutionContext executionContext) {
        ExecutionHandler.setExecutionContextForTasks(action.getContext(),
                executionContext, action.getContext().getLock());
        return runAction(action, true);
    }

    @Override
    public VdcQueryReturnValue runInternalQuery(VdcQueryType queryType, VdcQueryParametersBase queryParameters) {
        return runInternalQuery(queryType, queryParameters, null);
    }
}
