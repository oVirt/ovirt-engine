package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * Base class for main tab presenters.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Main model type.
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractMainTabPresenter<T, M extends SearchableListModel, V extends View, P extends Proxy<?>> extends Presenter<V, P> {

    protected final PlaceManager placeManager;
    protected final MainModelProvider<T, M> modelProvider;

    public AbstractMainTabPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, MainModelProvider<T, M> modelProvider) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;
        this.modelProvider = modelProvider;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Notify model provider that the tab has been revealed
        modelProvider.onMainTabSelected();
    }

    @ProxyEvent
    public void onMainModelSelectionChange(MainModelSelectionChangeEvent event) {
        if (event.getMainModel() == modelProvider.getModel()) {
            // Reveal main tab place when the corresponding model is selected
            placeManager.revealPlace(getMainTabRequest());
        }
    }

    /**
     * Returns the place request associated with this main tab presenter.
     */
    protected abstract PlaceRequest getMainTabRequest();

    /**
     * Controls the sub tab panel visibility.
     */
    protected void setSubTabPanelVisible(boolean subTabPanelVisible) {
        UpdateMainContentLayoutEvent.fire(this, subTabPanelVisible);
    }

}
