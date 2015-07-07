package org.ovirt.engine.ui.userportal.section.main.presenter;

import java.util.List;

import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableTableModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Base class for side tab presenters that work with {@link ListWithDetailsModel}.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Model type.
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSideTabWithDetailsPresenter<T, M extends ListWithDetailsModel,
    V extends AbstractSideTabWithDetailsPresenter.ViewDef<T>, P extends Proxy<?>>
        extends AbstractModelActivationPresenter<T, M, V, P> {

    public interface ViewDef<T> extends View, HasActionTable<T> {

        /**
         * Controls the sub tab panel visibility.
         */
        void setSubTabPanelVisible(boolean subTabPanelVisible);

    }

    private final PlaceManager placeManager;
    protected final UserPortalSearchableTableModelProvider<T, M> modelProvider;

    public AbstractSideTabWithDetailsPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, UserPortalSearchableTableModelProvider<T, M> modelProvider) {
        super(eventBus, view, proxy, modelProvider, MainTabExtendedPresenter.TYPE_SetTabContent);
        this.modelProvider = modelProvider;
        this.placeManager = placeManager;
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getTable().getSelectionModel()
                .addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        // Update model selection
                        modelProvider.setSelectedItems(getSelectedItems());

                        // Let others know that the table selection has changed
                        fireTableSelectionChangeEvent();

                        // Update the layout
                        updateLayout();

                        // Reveal the appropriate place based on selection
                        if (hasSelection()) {
                            placeManager.revealPlace(getSubTabRequest());
                        } else {
                            placeManager.revealPlace(getSideTabRequest());
                        }
                    }
                }));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().getTable().setLoadingState(LoadingState.LOADING);
        // Clear table selection and update sub tab panel visibility
        if (hasSelection()) {
            clearSelection();
        } else {
            updateLayout();
        }

        getView().getTable().resetScrollPosition();
    }

    @Override
    public void onHide() {
        super.onHide();
        modelProvider.clearCurrentItems();
    }

    /**
     * Subclasses should fire an event to indicate that the table selection has changed.
     */
    protected abstract void fireTableSelectionChangeEvent();

    void updateLayout() {
        getView().setSubTabPanelVisible(hasSelection());
    }

    /**
     * Returns the place request associated with this side tab presenter.
     */
    protected abstract PlaceRequest getSideTabRequest();

    private PlaceRequest getSubTabRequest() {
        return PlaceRequestFactory.get(createRequestToken());
    }

    protected String createRequestToken() {
        String subTabName = modelProvider.getModel().getActiveDetailModel().getHashName();
        String requestToken = getSideTabRequest().getNameToken() + UserPortalApplicationPlaces.SUB_TAB_PREFIX + subTabName;
        return requestToken;
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
