package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.system.BaseApplicationInit;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.common.restapi.RestApiSessionManager;
import org.ovirt.engine.ui.webadmin.uimode.UiModeData;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class ApplicationInit extends BaseApplicationInit<LoginModel> {

    private final PlaceManager placeManager;
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
            CurrentUserRole currentUserRole, Provider<CommonModel> commonModelProvider) {
        super(typeResolver, frontendEventsHandler, frontendFailureEventListener,
                user, eventBus, loginModelProvider, lockInteractionManager, frontend, currentUserRole,
                commonModelProvider, restApiSessionManager);
        this.placeManager = placeManager;
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

        // Initiate transition to requested application place
        placeManager.revealCurrentPlace();
    }

    @Override
    protected void beforeLogin(LoginModel loginModel) {
    }

    @Override
    protected boolean filterFrontendQueries() {
        return false;
    }

    @Override
    protected void onLogin(final LoginModel loginModel) {
        super.onLogin(loginModel);

        // Initialize reports
        ReportInit.getInstance().init();

        // Update Reports availability after reports xml has been retrieved
        ReportInit.getInstance().getReportsInitEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateReportsAvailability();
            }
        });

        performLogin(loginModel);
    }

    @Override
    protected void initFrontend() {
        super.initFrontend();

        ReportInit.getInstance().initHandlers(eventBus);
    }

    void handleUiMode(UiModeData uiModeData) {
        ApplicationMode uiMode = uiModeData.getUiMode();
        ApplicationModeHelper.setUiMode(uiMode);
    }

}
