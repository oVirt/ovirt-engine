package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.logging.ApplicationLogManager;
import org.ovirt.engine.ui.common.system.BaseApplicationInit;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.widget.AlertManager;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager.PluginsReadyCallback;
import org.ovirt.engine.ui.webadmin.uimode.UiModeData;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ApplicationInit extends BaseApplicationInit<LoginModel> implements PluginsReadyCallback {

    private final ApplicationDynamicMessages dynamicMessages;

    private boolean pluginsReady = false;

    @Inject
    public ApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user,
            Provider<LoginModel> loginModelProvider,
            LockInteractionManager lockInteractionManager,
            Frontend frontend,
            ApplicationLogManager applicationLogManager,
            AlertManager alertManager,
            ApplicationDynamicMessages dynamicMessages,
            CurrentUserRole currentUserRole,
            PluginManager pluginManager,
            ClientStorage clientStorage) {
        super(typeResolver, frontendEventsHandler, frontendFailureEventListener, user,
                loginModelProvider, lockInteractionManager, frontend, currentUserRole,
                applicationLogManager, alertManager, clientStorage);
        this.dynamicMessages = dynamicMessages;
        pluginManager.setPluginsReadyCallback(this);
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
    }

    @Override
    public void onPluginsReady() {
        pluginsReady = true;
    }

    @Override
    protected void performPlaceTransition() {
        // Make sure all plugins that need pre-loading have been loaded already
        if (!pluginsReady) {
            Scheduler.get().scheduleDeferred(() -> performPlaceTransition());
        } else {
            // Do the actual place transition
            super.performPlaceTransition();
        }
    }

    @Override
    protected boolean filterFrontendQueries() {
        return false;
    }

    @Override
    protected void onLogin(final LoginModel loginModel) {
        performLogin(loginModel);
    }
}
