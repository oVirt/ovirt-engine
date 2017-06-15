package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VolumeParameterPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VolumeParameterModel, VolumeParameterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VolumeParameterModel> {
    }

    @Inject
    public VolumeParameterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
