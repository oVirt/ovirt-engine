package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Creates and binds {@link MainTabCustomProxy} instances.
 */
public class MainTabCustomProxyFactory {

    private final ClientGinjector ginjector;
    private final Provider<MainTabCustomPresenterProvider> provider;
    private final Provider<MainTabPanelPresenter> tabPanelPresenterProvider;

    @Inject
    public MainTabCustomProxyFactory(ClientGinjector ginjector,
            Provider<MainTabCustomPresenterProvider> provider,
            Provider<MainTabPanelPresenter> tabPanelPresenterProvider) {
        this.ginjector = ginjector;
        this.provider = provider;
        this.tabPanelPresenterProvider = tabPanelPresenterProvider;
    }

    public MainTabCustomProxy create(String label, String historyToken, String contentUrl) {
        MainTabCustomPresenterProvider presenterProvider = provider.get();
        presenterProvider.setContentUrl(contentUrl);

        // Create and bind tab proxy
        MainTabCustomProxy proxy = new MainTabCustomProxy(
                ginjector, presenterProvider, label, Float.MAX_VALUE, historyToken);

        // Refresh tab panel using deferred command
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                tabPanelPresenterProvider.get().refreshTabs();
            }
        });

        return proxy;
    }

}
