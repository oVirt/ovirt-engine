package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.events.EventModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EventPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<EventModel, EventPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<EventModel> {
    }

    @Inject
    public EventPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}

