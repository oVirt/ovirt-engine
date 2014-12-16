package org.ovirt.engine.ui.common.system;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.common.auth.AutoLoginData;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.CurrentUser.LogoutHandler;
import org.ovirt.engine.ui.common.auth.SSOTokenData;
import org.ovirt.engine.ui.common.logging.LocalStorageLogHandler;
import org.ovirt.engine.ui.common.restapi.EngineSessionTimeoutData;
import org.ovirt.engine.ui.common.restapi.RestApiSessionManager;
import org.ovirt.engine.ui.common.system.ApplicationFocusChangeEvent.ApplicationFocusChangeHandler;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.FrontendLoginHandler;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.communication.SSOTokenChangeEvent;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.Bootstrapper;

/**
 * Contains initialization logic that gets executed at application startup.
 *
 * @param <T>
 *            Login model type.
 */
public abstract class BaseApplicationInit<T extends LoginModel> implements Bootstrapper, LogoutHandler {

    private final RestApiSessionManager restApiSessionManager;
    private final ITypeResolver typeResolver;
    private final FrontendEventsHandlerImpl frontendEventsHandler;
    protected final FrontendFailureEventListener frontendFailureEventListener;

    protected final CurrentUser user;
    protected final EventBus eventBus;
    protected final Frontend frontend;
    private final CurrentUserRole currentUserRole;

    // Using Provider because any UiCommon model will fail before TypeResolver is initialized
    private final Provider<T> loginModelProvider;

    private final LockInteractionManager lockInteractionManager;
    private final LocalStorageLogHandler localStorageLogHandler;

