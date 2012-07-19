package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

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
public abstract class AbstractMainTabWithDetailsPresenter<T, M extends ListWithDetailsModel, V extends AbstractMainTabWithDetailsPresenter.ViewDef<T>, P extends ProxyPlace<?>>
        extends AbstractMainTabPresenter<T, M, V, P> {

    public interface ViewDef<T> extends View, HasActionTable<T> {
    }

    public AbstractMainTabWithDetailsPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, MainModelProvider<T, M> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getTable().getSelectionModel()
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

        // Clear table selection and update sub tab panel visibility
        if (hasSelection()) {
            clearSelection();
        } else {
            updateLayout();
        }

        getView().getTable().resetScrollPosition();
    }

    /**
     * Subclasses should fire an event to indicate that the table selection has changed.
     */
    protected abstract void fireTableSelectionChangeEvent();

    void updateLayout() {
        setSubTabPanelVisible(hasSelection());
    }

    PlaceRequest getSubTabRequest() {
        String subTabName = modelProvider.getModel().getActiveDetailModel().getHashName();
        String requestToken = getMainTabRequest().getNameToken() + ApplicationPlaces.SUB_TAB_PREFIX + subTabName;

        return new PlaceRequest(requestToken);
    }

    /**
     * Returns items currently selected in the table.
     */
    protected List<T> getSelectedItems() {
        return getView().getTable().getSelectionModel().getSelectedList();
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
        getView().getTable().getSelectionModel().clear();
    }

}
