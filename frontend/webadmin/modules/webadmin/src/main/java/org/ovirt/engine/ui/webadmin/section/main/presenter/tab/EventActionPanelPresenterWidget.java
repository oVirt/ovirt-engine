package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;

import com.google.web.bindery.event.shared.EventBus;

public class EventActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, AuditLog, EventListModel<Void>> {

    @Inject
    public EventActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, AuditLog> view,
            MainModelProvider<AuditLog, EventListModel<Void>> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
    }
}
