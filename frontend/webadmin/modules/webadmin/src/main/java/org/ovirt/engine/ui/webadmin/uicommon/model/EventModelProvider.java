package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class EventModelProvider extends SearchableTabModelProvider<AuditLog, EventListModel> {

    @Inject
    public EventModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

}
