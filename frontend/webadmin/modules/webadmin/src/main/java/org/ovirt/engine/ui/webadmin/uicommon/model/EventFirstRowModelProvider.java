package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;

public class EventFirstRowModelProvider extends EventModelProvider {

    @Inject
    public EventFirstRowModelProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    protected void updateDataProvider(List<AuditLog> items) {
        List<AuditLog> firstRowData = items.isEmpty() ? items : Arrays.asList(items.get(0));
        super.updateDataProvider(firstRowData);
    }

}
