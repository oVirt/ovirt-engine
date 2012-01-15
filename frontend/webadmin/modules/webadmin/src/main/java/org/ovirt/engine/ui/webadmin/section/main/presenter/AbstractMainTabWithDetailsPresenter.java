package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.view.ApplicationFocusChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.OrderedMultiSelectionModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;

/**
 * Base class for main tab presenters that work with {@link ListWithDetailsModel}.
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
public abstract class AbstractMainTabWithDetailsPresenter<T, M extends ListWithDetailsModel, V extends AbstractMainTabWithDetailsPresenter.ViewDef<T>, P extends Proxy<?>>
        extends AbstractMainTabPresenter<T, M, V, P> {

    public interface ViewDef<T> extends View {

        /**
         * Returns the selection model used by the main tab table widget.
         */
        OrderedMultiSelectionModel<T> getTableSelectionModel();

        void onFocus();

        void onBlur();

    }

    public AbstractMainTabWithDetailsPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, MainModelProvider<T, M> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getTableSelectionModel()
                .addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        // Update main model selection
                        modelProvider.setSelectedItems(getSelectedItems());

                        // Let others know that the table selection has changed
                        fireTableSelectionChangeEvent();

                        // Update the layout
                        updateLayout();

                        // Reveal the appropriate place based on selection
                        if (hasSelection()) {
                            placeManager.revealPlace(getSubTabRequest());
                        } else {
                            placeManager.revealPlace(getMainTabRequest());
                        }
                    }
                }));
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        if (hasSelection()) {
            clearSelection();
        } else {
            updateLayout();
        }
    }

    /**
     * Subclasses should fire an event to indicate that the table selection has changed.
     */
    protected abstract void fireTableSelectionChangeEvent();

    void updateLayout() {
        setSubTabPanelVisible(hasSelection());
    }

    PlaceRequest getSubTabRequest() {
        PlaceRequest currentRequest = placeManager.getCurrentPlaceRequest();
        boolean subTabRequest = currentRequest.getNameToken().startsWith(
                getMainTabRequest().getNameToken() + ApplicationPlaces.SUB_TAB_PREFIX);

        return subTabRequest ? currentRequest : getDefaultSubTabRequest();
    }

    /**
     * Returns items currently selected in the table.
     */
    protected List<T> getSelectedItems() {
        return getView().getTableSelectionModel().getSelectedList();
    }

    /**
     * Returns {@code true} when there is at least one item selected in the table, {@code false} otherwise.
     */
    protected boolean hasSelection() {
        return !getSelectedItems().isEmpty();
    }

    /**
     * Deselects any selected values in the table.
     */
    protected void clearSelection() {
        getView().getTableSelectionModel().clear();
    }

    T getFirstSelectedItem() {
        return hasSelection() ? getSelectedItems().get(0) : null;
    }

    /**
     * Returns the place request associated with the default sub tab presenter.
     * <p>
     * Will be revealed when the table selection is not empty.
     */
    protected abstract PlaceRequest getDefaultSubTabRequest();

    @ProxyEvent
    public void onApplicationFocusChange(ApplicationFocusChangeEvent event) {
        GWT.log("ApplicationFocusChangeEvent(" + event.isInFocus() + ") caught by handler");
        if (event.isInFocus()) {
            getView().onFocus();
        } else {
            getView().onBlur();
        }
    }

}
