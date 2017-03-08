package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
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
    DynamicTabPanel, P extends Proxy<?>> extends DynamicTabContainerPresenter<V, P> {

    public interface ViewDef extends TabView {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabBar = new Type<>();

    protected final AbstractMainTabSelectedItems<?> selectedItems;

    @Inject
    private PlaceManager placeManager;

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            AbstractMainTabSelectedItems<?> selectedItems) {
        this(eventBus, view, proxy, tabContentSlot, requestTabsEventType, changeTabEventType,
                selectedItems, MainContentPresenter.TYPE_SetSubTabPanelContent);
    }

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            AbstractMainTabSelectedItems<?> selectedItems,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, tabContentSlot, requestTabsEventType, changeTabEventType,
                slot);
        this.selectedItems = selectedItems;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        // Show sub tab
        UpdateMainContentLayoutEvent.fire(this, UpdateMainContentLayout.ContentDisplayType.SUB, null);
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
