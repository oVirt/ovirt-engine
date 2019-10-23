package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.ui.common.place.ApplicationPlaceManager;
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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.presenter.slots.Slot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest.Builder;

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
        extends AbstractTabPresenter<V, P> implements PlaceTransitionHandler, MainSelectedItemChangeListener<T> {

    @GenEvent
    public class DetailItemSelectionChange {

        List<?> selectedItems;

    }

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

        void setPlaceTransitionHandler(PlaceTransitionHandler handler);
    }

    private static final Logger logger = Logger.getLogger(AbstractSubTabPresenter.class.getName());

    public static final Slot<ActionPanelPresenterWidget<?, ?, ?>> TYPE_SetActionPanel = new Slot<>();

    private final ApplicationPlaceManager placeManager;
    private final DetailModelProvider<M, D> modelProvider;
    private final AbstractMainSelectedItems<T> selectedMainItems;
    private boolean resizing = false;
    private PlaceRequest currentPlace;

    /**
     * @param view View type (extends AbstractSubTabPresenter.ViewDef&lt;T&gt;)
     * @param proxy Proxy type (extends TabContentProxyPlace)
     * @param modelProvider DetailModelProvider&lt;M, D&gt; -  M - Main model type (extends ListWithDetailsModel),
     *                                                         D - Detail model type extends HasEntity
     */
    public AbstractSubTabPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, DetailModelProvider<M, D> modelProvider,
            AbstractMainSelectedItems<T> selectedMainItems,
            ActionPanelPresenterWidget<?, ?, M> actionPanelPresenterWidget,
            NestedSlot slot) {
        super(eventBus, view, proxy, actionPanelPresenterWidget, slot);
        this.placeManager = (ApplicationPlaceManager) placeManager;
        this.modelProvider = modelProvider;
        this.selectedMainItems = selectedMainItems;
    }

    protected ActionTable<?> getTable() {
        return getView().getTable();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPlaceTransitionHandler(this);
        OvirtSelectionModel<?> tableSelectionModel = getTable() != null ? getTable().getSelectionModel() : null;
        if (tableSelectionModel != null) {
            registerHandler(tableSelectionModel.addSelectionChangeHandler(event -> {
                // Update detail model selection
                updateDetailModelSelection();
            }));
        }
        OvirtSelectionModel<T> mainModelSelectionModel = getMainModel().getSelectionModel();
        if (mainModelSelectionModel != null) {
            registerHandler(mainModelSelectionModel.addSelectionChangeHandler(event -> {
                itemChanged(mainModelSelectionModel.getFirstSelectedObject());
            }));
        }
        initializeHandlers();
        registerHandler(getView().addWindowResizeHandler(e -> {
            if (!resizing) {
                Scheduler.get().scheduleDeferred(() -> {
                    getView().resizeToFullHeight();
                    resizing = false;
                });
                resizing = true;
            }
        }));
        getMainModel().getItemsChangedEvent().addListener((ev, sender, args) -> {
            if (currentPlace != null) {
                prepareFromRequest(currentPlace);
            }
            if (getMainModel().getSelectedItem() == null && isVisible()) {
                // Item has been removed, switch to main view.
                placeManager.revealPlace(getMainContentRequest());
            }
        });

        getSelectedMainItems().registerListener(this);
        setInSlot(TYPE_SetActionPanel, getActionPanelPresenterWidget());
    }

    @Override
    public void itemChanged(T item) {
        boolean widgetVisible = getView().asWidget().isVisible();
        if (item != null && widgetVisible) {
            placeManager.setFragmentParameters(getFragmentParamsFromEntity(item));
            getView().setMainSelectedItem(item);
        } else if (item == null && widgetVisible && (getMainModel().getItems() == null
                || getMainModel().getItems().isEmpty())) {
            // No selection so we can't positively show anything, switch to grid.
            placeManager.revealPlace(getMainContentRequest());
        }
    }

    protected Map<String, String> getFragmentParamsFromEntity(T item) {
        Map<String, String> result = new HashMap<>();
        if (item != null) {
            result.put(FragmentParams.NAME.getName(), ((Nameable) item).getName());
        }
        return result;
    }

    /**
     * Updates the detail model with items currently selected in the main tab.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void updateDetailModelSelection() {
        if (modelProvider instanceof SearchableDetailModelProvider) {
            List<?> selectedItems = getSelectedItems();
            ((SearchableDetailModelProvider) modelProvider).setSelectedItems(selectedItems);
            DetailItemSelectionChangeEvent.fire(AbstractSubTabPresenter.this,
                    selectedItems != null ? selectedItems : new ArrayList<>());
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
        getView().resizeToFullHeight();

        // Notify model provider that the tab has been revealed
        modelProvider.onSubTabSelected();

        T entity = (T) modelProvider.getModel().getEntity();
        if (entity != null) {
            onDetailModelEntityChange(entity);
        }
        if (hasActionPanelPresenterWidget() && getTable() != null) {
            getTable().setActionMenus(getActionPanelPresenterWidget().getActionButtons());
        }
        placeManager.setFragmentParameters(getFragmentParamsFromEntity(entity), false);
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

        Set<FragmentParams> params = FragmentParams.getParams(request);

        if (params.isEmpty()) {
            // Try to obtain previous parameters if possible.
            params = FragmentParams.getParams(placeManager.getCurrentPlaceRequest());
        }
        final String fragmentNameValue = request.getParameter(FragmentParams.NAME.getName(), "");
        final List<T> itemToSwitchTo;
        if (params.contains(FragmentParams.NAME)) {
            // Someone passed a fragment with a name here.
            itemToSwitchTo = switchToName(fragmentNameValue, request);
            if (itemToSwitchTo == null || itemToSwitchTo.isEmpty()) {
                currentPlace = request;
            } else {
                currentPlace = null;
            }
        } else {
            itemToSwitchTo = getMainModel().getSelectionModel().getSelectedObjects();
            currentPlace = null;
        }
        // Give the selection model time to resolve before trying to reveal the detail tab.
        Scheduler.get().scheduleDeferred(() -> {
            // Check if there is a name parameter so we can preselect something.
            if (itemToSwitchTo != null && !itemToSwitchTo.isEmpty()) {
                // Reveal presenter only when there is something selected in the main tab
                getProxy().manualReveal(this);
            } else if ("".equals(fragmentNameValue) ||
                    (!"".equals(fragmentNameValue) &&
                            getMainModel().getDefaultSearchString().equals(getMainModel().getSearchString()) &&
                            getMainModel().getItems() != null)) {
                // Show the main view in the following cases:
                // 1. No fragment is provided and thus we can't locate the entity to show.
                // 2. A fragment is provided but it is not a valid entity. This is the case if the value is not empty
                // and the search string is set back to the default search string after attempting to search for
                // the name, and the items are not null, meaning at least one attempt at loading the data has been
                // made.
                getProxy().manualRevealFailed();
                placeManager.revealPlace(getMainContentRequest());
                currentPlace = null;
            }
        });
    }

    private List<T> switchToName(String name, PlaceRequest request) {
        if (!"".equals(name)) {
            List<T> namedItems = (List<T>) FragmentParams.findItemByName(name, getMainModel());
            if (namedItems != null && !namedItems.isEmpty()) {
                final List<T> filteredItems = filterByAdditionalParams(namedItems, request);
                getMainModel().getSelectionModel().clear();
                // This needs to be deferred, so the 'clear' is registered by the selection model. The selection model
                // schedules its resolution of changes so we want the clear to happen so when we select the entity
                // it updates properly in case what we selected was already selected
                // (so a selection changed event fires).
                Scheduler.get().scheduleDeferred(
                        () -> filteredItems.forEach(item -> getMainModel().getSelectionModel().setSelected(item, true)));
                namedItems = filteredItems;
            } else if (getMainModel().getItems() != null) {
                // Items loaded and not found.
                String searchForNameString = getMainModel().getDefaultSearchString() + "name=" + name; //$NON-NLS-1$
                if (searchForNameString.equals(getMainModel().getSearchString())) {
                    // Searched for the string and couldn't find it.
                    getMainModel().setSearchString(getMainModel().getDefaultSearchString());
                } else {
                    getMainModel().setSearchString(searchForNameString);
                }
                getMainModel().getSearchCommand().execute();
            } else {
                // Not loaded yet, load items.
                getMainModel().getSearchCommand().execute();
            }
            return namedItems;
        }
        return null;
    }

    /**
     * Filter by any additional fragment parameters.
     * @param namedItems The list of un-filtered items.
     * @param param The set of parameters.
     * @return the filtered list of named items.
     */
    protected List<T> filterByAdditionalParams(List<T> namedItems, PlaceRequest request) {
        return namedItems;
    }

    @SuppressWarnings("unchecked")
    protected ListWithDetailsModel<M, D, T> getMainModel() {
        return modelProvider.getMainModel();
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

    @Override
    public void handlePlaceTransition(String nameToken, Map<String, String> parameters) {
        final Builder builder = new Builder();
        builder.nameToken(nameToken);
        builder.with(parameters);
        placeManager.revealPlace(builder.build());
    }
}
