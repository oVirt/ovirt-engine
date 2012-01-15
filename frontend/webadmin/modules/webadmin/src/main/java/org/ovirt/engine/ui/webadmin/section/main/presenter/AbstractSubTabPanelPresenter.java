package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * Base class for sub tab panel presenters.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSubTabPanelPresenter<V extends TabView, P extends Proxy<?>> extends TabContainerPresenter<V, P> {

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot, Type<RequestTabsHandler> requestTabsEventType) {
        super(eventBus, view, proxy, tabContentSlot, requestTabsEventType);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainContentPresenter.TYPE_SetSubTabPanelContent, this);
    }

}
