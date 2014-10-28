package org.ovirt.engine.ui.common.presenter;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.RequestTabsEvent;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabPanel;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxy;

/**
 * A {@link TabContainerPresenter} that handles tabs added dynamically during runtime.
 * <p>
 * To add new tab during runtime, create and bind its proxy to listen to GWTP {@link RequestTabsEvent} for the given tab
 * container presenter.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class DynamicTabContainerPresenter<V extends TabView & DynamicTabContainerPresenter.DynamicTabPanel, P extends Proxy<?>>
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

    private static final Logger logger = Logger.getLogger(DynamicTabContainerPresenter.class.getName());

    private final Object tabContentSlot;
    private final Type<RequestTabsHandler> requestTabsEventType;

    public DynamicTabContainerPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, tabContentSlot,
                requestTabsEventType, changeTabEventType, slot);
        this.tabContentSlot = tabContentSlot;
        this.requestTabsEventType = requestTabsEventType;
    }

    @Override
    // TODO-GWT: override setInSlot to get hold of TabContentProxy in case of Presenter
    public void setInSlot(Object slot, PresenterWidget<?> content) {
        super.setInSlot(slot, content);

        // Update the view with regard to current tab's history token
        // in order to retain "active" tab after refreshing all tabs
        if (slot == getTabContentSlot()) {
            try {
                Presenter<?, ?> presenter = (Presenter<?, ?>) content;
                TabContentProxy<?> proxy = (TabContentProxy<?>) presenter.getProxy();
                getView().setActiveTabHistoryToken(proxy.getTargetHistoryToken());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while updating tab view", e); //$NON-NLS-1$
            }
        }
    }

    Object getTabContentSlot() {
        return tabContentSlot;
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(RedrawDynamicTabContainerEvent.getType(),
                new RedrawDynamicTabContainerEvent.RedrawDynamicTabContainerHandler() {

            @Override
            public void onRedrawDynamicTabContainer(RedrawDynamicTabContainerEvent event) {
                if (requestTabsEventType == event.getRequestTabsEventType()) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            // Remove all tabs
                            getView().removeTabs();

                            // Re-add tabs in response to RequestTabsEvent
                            RequestTabsEvent.fire(DynamicTabContainerPresenter.this,
                                        requestTabsEventType, DynamicTabContainerPresenter.this);
                        }
                    });
                }
            }
        }));
    }
}
