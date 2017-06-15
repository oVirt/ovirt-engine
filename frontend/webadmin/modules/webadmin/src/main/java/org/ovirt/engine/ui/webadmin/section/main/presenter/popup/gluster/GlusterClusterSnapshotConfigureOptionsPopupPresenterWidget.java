package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterClusterSnapshotConfigModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GlusterClusterSnapshotConfigModel, GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget.ViewDef> {
    @Inject
    public GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget(EventBus eventBus,
            ViewDef view,
            Provider<GlusterVolumeSnapshotConfigureOptionsPopupPresenterWidget> snapshotPopupProvider,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupPrivder) {
        super(eventBus, view);
    }

    @Override
    public void init(final GlusterClusterSnapshotConfigModel model) {
        super.init(model);
    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterClusterSnapshotConfigModel> {
    }

}
