package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeProfileStatisticsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VolumeProfileStatisticsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VolumeProfileStatisticsModel, VolumeProfileStatisticsPopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VolumeProfileStatisticsModel> {
    }

    @Inject
    public VolumeProfileStatisticsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
