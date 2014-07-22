package org.ovirt.engine.ui.common.system;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.auth.AutoLoginData;
import org.ovirt.engine.ui.common.auth.CurrentUser.LogoutHandler;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.SSOTokenData;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.model.CleanupModelEvent;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.communication.SSOTokenChangeEvent;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.gwtplatform.mvp.client.Bootstrapper;

/**
 * Contains initialization logic that gets executed at application startup.
 *
 * @param <T>
 *            Login model type.
 */
public abstract class BaseApplicationInit<T extends LoginModel> implements Bootstrapper, LogoutHandler {

    private final ITypeResolver typeResolver;
    private final FrontendEventsHandlerImpl frontendEventsHandler;
    protected final FrontendFailureEventListener frontendFailureEventListener;

    protected final CurrentUser user;
    protected final EventBus eventBus;
    protected final Frontend frontend;
    private CurrentUserRole currentUserRole;

    // Using Provider because any UiCommon model will fail before TypeResolver is initialized
    private final Provider<T> loginModelProvider;

    private final LockInteractionManager lockInteractionManager;

    public BaseApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user, EventBus eventBus,
            Provider<T> loginModelProvider,
            LockInteractionManager lockInteractionManager,
            Frontend frontend, CurrentUserRole currentUserRole) {
        this.typeResolver = typeResolver;
        this.frontendEventsHandler = frontendEventsHandler;
        this.frontendFailureEventListener = frontendFailureEventListener;
        this.user = user;
        this.eventBus = eventBus;
        this.loginModelProvider = loginModelProvider;
        this.lockInteractionManager = lockInteractionManager;
        this.frontend = frontend;
        this.currentUserRole = currentUserRole;
    }

    @Override
    public void onBootstrap() {
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
    }

    protected T getLoginModel() {
        return loginModelProvider.get();
    }

    protected void initLoginModel() {
        final T loginModel = getLoginModel();

        // Add model login event handler
        loginModel.getLoggedInEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onLogin(loginModel);
            }
        });

        loginModel.getCreateInstanceOnly().getEntityChangedEvent().addListener(
                new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        currentUserRole.setCreateInstanceOnly(loginModel.getCreateInstanceOnly().getEntity() );
                    }
                }
        );
    }

    /**
     * Called right after the model fires its login event.
     */
    protected abstract void onLogin(T loginModel);

    @Override
    public abstract void onLogout();

    protected void performLogout() {
        getLoginModel().resetAfterLogout();
        user.onUserLogout();
        AsyncDataProvider.clearCache();
        CleanupModelEvent.fire(eventBus);
    }

    protected void performLogin(T loginModel) {
        DbUser loggedUser = loginModel.getLoggedUser();
        String loginPassword = loginModel.getPassword().getEntity();

        // UiCommon login preparation
        frontend.initLoggedInUser(loggedUser, loginPassword);
        beforeUiCommonInitEvent(loginModel);
        UiCommonInitEvent.fire(eventBus);

        // UI login actions
        user.onUserLogin(loggedUser);

        // Post-login actions
        loginModel.getPassword().setEntity(null);
    }

    /**
     * Called right before {@link UiCommonInitEvent} gets fired.
     * <p>
     * Any remaining UiCommon initialization logic should be performed here.
     */
    protected abstract void beforeUiCommonInitEvent(T loginModel);

    protected void initUiCommon() {
        // Set up UiCommon type resolver
        TypeResolver.initialize(typeResolver);
    }

    protected void initFrontend() {
        // Set up Frontend event handlers
        frontend.setEventsHandler(frontendEventsHandler);
        frontend.getFrontendFailureEvent().addListener(frontendFailureEventListener);

        frontend.getFrontendNotLoggedInEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                user.logout();
            }
        });

        frontend.setFilterQueries(filterFrontendQueries());
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
    }

}
