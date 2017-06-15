package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.users.EventNotificationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ManageEventsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<EventNotificationModel, ManageEventsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<EventNotificationModel> {
    }

    @Inject
    public ManageEventsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
