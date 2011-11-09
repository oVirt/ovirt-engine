package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;

public class EventModelProvider extends SearchableTabModelProvider<AuditLog, EventListModel> {
    private final ClientGinjector ginjector;

    public ClientGinjector getGinjector() {
        return ginjector;
    }

    @Inject
    public EventModelProvider(ClientGinjector ginjector) {
        super(ginjector);
        this.ginjector = ginjector;
        init();
    }

    protected void init() {
        getModel().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateDataProvider((List<AuditLog>) getModel().getItems());
            }
        });
    }

    @Override
    public void setSelectedItems(List<AuditLog> items) {

    }

    @Override
    public EventListModel getModel() {
        return (EventListModel) getCommonModel().getEventList();
    }

}
