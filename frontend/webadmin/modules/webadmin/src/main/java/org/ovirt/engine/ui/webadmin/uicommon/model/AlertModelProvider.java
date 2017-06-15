package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AlertModelProvider extends SearchableTabModelProvider<AuditLog, AlertListModel> {

    public interface AlertCountChangeHandler {

        void onAlertCountChange(int count);

    }

    private AlertCountChangeHandler alertCountChangeHandler;

    @Inject
    public AlertModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
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

}
