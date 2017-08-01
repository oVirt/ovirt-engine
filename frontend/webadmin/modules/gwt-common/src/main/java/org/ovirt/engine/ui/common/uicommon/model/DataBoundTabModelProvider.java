package org.ovirt.engine.ui.common.uicommon.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionModel;
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

    private AsyncDataProvider<T> dataProvider;
    private Comparator<T> defaultItemComparator;

    public DataBoundTabModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    @Override
    protected void initializeModelHandlers(M model) {
        super.initializeModelHandlers(model);

        // Add model items change handler
        model.getItemsChangedEvent().addListener((ev, sender, args) -> {
            if (handleItemsChangedEvent()) {
                updateData();
            }
        });
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                clearData();
            }
        });
    }

    void clearData() {
        getDataProvider().updateRowCount(0, false);
    }

    public SelectionModel<T> getSelectionModel() {
        return getModel().getSelectionModel();
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

    @Override
    public int getFromCount() {
        return getModel().getSearchPageSize() * (getModel().getSearchPageNumber() - 1) + 1;
    }

    @Override
    public int getToCount() {
        return (getFromCount() - 1) + (getModel().getItems() == null ? 0 : getModel().getItems().size());
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
                items.sort(getDefaultItemComparator());
            }

            updateDataProvider(items);
        }
    }

    /**
     * Updates the data provider with new data received from model.
     */
    protected void updateDataProvider(List<T> items) {
        getDataProvider().updateRowCount(items.size(), true);
        getDataProvider().updateRowData(0, items);
    }

    protected AsyncDataProvider<T> getDataProvider() {
        if (dataProvider == null) {
            dataProvider = new AsyncDataProvider<T>(getModel().getSelectionModel()) {
                @Override
                protected void onRangeChanged(HasData<T> display) {
                    // We might get here after the ItemsChangedEvent has been triggered
                    updateData();
                }
            };
        }
        return dataProvider;
    }

    /**
     * Returns the default {@link Comparator} to use when setting new data.
     */
    protected Comparator<T> getDefaultItemComparator() {
        if (defaultItemComparator == null) {
            Comparator<T> nameComparator = (o1, o2) -> {
                String name1 = (o1 instanceof Nameable) ? ((Nameable) o1).getName() : null;
                String name2 = (o2 instanceof Nameable) ? ((Nameable) o2).getName() : null;
                return LexoNumericComparator.comp(name1, name2);
            };

            Comparator<T> idComparator = Comparator.comparing((Function<T, Comparable>) item -> {
                Object id = (item instanceof Queryable) ? ((Queryable) item).getQueryableId() : null;
                return (id instanceof Comparable) ? (Comparable) id : null;
            }, Comparator.nullsLast(Comparator.naturalOrder()));

            defaultItemComparator = nameComparator.thenComparing(idComparator);
        }
        return defaultItemComparator;
    }

    @Override
    public void addDataDisplay(HasData<T> display) {
        getDataProvider().addDataDisplay(display);
    }

}
