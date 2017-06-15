package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.presenter.SetDynamicTabAccessibleEvent.SetDynamicTabAccessibleHandler;
import org.ovirt.engine.ui.common.widget.tab.AbstractCompositeTab;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Base class for dynamic tab presenters.
 * <p>
 * Dynamic tab presenters have their proxies created and bound dynamically during runtime, as opposed to using GWTP
 * {@code ProxyCodeSplit} or {@code ProxyStandard} annotations that generate proxy implementations via deferred binding.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class DynamicTabPresenter<V extends View, P extends DynamicTabProxy<?>> extends Presenter<V, P> implements SetDynamicTabAccessibleHandler {

    private final PlaceManager placeManager;

    private boolean tabAccessible = AbstractCompositeTab.DEFAULT_ACCESSIBLE;

    public DynamicTabPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
        this.placeManager = placeManager;
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(SetDynamicTabAccessibleEvent.getType(), this));
    }

    @Override
    public void onSetDynamicTabAccessible(SetDynamicTabAccessibleEvent event) {
        if (getProxy().getTargetHistoryToken().equals(event.getHistoryToken())) {
            setTabAccessible(event.isTabAccessible());
        }
    }

    void setTabAccessible(boolean tabAccessible) {
        this.tabAccessible = tabAccessible;

        // If the current place matches dynamic tab presenter, reveal
        // the place again in order to apply presenter reveal process
        // after tab accessibility change
        String currentPlaceToken = placeManager.getCurrentPlaceRequest().getNameToken();
        if (getProxy().getTargetHistoryToken().equals(currentPlaceToken)) {
            placeManager.revealCurrentPlace();
        }
    }

    boolean isTabAccessible() {
        return tabAccessible;
    }

    /**
     * We use manual reveal since we want to control dynamic tab presenter accessibility.
     */
    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        // Reveal presenter only when the tab is marked as accessible
        if (isTabAccessible()) {
            getProxy().manualReveal(this);
        } else {
            getProxy().manualRevealFailed();
        }
    }

}
