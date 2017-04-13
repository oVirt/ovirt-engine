package org.ovirt.engine.ui.webadmin.system;

import java.util.ArrayList;
import java.util.List;

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
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager.PluginsReadyCallback;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicMainTabAddedEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicMainTabAddedEvent.DynamicMainTabAddedHandler;
import org.ovirt.engine.ui.webadmin.uimode.UiModeData;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ApplicationInit extends BaseApplicationInit<LoginModel> implements PluginsReadyCallback,
        DynamicMainTabAddedHandler {

    static class DynamicMainTabInfo {

        String historyToken;
        String searchPrefix;

        DynamicMainTabInfo(String historyToken, String searchPrefix) {
            this.historyToken = historyToken;
            this.searchPrefix = searchPrefix;
        }

    }

    private final ApplicationDynamicMessages dynamicMessages;
    private final Provider<CommonModel> commonModelProvider;

    private boolean pluginsReady = false;
    private boolean loginComplete = false;

    private final List<DynamicMainTabInfo> dynamicMainTabs = new ArrayList<>();

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
            ApplicationDynamicMessages dynamicMessages,
            CurrentUserRole currentUserRole,
            Provider<CommonModel> commonModelProvider, PluginManager pluginManager) {
        super(typeResolver, frontendEventsHandler, frontendFailureEventListener, user,
                eventBus, loginModelProvider, lockInteractionManager, frontend, currentUserRole,
                applicationLogManager, alertManager);
        this.dynamicMessages = dynamicMessages;
        this.commonModelProvider = commonModelProvider;
        pluginManager.setPluginsReadyCallback(this);
        eventBus.addHandler(DynamicMainTabAddedEvent.getType(), this);
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

    @Override
    protected void afterLogin() {
        loginComplete = true;

        for (DynamicMainTabInfo tabInfo : dynamicMainTabs) {
            addDynamicMainTabToCommonModel(tabInfo.historyToken, tabInfo.searchPrefix);
        }
        dynamicMainTabs.clear();
    }

    @Override
    public void onDynamicMainTabAdded(DynamicMainTabAddedEvent event) {
        String historyToken = event.getHistoryToken();
        String searchPrefix = event.getSearchPrefix();

        if (loginComplete) {
            addDynamicMainTabToCommonModel(historyToken, searchPrefix);
        } else {
            dynamicMainTabs.add(new DynamicMainTabInfo(historyToken, searchPrefix));
        }
    }

    void addDynamicMainTabToCommonModel(String historyToken, String searchPrefix) {
        commonModelProvider.get().addPluginModel(historyToken, searchPrefix);
    }

}
