package org.ovirt.engine.ui.common.uicommon.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Provider;

/**
 * A {@link SearchableModelProvider} implementation that provides data to {@link HasData} widgets.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public abstract class DataBoundTabModelProvider<T, M extends SearchableListModel> extends TabModelProvider<M> implements SearchableTableModelProvider<T, M> {

    private final AsyncDataProvider<T> dataProvider;
    private Comparator<T> defaultItemComparator;

    public DataBoundTabModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        this(eventBus, defaultConfirmPopupProvider, null);
    }

    public DataBoundTabModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            ProvidesKey<T> keyProvider) {
        super(eventBus, defaultConfirmPopupProvider);

        this.dataProvider = new AsyncDataProvider<T>(keyProvider) {
            @Override
            protected void onRangeChanged(HasData<T> display) {
                // We might get here after the ItemsChangedEvent has been triggered
                updateData();
            }
        };
    }

    @Override
    protected void initializeModelHandlers(M model) {
        super.initializeModelHandlers(model);

        // Add model items change handler
        model.getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (handleItemsChangedEvent()) {
                    updateData();
                }
            }
        });
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                    clearData();
                }
            }
        });
    }

    void clearData() {
        getDataProvider().updateRowCount(0, false);
    }

    /**
     * @return {@code true} when the ItemsChangedEvent of the model should trigger data update, {@code false} otherwise.
     */
    protected boolean handleItemsChangedEvent() {
        return true;
    }

    @Override
    public void setSelectedItems(List<T> items) {
        // Order is important
        getModel().setSelectedItem(items.size() > 0 ? items.get(0) : null);
        getModel().setSelectedItems(items);
    }

    @Override
    public void onManualRefresh() {
        //Do nothing by default.
    }

    @Override
    public Object getKey(T item) {
        if (item instanceof IVdcQueryable) {
            return ((IVdcQueryable) item).getQueryableId();
        }

        return getDataProvider().getKey(item);
    }

    @Override
    public boolean canGoForward() {
        return getModel().getSearchNextPageCommand().getIsExecutionAllowed();
    }

    @Override
    public boolean canGoBack() {
        return getModel().getSearchPreviousPageCommand().getIsExecutionAllowed();
    }

    @Override
    public void goForward() {
        getModel().getSearchNextPageCommand().execute();
    }

    @Override
    public void goBack() {
        getModel().getSearchPreviousPageCommand().execute();
    }

    @Override
    public void refresh() {
        getModel().getForceRefreshCommand().execute();
    }

    @Override
    public String getItemsCount() {
        return getModel().getItemsCountString();
    }

    /**
     * Retrieves current data from model and updates the data provider.
     */
    @SuppressWarnings("unchecked")
    protected void updateData() {
        List<T> items = getModel().getItems() == null ? null : new ArrayList<T>(getModel().getItems());

        if (items != null) {
            // use default item order, unless the items are already sorted
            if (!getModel().hasItemsSorted()) {
                Collections.sort(items, getDefaultItemComparator());
            }

            updateDataProvider(items);
        }
    }

    /**
     * Updates the data provider with new data received from model.
     */
    protected void updateDataProvider(List<T> items) {
        dataProvider.updateRowCount(items.size(), true);
        dataProvider.updateRowData(0, items);
    }

    protected AsyncDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    /**
     * Returns the default {@link Comparator} to use when setting new data.
     */
    protected Comparator<T> getDefaultItemComparator() {
        if (defaultItemComparator == null) {
            final Comparator<T> nameComparator = new Comparator<T>() {
                @Override
                public int compare(T a, T b) {
                    String name1 = (a instanceof Nameable) ? ((Nameable) a).getName() : null;
                    String name2 = (b instanceof Nameable) ? ((Nameable) b).getName() : null;
                    return LexoNumericComparator.comp(name1, name2);
                }
            };

            final Comparator<T> idComparator = new Comparator<T>() {
                // inspired by Java 8 Comparators.NullComparator (nullFirst = false)
                private final Comparator<Comparable> nullComparator = new Comparator<Comparable>() {
                    @Override
                    public int compare(Comparable a, Comparable b) {
                        if (a == null) {
                            return (b == null) ? 0 : 1;
                        } else if (b == null) {
                            return -1;
                        } else {
                            return a.compareTo(b);
                        }
                    }
                };

                @Override
                public int compare(T a, T b) {
                    Object id1 = (a instanceof IVdcQueryable) ? ((IVdcQueryable) a).getQueryableId() : null;
                    Object id2 = (b instanceof IVdcQueryable) ? ((IVdcQueryable) b).getQueryableId() : null;
                    Comparable idComp1 = (id1 instanceof Comparable) ? (Comparable) id1 : null;
                    Comparable idComp2 = (id2 instanceof Comparable) ? (Comparable) id2 : null;
                    return nullComparator.compare(idComp1, idComp2);
                }
            };

            // chain comparators like in Java 8 Comparator.thenComparing
            defaultItemComparator = new Comparator<T>() {
                @Override
                public int compare(T a, T b) {
                    int res = nameComparator.compare(a, b);
                    return (res != 0) ? res : idComparator.compare(a, b);
                }
            };
        }
        return defaultItemComparator;
    }

    @Override
    public void addDataDisplay(HasData<T> display) {
        dataProvider.addDataDisplay(display);
    }

}
