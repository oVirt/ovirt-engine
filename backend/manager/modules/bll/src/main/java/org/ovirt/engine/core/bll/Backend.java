package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.AsyncQueryResults;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.searchbackend.BaseConditionFieldAutoCompleter;
import org.ovirt.engine.core.utils.ErrorTranslatorImpl;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.ThreadLocalSessionCleanerInterceptor;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

// Here we use a Singleton bean
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access to all the
// business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state of the singleton is synchronized across all clients.
@Local({ BackendLocal.class, BackendInternal.class })
@Interceptors({ ThreadLocalSessionCleanerInterceptor.class })
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Backend implements BackendInternal, BackendRemote {

    @SuppressWarnings("unused")
    @EJB
    private SchedulerUtil scheduler;

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

    @ExcludeClassInterceptors
    public VDSBrokerFrontend getResourceManager() {
        return _resourceManger;
    }

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    @PostConstruct
    public void create() {
        checkDBConnectivity();
        Initialize();
    }

    private void checkDBConnectivity() {
        boolean dbUp = false;
        long expectedTimeout =
                System.currentTimeMillis()
                        + DbFacade.getInstance().getOnStartConnectionTimeout();
        long waitBetweenInterval = DbFacade.getInstance().getConnectionCheckInterval();
        while (!dbUp && System.currentTimeMillis() < expectedTimeout) {
            try {
                dbUp = DbFacade.getInstance().CheckDBConnection();
                try {
                    Thread.sleep(waitBetweenInterval);
                } catch (InterruptedException e) {
                    log.warn("Failed to wait between connection polling attempts. " +
                            "Original exception is: " + ExceptionUtils.getMessage(e));
                }
            } catch (RuntimeException ex) {
                log.error("Error in getting DB connection. The database is inaccessible. " +
                        "Original exception is: " + ExceptionUtils.getMessage(ex));
            }
        }
        if (!dbUp) {
            throw new IllegalStateException("Could not obtain connection to the database." +
                    " Please make sure that DB is up and accepting connections, and " +
                    "restart the application.");
        }
    }

    public DateTime getStartedAt() {
        return _startedAt;
    }

    /**
     * Initializes internal data
     *
     * <exception>VdcBLL.VdcBLLException
     */
    public void Initialize() {
        log.infoFormat("Start time: {0}", new java.util.Date());
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

        log.infoFormat("VDSBrokerFrontend: {0}", new java.util.Date());
        CpuFlagsManagerHandler.InitDictionaries();
        log.infoFormat("CpuFlagsManager: {0}", new java.util.Date());
        // ResourceManager res = ResourceManager.Instance;
        // Initialize the AuditLogCleanupManager
        AuditLogCleanupManager.getInstance();
        log.infoFormat("AuditLogCleanupManager: {0}", new java.util.Date());

        TagsDirector.getInstance();
        log.infoFormat("TagsDirector: {0}", new java.util.Date());
        ImagesSyncronizer.getInstance();
        log.infoFormat("ImagesSyncronizer: {0}", new java.util.Date());
        IsoDomainListSyncronizer.getInstance();
        log.infoFormat("IsoDomainListSyncronizer: {0}", new java.util.Date());
        InitHandlers();
        log.infoFormat("InitHandlers: {0}", new java.util.Date());

        final String AppErrorsFileName = "bundles/AppErrors.properties";
        final String VdsErrorsFileName = "bundles/VdsmErrors.properties";
        errorsTranslator = new ErrorTranslatorImpl(AppErrorsFileName, VdsErrorsFileName);
        log.infoFormat("ErrorTranslator: {0}", new java.util.Date());
        _vdsErrorsTranslator = new ErrorTranslatorImpl(VdsErrorsFileName);
        log.infoFormat("VdsErrorTranslator: {0}", new java.util.Date());

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

      try{
            File fLock = new File(Config.<String> GetValue(ConfigValues.SignLockFile));
            if (fLock.exists()) {
                if (! fLock.delete()) {
                    log.error("Cleanup lockfile failed to delete the locking file.");
                }
            }
        }
        catch (SecurityException se) {
            log.error("Cleanup lockfile failed!", se);
       }
    }

    /**
     * Handles compensation in case of uncompleted compensation-aware commands resulted from server failure.
     */
    private void compensate() {
        // get all command snapshot entries
        List<KeyValue> commandSnapshots =
                DbFacade.getInstance().getBusinessEntitySnapshotDAO().getAllCommands();
        for (KeyValue commandSnapshot : commandSnapshots) {
            // create an instance of the related command by its class name and command id
            CommandBase<?> cmd =
                        CommandsFactory.CreateCommand(commandSnapshot.getValue().toString(),
                                (Guid) commandSnapshot.getKey());
            if (cmd != null) {
                cmd.compensate();
                log.infoFormat("Running compensation on startup for Command : {0} , Command Id : {1}",
                            commandSnapshot.getValue(), commandSnapshot.getKey());
            } else {
                log.errorFormat("Failed to run compensation on startup for Command {0} , Command Id : {1}",
                            commandSnapshot.getValue(), commandSnapshot.getKey());
            }
        }
    }

    @ExcludeClassInterceptors
    public VdcReturnValueBase runInternalAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return runActionImpl(actionType, parameters, true, null);
    }

    public VdcReturnValueBase RunAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return runActionImpl(actionType, parameters, false, null);
    }

    private VdcReturnValueBase runActionImpl(VdcActionType actionType, VdcActionParametersBase parameters,
                                             boolean runAsInternal, CompensationContext context) {
        switch (actionType) {
        case AutoLogin:
            VdcReturnValueBase returnValue = new VdcReturnValueBase();
            returnValue.setCanDoAction(false);
            returnValue.getCanDoActionMessages().add(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION.toString());
            return returnValue;

        default: {
            CommandBase command = CommandsFactory.CreateCommand(actionType, parameters);
            command.setInternalExecution(runAsInternal);
            if (context != null) {
                command.setCompensationContext(context);
            }
            return command.ExecuteAction();
        }

        }
    }

    @Override
    public VdcReturnValueBase endAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CompensationContext compensationContext) {
        CommandBase<?> command = CommandsFactory.CreateCommand(actionType, parameters);

        if (compensationContext != null) {
            command.setCompensationContext(compensationContext);
        }

        return command.EndAction();
    }

    public VdcReturnValueBase EndAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return endAction(actionType, parameters, null);
    }

    @ExcludeClassInterceptors
    public VdcQueryReturnValue runInternalQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runQueryImpl(actionType, parameters, false);
    }

    public VdcQueryReturnValue RunQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runQueryImpl(actionType, parameters, true);
    }

    private VdcQueryReturnValue runQueryImpl(VdcQueryType actionType, VdcQueryParametersBase parameters,
                                             boolean isPerformUserCheck) {
        if (isPerformUserCheck) {
            String sessionId = addSessionToContext(parameters);
            if (StringHelper.isNullOrEmpty(sessionId)
                    || SessionDataContainer.getInstance().GetData(sessionId, "VdcUser", parameters.getRefresh()) == null) {
                VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
                returnValue.setSucceeded(false);
                returnValue.setExceptionString(VdcBllMessages.USER_IS_NOT_LOGGED_IN.toString());
                return returnValue;
            }
        }
        QueriesCommandBase command = CommandsFactory.CreateQueryCommand(actionType, parameters);
        command.Execute();
        return command.getQueryReturnValue();

    }

    public void RunAsyncQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        addSessionToContext(parameters);
        QueriesCommandBase command = CommandsFactory.CreateQueryCommand(actionType, parameters);
        command.Execute();
    }

    private String addSessionToContext(VdcQueryParametersBase parameters) {
        String sessionId = parameters.getHttpSessionId();
        boolean isAddToContext = true;
        if (StringHelper.isNullOrEmpty(sessionId)) {
            sessionId = parameters.getSessionId();
        }
        // This is a workaround for front end
        // Where no session, try to get Id of session which was attached to
        // request
        if (StringHelper.isNullOrEmpty(sessionId)) {
            sessionId = ThreadLocalParamsContainer.getHttpSessionId();
            isAddToContext = false;
        }
        if (!StringHelper.isNullOrEmpty(sessionId) && isAddToContext) {
            ThreadLocalParamsContainer.setHttpSessionId(sessionId);
        }
        return sessionId;
    }

    @Override
    public java.util.ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters) {
        return runMultipleActionsImpl(actionType, parameters, false);
    }

    @Override
    @ExcludeClassInterceptors
    public java.util.ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters) {
        return runMultipleActionsImpl(actionType, parameters, true);
    }

    public java.util.ArrayList<VdcReturnValueBase> runMultipleActionsImpl(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters,
            boolean isInternal) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (!StringHelper.isNullOrEmpty(sessionId)) {
            for (VdcActionParametersBase parameter : parameters) {
                if (StringHelper.isNullOrEmpty(parameter.getSessionId())) {
                    parameter.setSessionId(sessionId);
                }
            }
        }
        MultipleActionsRunner runner = MultipleActionsRunnersFactory.CreateMultipleActionsRunner(actionType,
                parameters, isInternal);
        return runner.Execute();
    }

    @ExcludeClassInterceptors
    public ErrorTranslator getErrorsTranslator() {
        return errorsTranslator;
    }

    @ExcludeClassInterceptors
    public ErrorTranslator getVdsErrorsTranslator() {
        return _vdsErrorsTranslator;
    }

    /**
     * Login in to the system
     *
     * @param parameters
     *            The parameters.
     * @return user if success, else null // //
     */
    public VdcReturnValueBase Login(LoginUserParameters parameters) {
        switch (parameters.getActionType()) {
        case AutoLogin:
        case LoginAdminUser: {
            CommandBase command = CommandsFactory.CreateCommand(parameters.getActionType(), parameters);
            return command.ExecuteAction();
        }
        default: {
            return NotAutorizedError();
        }

        }
    }

    private VdcReturnValueBase NotAutorizedError() {
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(false);
        returnValue.getCanDoActionMessages().add(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION.toString());
        return returnValue;
    }

    public VdcReturnValueBase Logoff(LogoutUserParameters parameters) {
        return RunAction(VdcActionType.LogoutUser, parameters);
    }

    /**
     * @param vdsId
     * @return
     */
    public VDSStatus PowerUpVDS(int vdsId) {
        return VDSStatus.Up;
    }

    /**
     * @param vdsId
     * @return
     */
    public VDSStatus ShutDownVDS(int vdsId) {
        return VDSStatus.Down;
    }

    public tags GetTagByTagName(String tagName) {
        throw new NotImplementedException();
    }

    public String GetTagIdsAndChildrenIdsByRegExp(String tagNameRegExp) {
        throw new NotImplementedException();
    }

    public String GetTagIdAndChildrenIds(int tagId) {
        throw new NotImplementedException();
    }

    public VdcQueryReturnValue RunPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        switch (actionType) {
        case GetDomainList:
        case GetLicenseProperties:
        case RegisterVds:
        case CheckDBConnection:
            return runQueryImpl(actionType, parameters, false);
        case GetConfigurationValue: {
            GetConfigurationValueParameters configParameters = (GetConfigurationValueParameters) parameters;
            if (configParameters.getConfigValue() == ConfigurationValues.VdcVersion ||
                    configParameters.getConfigValue() == ConfigurationValues.ProductRPMVersion) {
                return runQueryImpl(actionType, parameters, false);
            } else {
                VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
                returnValue.setSucceeded(false);
                returnValue.setExceptionString(VdcBllMessages.USER_CANNOT_RUN_QUERY_NOT_PUBLIC.toString());
                return returnValue;
            }
        }
        default: {
            VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
            returnValue.setSucceeded(false);
            returnValue.setExceptionString(VdcBllMessages.USER_CANNOT_RUN_QUERY_NOT_PUBLIC.toString());
            return returnValue;
        }
        }
    }

    public VdcReturnValueBase RunUserAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        if (StringHelper.isNullOrEmpty(parameters.getHttpSessionId())) {
            return NotAutorizedError();
        } else {
            return RunAction(actionType, parameters);
        }
    }

    public VdcQueryReturnValue RunUserQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runQueryImpl(actionType, parameters, true);
    }

    public java.util.ArrayList<VdcReturnValueBase> RunUserMultipleActions(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters) {
        for (VdcActionParametersBase parameter : parameters) {
            if (StringHelper.isNullOrEmpty(parameter.getHttpSessionId())) {
                java.util.ArrayList<VdcReturnValueBase> returnValues = new java.util.ArrayList<VdcReturnValueBase>();
                for (int i = 0; i < parameters.size(); i++) {
                    returnValues.add(NotAutorizedError());
                }
                return returnValues;
            }
        }

        return runInternalMultipleActions(actionType, parameters);

    }

    public VdcReturnValueBase RunAutoAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        return RunAction(actionType, parameters);
    }

    public VdcQueryReturnValue RunAutoQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        return runInternalQuery(actionType, parameters);
    }

    public AsyncQueryResults GetAsyncQueryResults() {
        return BackendCallBacksDirector.getInstance().GetAsyncQueryResults();
    }

    private static LogCompat log = LogFactoryCompat.getLog(Backend.class);

    @Override
    @ExcludeClassInterceptors
    public VdcReturnValueBase runInternalAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CompensationContext context) {
        return runActionImpl(actionType, parameters, true, context);
    }
}
