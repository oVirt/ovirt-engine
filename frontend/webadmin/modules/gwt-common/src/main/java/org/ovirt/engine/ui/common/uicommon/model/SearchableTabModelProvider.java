package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Default {@link SearchableTableModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public abstract class SearchableTabModelProvider<T, M extends SearchableListModel> extends DataBoundTabModelProvider<T, M> implements SearchableTableModelProvider<T, M> {

    public SearchableTabModelProvider(BaseClientGinjector ginjector) {
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

}
