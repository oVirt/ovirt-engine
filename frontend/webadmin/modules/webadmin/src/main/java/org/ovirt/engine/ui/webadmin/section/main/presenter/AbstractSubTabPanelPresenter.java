package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.presenter.ScrollableTabBarPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Base class for sub tab panel presenters.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSubTabPanelPresenter<V extends AbstractSubTabPanelPresenter.ViewDef &
    DynamicTabPanel, P extends Proxy<?>> extends DynamicTabContainerPresenter<V, P> implements TabWidgetHandler {

    public interface ViewDef extends TabView, HasUiHandlers<TabWidgetHandler> {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabBar = new Type<>();

    protected final AbstractMainTabSelectedItems<?> selectedItems;
    protected final ScrollableTabBarPresenterWidget tabBar;

    @Inject
    private PlaceManager placeManager;

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            AbstractMainTabSelectedItems<?> selectedItems,
            ScrollableTabBarPresenterWidget tabBar) {
        this(eventBus, view, proxy, tabContentSlot, requestTabsEventType, changeTabEventType,
                tabBar, selectedItems, MainContentPresenter.TYPE_SetSubTabPanelContent);
    }

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            ScrollableTabBarPresenterWidget tabBar,
            AbstractMainTabSelectedItems<?> selectedItems,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, tabContentSlot, requestTabsEventType, changeTabEventType,
                slot);
        getView().setUiHandlers(tabBar);
        this.selectedItems = selectedItems;
        this.tabBar = tabBar;
        this.tabBar.setWantsOffset(false);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_SetTabBar, tabBar);
        // Show sub tab panel when revealing sub tab presenter
        UpdateMainContentLayoutEvent.fire(this, true);
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        tabBar.addTabWidget(tabWidget, index);
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        tabBar.removeTabWidget(tabWidget);
    }

    protected PlaceRequest getMainTabRequest() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        if (getProxy() instanceof TabContentProxyPlace && getMainTabRequest() != null) {
            // Reveal presenter only when there is something selected in the main tab
            if (selectedItems != null && selectedItems.hasSelection()) {
                ((TabContentProxyPlace<AbstractSubTabPanelPresenter<V, P>>)getProxy()).manualReveal(this);
            } else {
                ((TabContentProxyPlace<AbstractSubTabPanelPresenter<V, P>>)getProxy()).manualRevealFailed();
                placeManager.revealPlace(getMainTabRequest());
            }
        }
    }
}
