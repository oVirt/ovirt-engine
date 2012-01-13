package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.OrderedMultiSelectionModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Base class for sub tab presenters.
 *
 * @param <T>
 *            Main tab table row data type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSubTabPresenter<T, M extends ListWithDetailsModel, D extends EntityModel, V extends AbstractSubTabPresenter.ViewDef<T>, P extends ProxyPlace<?>>
        extends Presenter<V, P> {

    public interface ViewDef<T> extends View {

        /**
         * Notifies the view that the main tab table selection has changed.
         */
        void setMainTabSelectedItem(T selectedItem);

        /**
         * For table-based sub tab views, returns the selection model used by the sub tab table widget. Returns
         * {@code null} otherwise.
         */
        OrderedMultiSelectionModel<?> getTableSelectionModel();

        void setLoadingState(LoadingState state);
    }

    private final PlaceManager placeManager;
    private final DetailModelProvider<M, D> modelProvider;

    private List<T> mainTabSelectedItems;

    public AbstractSubTabPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, DetailModelProvider<M, D> modelProvider) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;
        this.modelProvider = modelProvider;
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    protected void onBind() {
        super.onBind();

        OrderedMultiSelectionModel<?> tableSelectionModel = getView().getTableSelectionModel();
        if (tableSelectionModel != null) {
            registerHandler(tableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    // Update detail model selection
                    updateDetailModelSelection();
                }
            }));
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Notify model provider that the tab has been revealed
        modelProvider.onSubTabSelected();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Clear table selection before starting
        clearSelection();
    }

    /**
     * Update the detail model selection with currently selected items in the table.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateDetailModelSelection() {
        if (modelProvider instanceof SearchableDetailModelProvider) {
            ((SearchableDetailModelProvider) modelProvider).setSelectedItems(getSelectedItems());
        }
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        // Reveal presenter only when there is something selected in the main tab table
        if (hasMainTabSelection()) {
            getProxy().manualReveal(this);
        } else {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(getMainTabRequest());
        }
    }

    boolean hasMainTabSelection() {
        return mainTabSelectedItems != null && !mainTabSelectedItems.isEmpty();
    }

    T getMainTabSelectedItem() {
        return hasMainTabSelection() ? mainTabSelectedItems.get(0) : null;
    }

    /**
     * Notifies this sub tab presenter that the main tab table selection has changed.
     */
    protected void updateMainTabSelection(List<T> mainTabSelectedItems) {
        this.mainTabSelectedItems = mainTabSelectedItems;

        T firstSelectedItem = getMainTabSelectedItem();

        // Notify view of selection change
        if (firstSelectedItem != null) {
            getView().setMainTabSelectedItem(firstSelectedItem);
        }
    }

    /**
     * Returns the place request associated with the main tab presenter for this sub tab.
     * <p>
     * Will be revealed when the user tries to navigate to this sub tab while there is nothing selected in the main tab
     * table.
     */
    protected abstract PlaceRequest getMainTabRequest();

    /**
     * Returns {@code true} when the view exposes a table selection model.
     */
    boolean hasTableSelectionModel() {
        return getView().getTableSelectionModel() != null;
    }

    /**
     * Returns items currently selected in the table.
     */
    protected List<?> getSelectedItems() {
        return hasTableSelectionModel() ? getView().getTableSelectionModel().getSelectedList() : null;
    }

    /**
     * Deselects any selected values in the table.
     */
    protected void clearSelection() {
        if (hasTableSelectionModel()) {
            getView().getTableSelectionModel().clear();
        }
    }

    @ProxyEvent
    public void onCommonModelChange(CommonModelChangeEvent event) {
        modelProvider.getModel().getEntityChangedEvent().addListener(new IEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                Object entity = modelProvider.getModel().getEntity();
                if (entity != null) {
                    getView().setMainTabSelectedItem((T) entity);
                }
            }
        });

        modelProvider.getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;
                if ("Progress".equals(pcArgs.PropertyName)) {
                    if (modelProvider.getModel().getProgress() != null) {
                        getView().setLoadingState(LoadingState.LOADING);
                    }
                }
            }
        });
    }
    protected DetailModelProvider<M, D> getModelProvider() {
        return modelProvider;
    }
}
