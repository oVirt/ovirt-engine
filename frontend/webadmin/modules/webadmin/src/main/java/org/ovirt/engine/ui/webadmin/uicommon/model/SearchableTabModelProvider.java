package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

/**
 * Default {@link SearchableTableModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public abstract class SearchableTabModelProvider<T, M extends SearchableListModel> extends DataBoundTabModelProvider<T, M> implements SearchableTableModelProvider<T, M> {

    public SearchableTabModelProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    protected void onCommonModelChange() {
        super.onCommonModelChange();

        // Add necessary property change handlers
        getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;

                // For EventListModel classes: update data whenever the last event changes
                if ("LastEvent".equals(pcArgs.PropertyName)) {
                    updateData();
                }
            }
        });
    }

    @Override
    public void setSelectedItems(List<T> items) {
        // Order is important
        if (items.size() > 0) {
            getModel().setSelectedItem(items.get(0));
        } else {
            getModel().setSelectedItem(null);
        }

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

}
