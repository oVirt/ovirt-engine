package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.MainModelSelectionChangeEvent;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Base class for main tab presenters.
 *
 * @param <T>
 *            Table row data type or {@code Void} if tab view doesn't provide table widget.
 * @param <M>
 *            Main model type.
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractMainTabPresenter<T, M extends SearchableListModel, V extends View,
    P extends TabContentProxyPlace<?>> extends AbstractTabPresenter<V, P> {

    protected final PlaceManager placeManager;
    protected final MainModelProvider<T, M> modelProvider;

    @Inject
    private Provider<CommonModel> commonModelProvider;

    public AbstractMainTabPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, MainModelProvider<T, M> modelProvider) {
        super(eventBus, view, proxy, MainTabPanelPresenter.TYPE_SetTabContent);
        this.placeManager = placeManager;
        this.modelProvider = modelProvider;
    }

    /**
     * We use manual reveal since we want to prevent users from accessing this presenter when the corresponding main
     * model is not available.
     */
    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Notify model provider that the tab has been revealed
        modelProvider.onMainTabSelected();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        // Reveal presenter only when the main model is available
        if (getModel().getIsAvailable()) {
            getProxy().manualReveal(this);
        } else {
            getProxy().manualRevealFailed();
            revealActiveMainModelPresenter();
        }
    }

    protected M getModel() {
        return modelProvider.getModel();
    }

    void revealActiveMainModelPresenter() {
        MainModelSelectionChangeEvent.fire(this, commonModelProvider.get().getSelectedItem());
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
