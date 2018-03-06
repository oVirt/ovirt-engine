package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.widget.tab.DynamicTabData;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Provider;
import com.gwtplatform.common.client.StandardProvider;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import com.gwtplatform.mvp.client.proxy.NonLeafTabContentProxyImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceWithGatekeeper;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlaceImpl;

/**
 * Presenter proxy implementation for {@link DynamicTabPresenter} subclasses.
 * <p>
 * Code for wrapped proxy implementation should match GWTP-generated {@code TabContentProxyPlaceImpl} subclasses for
 * presenters acting as tab content (presenters revealed within a {@code TabContainerPresenter}).
 *
 * @param <T>
 *            Presenter type.
 */
public abstract class DynamicTabProxy<T extends DynamicTabPresenter<?, ?>> extends TabContentProxyPlaceImpl<T> implements Provider<T> {

    public static class WrappedProxy<T extends DynamicTabPresenter<?, ?>> extends NonLeafTabContentProxyImpl<T> {

        public WrappedProxy(PlaceManager placeManager, EventBus eventBus,
                Provider<T> presenterProvider,
                Type<RequestTabsHandler> requestTabsEventType,
                Type<ChangeTabHandler> changeTabEventType,
                String label, float priority, String historyToken) {
            bind(placeManager, eventBus);
            this.tabData = new DynamicTabData(label, (int)priority, historyToken);
            this.targetHistoryToken = historyToken;
            this.requestTabsEventType = requestTabsEventType;
            this.changeTabEventType = changeTabEventType;
            this.presenter = new StandardProvider<>(presenterProvider);
            addRequestTabsHandler();
        }

    }

    private T presenter;

    public DynamicTabProxy(PlaceManager placeManager, EventBus eventBus,
            Gatekeeper gatekeeper,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            String label, float priority, String historyToken) {
        bind(placeManager, eventBus);
        setProxy(new WrappedProxy<>(placeManager, eventBus, this,
                requestTabsEventType, changeTabEventType, label, priority, historyToken));
        setPlace(new PlaceWithGatekeeper(historyToken, gatekeeper));

        // Create and bind presenter eagerly (don't wait for reveal request)
        Scheduler.get().scheduleDeferred(() -> get());
    }

    @Override
    public void manualRevealFailed() {
        super.manualRevealFailed();
        getPlaceManager().revealDefaultPlace();
    }

    @Override
    public final T get() {
        if (presenter == null) {
            presenter = createPresenter();
            presenter.bind();
        }

        return presenter;
    }

    /**
     * Instantiates the associated presenter.
     * <p>
     * This method is called when the presenter is requested for the first time (subsequent requests reuse the existing
     * presenter instance).
     */
    protected abstract T createPresenter();

}
