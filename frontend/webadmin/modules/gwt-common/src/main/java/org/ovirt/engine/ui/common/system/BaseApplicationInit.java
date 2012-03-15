package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.auth.AutoLoginData;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.CurrentUser.LogoutHandler;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;

/**
 * Contains initialization logic that gets executed at application startup.
 *
 * @param <T>
 *            Login model type.
 */
public abstract class BaseApplicationInit<T extends LoginModel> implements LogoutHandler {

    private final ITypeResolver typeResolver;
    private final FrontendEventsHandlerImpl frontendEventsHandler;
    private final FrontendFailureEventListener frontendFailureEventListener;

    protected final CurrentUser user;
    protected final EventBus eventBus;

    // Using Provider because any UiCommon model will fail before TypeResolver is initialized
    private final Provider<T> loginModelProvider;

    public BaseApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user, Provider<T> loginModelProvider,
            EventBus eventBus) {
        this.typeResolver = typeResolver;
        this.frontendEventsHandler = frontendEventsHandler;
        this.frontendFailureEventListener = frontendFailureEventListener;
        this.user = user;
        this.loginModelProvider = loginModelProvider;
        this.eventBus = eventBus;

        user.setLogoutHandler(this);

        initUiCommon();
        initLoginModel();
        initFrontend();

        handleAutoLogin();
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
    }

    /**
     * Called right after the model fires its login event.
     */
    protected abstract void onLogin(T loginModel);

    @Override
    public void onLogout() {
        user.onUserLogout();
    }

    protected void performLogin(T loginModel) {
        VdcUser loggedUser = loginModel.getLoggedUser();

        // UiCommon login preparation
        Frontend.setLoggedInUser(loggedUser);
        beforeUiCommonInitEvent(loginModel);
        UiCommonInitEvent.fire(eventBus);

        // UI login actions
        user.onUserLogin(loggedUser.getUserName());
        clearPassword(loginModel);
    }

    void clearPassword(T loginModel) {
        String password = (String) loginModel.getPassword().getEntity();

        if (password != null) {
            // Replace all password characters with whitespace
            password = password.replaceAll(".", " ");
        }

        loginModel.getPassword().setEntity(password);
    }

    /**
     * Called right before {@link UiCommonInitEvent} gets fired.
     * <p>
     * Any remaining UiCommon initialization logic should be performed here.
     */
    protected abstract void beforeUiCommonInitEvent(T loginModel);

    void initUiCommon() {
        // Set up UiCommon type resolver
        TypeResolver.Initialize(typeResolver);
    }

    void initFrontend() {
        // Set up Frontend event handlers
        Frontend.initEventsHandler(frontendEventsHandler);
        Frontend.getFrontendFailureEvent().addListener(frontendFailureEventListener);

        Frontend.getFrontendNotLoggedInEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                user.logout();
            }
        });
    }

    /**
     * When a user is already logged in on the server, the server provides user data within the host page.
     */
    void handleAutoLogin() {
        AutoLoginData autoLoginData = AutoLoginData.instance();

        if (autoLoginData != null) {
            final VdcUser vdcUser = autoLoginData.getVdcUser();

            // Use deferred command because CommonModel change needs to happen
            // after all model providers have been properly initialized
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    getLoginModel().AutoLogin(vdcUser);
                }
            });

            // Indicate that the user should be logged in automatically
            user.setAutoLogin(true);
        }
    }

}
