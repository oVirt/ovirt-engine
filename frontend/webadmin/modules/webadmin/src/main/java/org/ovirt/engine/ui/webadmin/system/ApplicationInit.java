package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.logging.ApplicationLogManager;
import org.ovirt.engine.ui.common.system.BaseApplicationInit;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.widget.AlertManager;
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
import org.ovirt.engine.ui.webadmin.uimode.UiModeData;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class ApplicationInit extends BaseApplicationInit<LoginModel> {

    private final PlaceManager placeManager;
    private final ApplicationDynamicMessages dynamicMessages;

    private final Provider<CommonModel> commonModelProvider;

    @Inject
    public ApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user,
            EventBus eventBus,
            Provider<LoginModel> loginModelProvider,
            LockInteractionManager lockInteractionManager,
            Frontend frontend,
            ApplicationLogManager applicationLogManager,
            AlertManager alertManager,
            PlaceManager placeManager,
            ApplicationDynamicMessages dynamicMessages,
            CurrentUserRole currentUserRole,
            Provider<CommonModel> commonModelProvider) {
        super(typeResolver, frontendEventsHandler, frontendFailureEventListener, user,
                eventBus, loginModelProvider, lockInteractionManager, frontend, currentUserRole,
                applicationLogManager, alertManager);
        this.placeManager = placeManager;
        this.dynamicMessages = dynamicMessages;
        this.commonModelProvider = commonModelProvider;
    }

    @Override
    protected void performBootstrap() {
        super.performBootstrap();
        Window.setTitle(dynamicMessages.applicationTitle());

        // Check for ApplicationMode configuration
        ApplicationMode uiMode = UiModeData.getUiMode();
        if (uiMode != null) {
            ApplicationModeHelper.setUiMode(uiMode);
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
        // Initialize reports
        ReportInit.getInstance().setSsoToken(user.getEngineSessionId());
        ReportInit.getInstance().init();

        // Update Reports availability after reports xml has been retrieved
        ReportInit.getInstance().getReportsInitEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                commonModelProvider.get().updateReportsAvailability();
            }
        });

        performLogin(loginModel);
    }

}
