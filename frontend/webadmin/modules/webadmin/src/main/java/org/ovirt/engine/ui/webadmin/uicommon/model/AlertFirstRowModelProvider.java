package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AlertFirstRowModelProvider extends AlertModelProvider {

    @Inject
    public AlertFirstRowModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    @Override
    protected void updateDataProvider(List<AuditLog> items) {
        List<AuditLog> firstRowData = items.isEmpty() ? items : Arrays.asList(items.get(0));
        super.updateDataProvider(firstRowData);
    }

}
