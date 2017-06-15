package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoReplicationSessionConfigModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GlusterVolumeGeoReplicationSessionConfigModel, GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterVolumeGeoReplicationSessionConfigModel> {
    }

    @Inject
    public GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final GlusterVolumeGeoReplicationSessionConfigModel model) {
        super.init(model);
    }
}
