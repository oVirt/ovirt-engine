package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;

public class EventModelProvider extends SearchableTabModelProvider<AuditLog, EventListModel> {

    @Inject
    public EventModelProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    public EventListModel getModel() {
        return (EventListModel) getCommonModel().getEventList();
    }

}
