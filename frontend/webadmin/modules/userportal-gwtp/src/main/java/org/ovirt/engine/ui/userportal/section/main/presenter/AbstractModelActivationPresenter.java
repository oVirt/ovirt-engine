package org.ovirt.engine.ui.userportal.section.main.presenter;

import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Base class for presenters bound to UiCommon list models whose activation needs to be handled by the application.
 * <p>
 * When {@linkplain #onReveal revealed}, the associated UiCommon model will be activated, with any other models being
 * stopped. This ensures that only the model of the currently visible (revealed) presenter is active at the given time.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractModelActivationPresenter<T, M extends SearchableListModel, V extends View, P extends Proxy<?>> extends Presenter<V, P> {

    protected final SearchableModelProvider<T, M> modelProvider;

    public AbstractModelActivationPresenter(EventBus eventBus, V view, P proxy,
            SearchableModelProvider<T, M> modelProvider,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
        this.modelProvider = modelProvider;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        // Activate model
        SearchableListModel currentModel = modelProvider.getModel();
        currentModel.getSearchCommand().execute();
    }

    @Override
    protected void onHide() {
        super.onHide();
        // Stop model
        SearchableListModel currentModel = modelProvider.getModel();
        currentModel.setItems(null);
        currentModel.stopRefresh();
    }
}
