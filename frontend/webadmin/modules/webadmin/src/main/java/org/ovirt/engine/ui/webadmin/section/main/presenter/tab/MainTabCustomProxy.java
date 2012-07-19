package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;
import com.gwtplatform.common.client.StandardProvider;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceWithGatekeeper;
import com.gwtplatform.mvp.client.proxy.TabContentProxyImpl;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlaceImpl;

/**
 * Custom {@link MainTabCustomPresenter} proxy implementation.
 */
public class MainTabCustomProxy extends TabContentProxyPlaceImpl<MainTabCustomPresenter> implements MainTabCustomPresenter.ProxyDef {

    public static class WrappedProxy extends TabContentProxyImpl<MainTabCustomPresenter> {

        public WrappedProxy(PlaceManager placeManager, EventBus eventBus,
                Provider<MainTabCustomPresenter> presenterProvider,
                String label, float priority, String historyToken) {
            bind(placeManager, eventBus);
            requestTabsEventType = MainTabPanelPresenter.TYPE_RequestTabs;
            tabData = new TabDataBasic(label, priority);
            targetHistoryToken = historyToken;
            addRequestTabsHandler();
            presenter = new StandardProvider<MainTabCustomPresenter>(presenterProvider);
        }

    }

    public MainTabCustomProxy(ClientGinjector ginjector,
            MainTabCustomPresenterProvider presenterProvider,
            String label, float priority, String historyToken) {
        bind(ginjector.getPlaceManager(), ginjector.getEventBus());
        proxy = new WrappedProxy(ginjector.getPlaceManager(), ginjector.getEventBus(),
                presenterProvider, label, priority, historyToken);
        place = new PlaceWithGatekeeper(historyToken, ginjector.getDefaultGatekeeper());
        presenterProvider.setProxy(this);
    }

}
