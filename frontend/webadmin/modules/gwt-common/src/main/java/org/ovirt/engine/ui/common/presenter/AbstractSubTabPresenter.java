package org.ovirt.engine.ui.common.presenter;

import java.util.List;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.OvirtSelectionModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Base class for presenters representing sub tabs that react to item selection changes within main tab presenters.
 *
 * @param <T> Main tab table row data type.
 * @param <M> Main model type (extends ListWithDetailsModel)
 * @param <D> Detail model type extends HasEntity
 * @param <V> View type (extends AbstractSubTabPresenter.ViewDef)
 * @param <P> Proxy type (extends TabContentProxyPlace)
 */
public abstract class AbstractSubTabPresenter<T, M extends ListWithDetailsModel, D extends HasEntity,
  V extends AbstractSubTabPresenter.ViewDef<T>, P extends TabContentProxyPlace<?>>
        extends AbstractTabPresenter<V, P> implements MainSelectedItemChangeListener<T> {

    // TODO(vszocs) use HasActionTable<I> instead of raw type HasActionTable, this will
    // require adding new type parameter to presenter (do later as part of refactoring)
    @SuppressWarnings("rawtypes")
    public interface ViewDef<T> extends View, HasActionTable {

        /**
         * Notifies the view that the main tab item selection has changed.
         */
        void setMainSelectedItem(T selectedItem);

        void resizeToFullHeight();

        HandlerRegistration addWindowResizeHandler(ResizeHandler handler);
    }

    private static final Logger logger = Logger.getLogger(AbstractSubTabPresenter.class.getName());

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetActionPanel = new Type<>();

    private final PlaceManager placeManager;
    private final DetailModelProvider<M, D> modelProvider;
    private final AbstractMainSelectedItems<T> selectedMainItems;

    /**
     * @param view View type (extends AbstractSubTabPresenter.ViewDef&lt;T&gt;)
     * @param proxy Proxy type (extends TabContentProxyPlace)
     * @param modelProvider DetailModelProvider&lt;M, D&gt; -  M - Main model type (extends ListWithDetailsModel),
     *                                                         D - Detail model type extends HasEntity
     */
    public AbstractSubTabPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, DetailModelProvider<M, D> modelProvider,
            AbstractMainSelectedItems<T> selectedMainItems,
            ActionPanelPresenterWidget<?, M> actionPanelPresenterWidget,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, actionPanelPresenterWidget, slot);
        this.placeManager = placeManager;
        this.modelProvider = modelProvider;
        this.selectedMainItems = selectedMainItems;
    }

    protected ActionTable<?> getTable() {
        return getView().getTable();
    }

    @Override
    protected void onBind() {
        super.onBind();

        OvirtSelectionModel<?> tableSelectionModel = getTable() != null ? getTable().getSelectionModel() : null;
        if (tableSelectionModel != null) {
            registerHandler(tableSelectionModel.addSelectionChangeHandler(event -> {
                // Update detail model selection
                updateDetailModelSelection();
            }));
        }
        OvirtSelectionModel<T> mainModelSelectionModel = modelProvider.getMainModel().getSelectionModel();
        if (mainModelSelectionModel != null) {
            registerHandler(mainModelSelectionModel.addSelectionChangeHandler(event -> {
                itemChanged(getSelectedMainItems().getSelectedItem());
            }));
        }
        initializeHandlers();
        registerHandler(getView().addWindowResizeHandler(e -> {
            Scheduler.get().scheduleDeferred(() -> getView().resizeToFullHeight());
        }));
        getSelectedMainItems().registerListener(this);
        itemChanged(getSelectedMainItems().getSelectedItem());
        setInSlot(TYPE_SetActionPanel, getActionPanelPresenterWidget());
    }

    @Override
    public void itemChanged(T item) {
        boolean widgetVisible = getView().asWidget().isVisible();
        if (item != null && widgetVisible) {
            getView().setMainSelectedItem(item);
        } else if (item == null && widgetVisible && (modelProvider.getMainModel().getItems() == null
                || modelProvider.getMainModel().getItems().isEmpty())) {
            // No selection so we can't positively show anything, switch to grid.
            placeManager.revealPlace(getMainContentRequest());
        }
    }

    /**
     * Updates the detail model with items currently selected in the main tab.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void updateDetailModelSelection() {
        if (modelProvider instanceof SearchableDetailModelProvider) {
            ((SearchableDetailModelProvider) modelProvider).setSelectedItems(getSelectedItems());
        }
    }

    /**
     * We use manual reveal since we want to prevent users from accessing this presenter when there is nothing selected
     * in the main tab.
     */
    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Notify model provider that the tab has been revealed
        modelProvider.onSubTabSelected();

        Object entity = modelProvider.getModel().getEntity();
        if (entity != null) {
            onDetailModelEntityChange(entity);
        }
        if (hasActionPanelPresenterWidget() && getTable() != null) {
            getTable().setActionMenus(getActionPanelPresenterWidget().getActionButtons());
        }
    }

    @Override
    protected void onHide() {
        super.onHide();
        // Notify model provider that the tab has been hidden
        modelProvider.onSubTabDeselected();
        if (getTable() != null) {
            getTable().hideContextMenu();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Clear table selection before starting
        clearSelection();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        // Reveal presenter only when there is something selected in the main tab
        if (selectedMainItems.hasSelection()) {
            getProxy().manualReveal(this);
        } else {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(getMainContentRequest());
        }
    }

    /**
     * Returns the place request associated with the main tab presenter for this sub tab.
     * <p>
     * Will be revealed when the user tries to access this sub tab while there is nothing selected in the main tab.
     */
    protected abstract PlaceRequest getMainContentRequest();

    /**
     * Returns items currently selected in the table or {@code null} if the sub tab view has no table widget associated.
     */
    protected List<?> getSelectedItems() {
        return getTable() != null ? getTable().getSelectionModel().asMultiSelectionModel().getSelectedList() : null;
    }

    /**
     * De-selects any items currently selected in the table. Does nothing if the sub tab view has no table widget
     * associated.
     */
    protected void clearSelection() {
        if (getTable() != null) {
            getTable().getSelectionModel().clear();
        }
    }

    public void initializeHandlers() {
        // Notify view when the entity of the detail model changes
        modelProvider.getModel().getEntityChangedEvent().addListener((ev, sender, args) -> {
            Object entity = modelProvider.getModel().getEntity();
            if (entity != null) {
                onDetailModelEntityChange(entity);
            }
        });

        // Notify view when the detail model indicates progress
        modelProvider.getModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                if (modelProvider.getModel().getProgress() != null) {
                    Scheduler.get().scheduleDeferred(() -> {
                        if (getTable() != null) {
                            getTable().setLoadingState(LoadingState.LOADING);
                        }
                    });
                }
            }
        });
    }

    /**
     * Override this method in case the detail model entity type is different from main model item type.
     */
    @SuppressWarnings("unchecked")
    protected void onDetailModelEntityChange(Object entity) {
        try {
            getView().setMainSelectedItem((T) entity);
        } catch (ClassCastException ex) {
            // Detail model entity type is different from main model item type.
            // This usually happens with synthetic item types that wrap multiple
            // logical entities into single type. Since views can typically edit
            // a single item type, we can do nothing here.
            logger.warning("Detail model entity type is different from main model item type"); // $NON-NLS-1$
        }
    }

    protected DetailModelProvider<M, D> getModelProvider() {
        return modelProvider;
    }

    protected AbstractMainSelectedItems<T> getSelectedMainItems() {
        return selectedMainItems;
    }

}
