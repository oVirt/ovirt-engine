package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;

public class ShowHideVfPresenterWidget extends ToggleButtonPresenterWidget {

    public interface ViewDef extends ToggleButtonPresenterWidget.ViewDef {
    }

    @Inject
    public ShowHideVfPresenterWidget(EventBus eventBus, ShowHideVfPresenterWidget.ViewDef view) {
        super(eventBus, view);
    }
}
