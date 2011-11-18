package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;

public class AlertModelProvider extends SearchableTabModelProvider<AuditLog, AlertListModel> {

    public interface AlertCountChangeHandler {

        void onAlertCountChange(int count);

    }

    private AlertCountChangeHandler alertCountChangeHandler;

    @Inject
    public AlertModelProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    public void setAlertCountChangeHandler(AlertCountChangeHandler alertCountChangeHandler) {
        this.alertCountChangeHandler = alertCountChangeHandler;
    }

    @Override
    protected void updateDataProvider(List<AuditLog> items) {
        if (alertCountChangeHandler != null) {
            alertCountChangeHandler.onAlertCountChange(items.size());
        }

        super.updateDataProvider(items);
    }

    @Override
    public AlertListModel getModel() {
        return getCommonModel().getAlertList();
    }

}
