package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.place.ApplicationPlaceManager;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
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
public abstract class AbstractMainPresenter<T, M extends SearchableListModel, V extends View,
    P extends ProxyPlace<?>> extends Presenter<V, P> {

    protected final ApplicationPlaceManager placeManager;
    protected final MainModelProvider<T, M> modelProvider;
    private final ActionPanelPresenterWidget<?, ?, ?> actionPanel;

    public AbstractMainPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, MainModelProvider<T, M> modelProvider,
            ActionPanelPresenterWidget<?, ?, M> actionPanel) {
        super(eventBus, view, proxy, MainContentPresenter.TYPE_SetContent);
        this.actionPanel = actionPanel;
        this.placeManager = (ApplicationPlaceManager) placeManager;
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
        modelProvider.onMainViewSelected();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        // Reveal presenter only when the main model is available
        if (getModel().getIsAvailable()) {
            getProxy().manualReveal(this);
        } else {
            getProxy().manualRevealFailed();
        }
    }

    protected M getModel() {
        return modelProvider.getModel();
    }

    /**
     * Returns the place request associated with this main tab presenter.
     */
    protected abstract PlaceRequest getMainViewRequest();

    public boolean placeMatches(String placeName) {
        return getMainViewRequest().getNameToken().equals(placeName);
    }

    public ActionPanelPresenterWidget<?, ?, ?> getActionPanelPresenterWidget() {
        return actionPanel;
    }

}
