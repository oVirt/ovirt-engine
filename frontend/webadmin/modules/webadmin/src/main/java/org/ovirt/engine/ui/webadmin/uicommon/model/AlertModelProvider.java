package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;

public class AlertModelProvider extends SearchableTabModelProvider<AuditLog, AlertListModel> {
    private final ClientGinjector ginjector;

    public ClientGinjector getGinjector() {
        return ginjector;
    }

    @Inject
    public AlertModelProvider(ClientGinjector ginjector) {
        super(ginjector);
        this.ginjector = ginjector;
        init();
    }

    protected void init() {
        getModel().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateDataProvider(getModel().getItems());
            }
        });
    }

    @Override
    public void setSelectedItems(List<AuditLog> items) {

    }

    @Override
    public AlertListModel getModel() {
        return getCommonModel().getAlertList();
    }

    @Override
    protected void onCommonModelChange() {
        // TODO Auto-generated method stub
        super.onCommonModelChange();
    }

}
