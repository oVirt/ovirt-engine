package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Base class for sub tab panel presenters.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSubTabPanelPresenter<V extends TabView & DynamicTabPanel, P extends Proxy<?>> extends DynamicTabContainerPresenter<V, P> {

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Type<RevealContentHandler<?>> revealContentEventType,
            Type<RequestTabsHandler> requestTabsEventType) {
        super(eventBus, view, proxy, revealContentEventType, requestTabsEventType);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainContentPresenter.TYPE_SetSubTabPanelContent, this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Show sub tab panel when revealing sub tab presenter
        UpdateMainContentLayoutEvent.fire(this, true);
    }

}
