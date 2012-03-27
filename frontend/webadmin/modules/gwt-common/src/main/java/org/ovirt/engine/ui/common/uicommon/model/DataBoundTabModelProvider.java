package org.ovirt.engine.ui.common.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.common.system.ApplicationFocusChangeEvent;
import org.ovirt.engine.ui.common.system.ApplicationFocusChangeEvent.ApplicationFocusChangeHandler;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

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
public abstract class DataBoundTabModelProvider<T, M extends SearchableListModel> extends TabModelProvider<M> implements SearchableTableModelProvider<T, M> {

    private final AsyncDataProvider<T> dataProvider;

    public DataBoundTabModelProvider(BaseClientGinjector ginjector) {
        this(ginjector, null);
    }

    public DataBoundTabModelProvider(BaseClientGinjector ginjector, ProvidesKey<T> keyProvider) {
        super(ginjector);

        dataProvider = new AsyncDataProvider<T>(keyProvider) {
            @Override
            protected void onRangeChanged(HasData<T> display) {
                // We might get here after the ItemsChangedEvent has been triggered
                updateData();
            }
        };

        // Add handler to be notified when the application window gains or looses its focus
        getEventBus().addHandler(ApplicationFocusChangeEvent.getType(), new ApplicationFocusChangeHandler() {
            @Override
            public void onApplicationFocusChange(ApplicationFocusChangeEvent event) {
                DataBoundTabModelProvider.this.onWindowFocusChange(event.isInFocus());
            }
        });
    }

    /**
     * Callback fired when the application window gains or looses its focus.
     */
    protected void onWindowFocusChange(boolean inFocus) {
        if (isModelReady()) {
            if (inFocus) {
                getModel().toForground();
            } else {
                getModel().toBackground();
            }
        }
    }

    /**
     * @return {@code true} when {@link #getModel} can be safely called to retrieve the model, {@code false} otherwise.
     */
    protected boolean isModelReady() {
        return getCommonModel() != null;
    }

    @Override
    protected void onCommonModelChange() {
        super.onCommonModelChange();

        // Add model items change handler
        getModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (handleItemsChangedEvent()) {
                    updateData();
                }
            }
        });
    }

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
        getModel().getSearchNextPageCommand().Execute();
    }

    @Override
    public void goBack() {
        getModel().getSearchPreviousPageCommand().Execute();
    }

    @Override
    public void refresh() {
        getModel().getForceRefreshCommand().Execute();
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
        List<T> items = (List<T>) getModel().getItems();

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

    protected AsyncDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    @Override
    public void addDataDisplay(HasData<T> display) {
        dataProvider.addDataDisplay(display);
    }

}