    public BaseApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user, EventBus eventBus,
            Provider<T> loginModelProvider,
            LockInteractionManager lockInteractionManager,
            LocalStorageLogHandler localStorageLogHandler,
            Frontend frontend, CurrentUserRole currentUserRole,
            RestApiSessionManager restApiSessionManager) {
        this.typeResolver = typeResolver;
        this.frontendEventsHandler = frontendEventsHandler;
        this.frontendFailureEventListener = frontendFailureEventListener;
        this.user = user;
        this.eventBus = eventBus;
        this.loginModelProvider = loginModelProvider;
        this.lockInteractionManager = lockInteractionManager;
        this.localStorageLogHandler = localStorageLogHandler;
        this.frontend = frontend;
        this.currentUserRole = currentUserRole;
        this.restApiSessionManager = restApiSessionManager;
    }

    @Override
    public final void onBootstrap() {
        Logger rootLogger = Logger.getLogger(""); //$NON-NLS-1$
        initLocalStorageLogHandler(rootLogger);
        initUncaughtExceptionHandler(rootLogger);

        // Perform actual bootstrap via deferred command to ensure that
        // UncaughtExceptionHandler is effective during the bootstrap
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                performBootstrap();
            }
        });
    }

    void initLocalStorageLogHandler(Logger rootLogger) {
        // Configure and attach LocalStorageLogHandler
        localStorageLogHandler.init();
        rootLogger.addHandler(localStorageLogHandler);

        // Enable/disable LocalStorageLogHandler when the application window gains/looses its focus
        eventBus.addHandler(ApplicationFocusChangeEvent.getType(), new ApplicationFocusChangeHandler() {
            @Override
            public void onApplicationFocusChange(ApplicationFocusChangeEvent event) {
                localStorageLogHandler.setActive(event.isInFocus());
            }
        });
    }

    void initUncaughtExceptionHandler(final Logger rootLogger) {
        // Prevent uncaught exceptions from escaping application code
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable t) {
                rootLogger.log(Level.SEVERE, "Uncaught exception: ", t); //$NON-NLS-1$
            }
        });
    }

    /**
     * Actual initialization logic that bootstraps the application.
     * <p>
     * Subclasses must override this method to initiate GWTP place transition via {@code PlaceManager},
     * for example:
     * <pre>
     * super.performBootstrap(); // Common initialization (mandatory)
     * performCustomBootstrap(); // Custom initialization (optional)
     * placeManager.revealCurrentPlace(); // Initiate place transition
     * </pre>
     */
    protected void performBootstrap() {
        // Handle UI logout requests
        user.setLogoutHandler(this);

        // Initialize UiCommon infrastructure
        initUiCommon();
        initFrontend();
        initLoginModel();

        // Check if the user should be logged in automatically
        AutoLoginData autoLoginData = AutoLoginData.instance();
        if (autoLoginData != null) {
            handleAutoLogin(autoLoginData);
        }

        // Check for Engine user session timeout configuration
        EngineSessionTimeoutData engineSessionTimeoutData = EngineSessionTimeoutData.instance();
        if (engineSessionTimeoutData != null) {
            restApiSessionManager.setSessionTimeout(engineSessionTimeoutData.getSessionTimeout());
            restApiSessionManager.setHardLimit(engineSessionTimeoutData.getSessionHardLimit());
        }
    }

    protected T getLoginModel() {
        return loginModelProvider.get();
    }

    protected void initLoginModel() {
        final T loginModel = getLoginModel();

        // Add model login event handler
        loginModel.getLoggedInEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                onLogin(loginModel);
            }
        });

        loginModel.getCreateInstanceOnly().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                currentUserRole.setCreateInstanceOnly(loginModel.getCreateInstanceOnly().getEntity() );
            }
        });
    }

    /**
     * Called right after the model fires its login event.
     */
    protected  void onLogin(T loginModel) {
        restApiSessionManager.recordLoggedInTime();
    }

    @Override
    public void onLogout() {
        AsyncQuery query = new AsyncQuery();
        query.setHandleFailure(true);
        query.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                // Reload entire application after the user has logged out successfully on backend.
                Window.Location.reload();
            }
        };

        frontend.logoffAsync(query);
    }

    protected void performLogin(T loginModel) {
        DbUser loggedUser = loginModel.getLoggedUser();
        String loginPassword = loginModel.getPassword().getEntity();

        beforeLogin(loginModel);
        // UiCommon login preparation
        frontend.initLoggedInUser(loggedUser, loginPassword);

        // UI login actions
        user.onUserLogin(loggedUser);

        // Post-login actions
        loginModel.getPassword().setEntity(null);
    }

    /**
     * Invoked right after the user has logged in successfully on backend, before proceeding with UI login sequence.
     */
    protected abstract void beforeLogin(T loginModel);

    protected void initUiCommon() {
        // Set up UiCommon type resolver
        TypeResolver.initialize(typeResolver);
    }

    protected void initFrontend() {
        // Set up Frontend event handlers
        frontend.setEventsHandler(frontendEventsHandler);
        frontend.getFrontendFailureEvent().addListener(frontendFailureEventListener);

        frontend.getFrontendNotLoggedInEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                user.logout();
            }
        });

        frontend.setFilterQueries(filterFrontendQueries());

        // Configure REST API integration for availability of engine session id
        frontend.setLoginHandler(new FrontendLoginHandler() {
            @Override
            public void onLoginSuccess() {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        frontend.runQuery(VdcQueryType.GetEngineSessionIdToken,
                                new VdcQueryParametersBase(),
                                new AsyncQuery(new INewAsyncCallback() {
                                    @Override
                                    public void onSuccess(Object model, Object returnValue) {
                                        String engineAuthToken = ((VdcQueryReturnValue) returnValue).getReturnValue();
                                        restApiSessionManager.acquireSession(engineAuthToken);
                                    }
                                })
                        );
                    }
                });
            }

            @Override
            public void onLogout() {
                restApiSessionManager.releaseSession();
            }
        });
    }

    /**
     * Indicates if all queries triggered through {@link Frontend} should be filtered or not.
     * <p>
     * A query that is filtered has its results constrained by user permissions. On the other hand, a query that is not
     * filtered returns all matching results without additional constraints.
     */
    protected abstract boolean filterFrontendQueries();

    /**
     * When a user is already logged in on the server, the server provides user data within the host page.
     */
    protected void handleAutoLogin(AutoLoginData autoLoginData) {
        final DbUser loggedUser = autoLoginData.getDbUser();

        // Use deferred command because CommonModel change needs to happen
        // after all model providers have been properly initialized
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                lockInteractionManager.showLoadingIndicator();
                getLoginModel().autoLogin(loggedUser);
            }
        });

        SSOTokenChangeEvent.fire(eventBus, SSOTokenData.instance().getToken());

        // Indicate that the user should be logged in automatically
        user.setAutoLogin(true);

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                // Assume the REST API session has been acquired and is still active
                restApiSessionManager.reuseSession();
            }
        });
    }

}
