package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A {@link SearchableModelProvider} implementation that provides data to {@link HasData} widgets.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public abstract class DataBoundTabModelProvider<T, M extends SearchableListModel> extends TabModelProvider<M> implements SearchableModelProvider<T, M> {

    private final AsyncDataProvider<T> dataProvider;

    public DataBoundTabModelProvider(ClientGinjector ginjector) {
        this(ginjector, null);
    }

    public DataBoundTabModelProvider(ClientGinjector ginjector, ProvidesKey<T> keyProvider) {
        super(ginjector);

        dataProvider = new AsyncDataProvider<T>(keyProvider) {
            @Override
            protected void onRangeChanged(HasData<T> display) {
                // We might get here after the ItemsChangedEvent has been triggered
                updateData();
            }
        };
    }

    @Override
    protected void onCommonModelChange() {
        super.onCommonModelChange();

        // Add model items change handler
        getModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateData();
            }
        });
    }

    /**
     * Retrieves current data from model and updates the data provider.
     */
    @SuppressWarnings("unchecked")
    protected void updateData() {
        List<T> items = (List<T>) getModel().getItems();

        if (items == null && handleNullDataAsEmpty()) {
            items = new ArrayList<T>();
        }

        if (items != null) {
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

    /**
     * @return {@code true} to handle {@code null} data as empty data passed to data provider, {@code false} to avoid
     *         handling {@code null} data at all.
     */
    protected boolean handleNullDataAsEmpty() {
        return false;
    }

    protected AsyncDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    /**
     * Adds a {@link HasData} widget to the data provider.
     */
    public void addDataDisplay(HasData<T> display) {
        dataProvider.addDataDisplay(display);
    }

}
