package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterWarningsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterWarningsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ClusterWarningsModel, ClusterWarningsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ClusterWarningsModel> {
    }

    @Inject
    public ClusterWarningsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
