package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VolumePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VolumeModel, VolumePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VolumeModel> {
    }

    @Inject
    public VolumePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
