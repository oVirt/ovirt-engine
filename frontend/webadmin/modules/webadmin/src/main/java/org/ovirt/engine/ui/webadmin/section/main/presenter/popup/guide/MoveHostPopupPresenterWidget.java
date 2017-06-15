package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class MoveHostPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<MoveHost, MoveHostPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<MoveHost> {
    }

    @Inject
    public MoveHostPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
