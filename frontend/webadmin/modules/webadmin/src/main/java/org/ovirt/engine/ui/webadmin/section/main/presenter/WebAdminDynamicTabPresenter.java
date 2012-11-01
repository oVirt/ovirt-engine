package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.DynamicTabPresenter;
import org.ovirt.engine.ui.common.presenter.DynamicTabProxy;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public abstract class WebAdminDynamicTabPresenter<V extends View, P extends DynamicTabProxy<?>> extends DynamicTabPresenter<V, P> {

    public WebAdminDynamicTabPresenter(EventBus eventBus, V view, P proxy, PlaceManager placeManager) {
        super(eventBus, view, proxy, placeManager);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Hide the sub tab panel in case of dynamic main tab
        if (isMainTab()) {
            UpdateMainContentLayoutEvent.fire(this, false);
        }
    }

    protected abstract boolean isMainTab();

}
