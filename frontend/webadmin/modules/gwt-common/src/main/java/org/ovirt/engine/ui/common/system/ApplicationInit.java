package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.auth.AutoLoginData;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Contains initialization logic that gets executed at application startup.
 */
public class ApplicationInit {

    private final ITypeResolver typeResolver;
    private final FrontendEventsHandlerImpl frontendEventsHandler;
    private final FrontendFailureEventListener frontendFailureEventListener;
    private final CurrentUser user;

    // Using Provider because any UiCommon model will fail before TypeResolver is initialized
    private final Provider<LoginModel> loginModelProvider;

    @Inject
    public ApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user, Provider<LoginModel> loginModelProvider) {
        this.typeResolver = typeResolver;
        this.frontendEventsHandler = frontendEventsHandler;
        this.frontendFailureEventListener = frontendFailureEventListener;
        this.user = user;
        this.loginModelProvider = loginModelProvider;

        initUiCommon();
        initFrontend();
        handleAutoLogin();
    }

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
                    loginModelProvider.get().AutoLogin(vdcUser);
                }
            });

            // Indicate that the user should be logged in automatically
            user.setAutoLogin(true);
        }
    }

}
