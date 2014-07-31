package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.auth.AutoLoginData;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.system.BaseApplicationInit;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.model.CommonModelManager;
import org.ovirt.engine.ui.common.uicommon.model.ModelInitializedEvent;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.FrontendLoginHandler;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.plugin.restapi.EngineSessionTimeoutData;
import org.ovirt.engine.ui.webadmin.plugin.restapi.RestApiSessionManager;
import org.ovirt.engine.ui.webadmin.uimode.UiModeData;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class ApplicationInit extends BaseApplicationInit<LoginModel> {

    private final PlaceManager placeManager;
    private final RestApiSessionManager restApiSessionManager;
    private final ApplicationDynamicMessages dynamicMessages;

    @Inject
    public ApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user,
            EventBus eventBus,
            Provider<LoginModel> loginModelProvider,
            LockInteractionManager lockInteractionManager,
            Frontend frontend,
            PlaceManager placeManager,
            RestApiSessionManager restApiSessionManager,
            ApplicationDynamicMessages dynamicMessages,
            CurrentUserRole currentUserRole) {
        super(typeResolver, frontendEventsHandler, frontendFailureEventListener,
                user, eventBus, loginModelProvider, lockInteractionManager, frontend, currentUserRole);
        this.placeManager = placeManager;
        this.restApiSessionManager = restApiSessionManager;
        this.dynamicMessages = dynamicMessages;

    }

    @Override
    public void onBootstrap() {
        super.onBootstrap();
        Window.setTitle(dynamicMessages.applicationTitle());

        // Check for ApplicationMode configuration
        UiModeData uiModeData = UiModeData.instance();
        if (uiModeData != null) {
            handleUiMode(uiModeData);
        }

        // Check for Engine user session timeout configuration
        EngineSessionTimeoutData engineSessionTimeoutData = EngineSessionTimeoutData.instance();
        if (engineSessionTimeoutData != null) {
            restApiSessionManager.setSessionTimeout(engineSessionTimeoutData.getValue());
        }

        // Initiate transition to requested application place
        placeManager.revealCurrentPlace();
    }

    @Override
    protected void beforeLogin(LoginModel loginModel) {
        CommonModelManager.init(eventBus);
        ModelInitializedEvent.fire(eventBus);
    }

    @Override
    protected boolean filterFrontendQueries() {
        return false;
    }

    @Override
    protected void onLogin(final LoginModel loginModel) {
        // Initialize reports
        ReportInit.getInstance().init();

        // Perform login only after reports have been initialized
        ReportInit.getInstance().getReportsInitEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                performLogin(loginModel);
            }
        });
    }

    @Override
    protected void initFrontend() {
        super.initFrontend();

        ReportInit.getInstance().initHandlers(eventBus);
        // Configure REST API integration for UI plugin infrastructure
        frontend.setLoginHandler(new FrontendLoginHandler() {
            @Override
            public void onLoginSuccess(final String userName, final String password, final String domain) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        final String domainToken = "@"; //$NON-NLS-1$
                        restApiSessionManager.acquireSession(
                                userName.contains(domainToken) ? userName : userName + domainToken + domain,
                                password);
                    }
                });
            }

            @Override
            public void onLogout() {
                restApiSessionManager.releaseSession();
            }
        });
    }

    @Override
    protected void handleAutoLogin(AutoLoginData autoLoginData) {
        super.handleAutoLogin(autoLoginData);

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                // Assume the REST API session has been acquired and is still active
                restApiSessionManager.reuseSession();
            }
        });
    }

    void handleUiMode(UiModeData uiModeData) {
        ApplicationMode uiMode = uiModeData.getUiMode();
        ApplicationModeHelper.setUiMode(uiMode);
    }

}
