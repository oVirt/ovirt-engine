package org.ovirt.engine.ui.common.presenter;

import java.util.List;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.common.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

/**
 * Base class for presenters representing sub tabs that react to item selection changes within main tab presenters.
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
public abstract class AbstractSubTabPresenter<T, M extends ListWithDetailsModel, D extends EntityModel, V extends AbstractSubTabPresenter.ViewDef<T>, P extends TabContentProxyPlace<?>>
        extends AbstractTabPresenter<V, P> {

    // TODO(vszocs) use HasActionTable<I> instead of raw type HasActionTable, this will
    // require adding new type parameter to presenter (do later as part of refactoring)
    @SuppressWarnings("rawtypes")
    public interface ViewDef<T> extends View, HasActionTable {

        /**
         * Notifies the view that the main tab item selection has changed.
         */
        void setMainTabSelectedItem(T selectedItem);

    }

    private static final Logger logger = Logger.getLogger(AbstractSubTabPresenter.class.getName());

    private final PlaceManager placeManager;
    private final DetailModelProvider<M, D> modelProvider;

    private List<T> mainTabSelectedItems;

    public AbstractSubTabPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, DetailModelProvider<M, D> modelProvider,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
        this.placeManager = placeManager;
        this.modelProvider = modelProvider;
    }

    @Override
    protected ActionTable<?> getTable() {
        return getView().getTable();
    }

    @Override
    protected void onBind() {
        super.onBind();

        OrderedMultiSelectionModel<?> tableSelectionModel = getTable() != null ? getTable().getSelectionModel() : null;
        if (tableSelectionModel != null) {
            registerHandler(tableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    // Update detail model selection
                    updateDetailModelSelection();
                }
            }));
        }
        initializeHandlers();
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
    }

    @Override
    protected void onHide() {
        super.onHide();
        modelProvider.onSubTabDeselected();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Clear table selection before starting
        clearSelection();

        if (getTable() != null) {
            getTable().resetScrollPosition();
        }
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        // Reveal presenter only when there is something selected in the main tab
        if (hasMainTabSelection()) {
            getProxy().manualReveal(this);
        } else {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(getMainTabRequest());
        }
    }

    protected boolean hasMainTabSelection() {
        return mainTabSelectedItems != null && !mainTabSelectedItems.isEmpty();
    }

    protected T getMainTabSelectedItem() {
        return hasMainTabSelection() ? mainTabSelectedItems.get(0) : null;
    }

    /**
     * Notifies this sub tab presenter that the main tab selection has changed.
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
     * Will be revealed when the user tries to access this sub tab while there is nothing selected in the main tab.
     */
    protected abstract PlaceRequest getMainTabRequest();

    /**
     * Returns items currently selected in the table or {@code null} if the sub tab view has no table widget associated.
     */
    protected List<?> getSelectedItems() {
        return getTable() != null ? getTable().getSelectionModel().getSelectedList() : null;
    }

    /**
     * Deselects any items currently selected in the table. Does nothing if the sub tab view has no table widget
     * associated.
     */
    protected void clearSelection() {
        if (getTable() != null) {
            getTable().getSelectionModel().clear();
        }
    }

    public void initializeHandlers() {
        // Notify view when the entity of the detail model changes
        modelProvider.getModel().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                Object entity = modelProvider.getModel().getEntity();
                if (entity != null) {
                    onDetailModelEntityChange(entity);
                }
            }
        });

        // Notify view when the detail model indicates progress
        modelProvider.getModel().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                    if (modelProvider.getModel().getProgress() != null) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                            @Override
                            public void execute() {
                                if (getTable() != null) {
                                    getTable().setLoadingState(LoadingState.LOADING);
                                }
                            }
                        });
                    }
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
            getView().setMainTabSelectedItem((T) entity);
        } catch (ClassCastException ex) {
            // Detail model entity type is different from main model item type.
            // This usually happens with synthetic item types that wrap multiple
            // logical entities into single type. Since views can typically edit
            // a single item type, we can do nothing here.
            logger.warning("Detail model entity type is different from main model item type"); //$NON-NLS-1$
        }
    }

    protected DetailModelProvider<M, D> getModelProvider() {
        return modelProvider;
    }

}
