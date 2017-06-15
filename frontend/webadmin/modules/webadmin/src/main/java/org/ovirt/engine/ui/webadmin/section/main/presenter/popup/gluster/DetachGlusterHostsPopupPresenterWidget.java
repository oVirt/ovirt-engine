package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.DetachGlusterHostsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DetachGlusterHostsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<DetachGlusterHostsModel, DetachGlusterHostsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<DetachGlusterHostsModel> {
    }

    @Inject
    public DetachGlusterHostsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
