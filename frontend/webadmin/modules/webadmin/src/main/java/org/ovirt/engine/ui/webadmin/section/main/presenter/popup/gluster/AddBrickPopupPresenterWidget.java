package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AddBrickPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VolumeBrickModel, AddBrickPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VolumeBrickModel> {
    }

    @Inject
    public AddBrickPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
