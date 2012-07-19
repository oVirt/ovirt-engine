package org.ovirt.engine.ui.common.presenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.RequestTabsEvent;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabPanel;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.TabContentProxy;

/**
 * A {@link TabContainerPresenter} that handles tabs added dynamically during runtime.
 * <p>
 * To add new tab during runtime, create and bind its proxy instance so that it listens to GWTP {@link RequestTabsEvent}
 * for the given tab container presenter.
 */
public abstract class DynamicTabContainerPresenter<V extends View & DynamicTabContainerPresenter.DynamicTabPanel, P extends Proxy<?>>
        extends TabContainerPresenter<V, P> {

    /**
     * Tab panel interface used with {@link DynamicTabContainerPresenter}.
     */
    public interface DynamicTabPanel extends TabPanel {

        /**
         * Sets the currently active tab's history token.
         */
        void setActiveTabHistoryToken(String historyToken);

    }

    private final Object tabContentSlot;
    private final Type<RequestTabsHandler> requestTabsEventType;

    public DynamicTabContainerPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot, Type<RequestTabsHandler> requestTabsEventType) {
        super(eventBus, view, proxy, tabContentSlot, requestTabsEventType);
        this.tabContentSlot = tabContentSlot;
        this.requestTabsEventType = requestTabsEventType;
    }

    @Override
    public void setInSlot(Object slot, PresenterWidget<?> content) {
        super.setInSlot(slot, content);

        // Update the view with regard to current tab's history token
        // in order to retain "active" tab after refreshing all tabs
        if (slot == tabContentSlot) {
            try {
                Presenter<?, ?> presenter = (Presenter<?, ?>) content;
                TabContentProxy<?> proxy = (TabContentProxy<?>) presenter.getProxy();
                getView().setActiveTabHistoryToken(proxy.getTargetHistoryToken());
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    /**
     * Removes and re-adds all related tabs to this tab container.
     * <p>
     * This method should be typically called using deferred command, since event bus handler registration (GWTP
     * {@link RequestTabsEvent}) is enqueued for consequent handler registration calls.
     */
    public void refreshTabs() {
        getView().removeTabs();
        RequestTabsEvent.fire(this, requestTabsEventType, this);
    }

}
