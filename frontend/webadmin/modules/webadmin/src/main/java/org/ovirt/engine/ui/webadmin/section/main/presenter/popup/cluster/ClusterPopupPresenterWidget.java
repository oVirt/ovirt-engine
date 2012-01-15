package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ClusterModel, ClusterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ClusterModel> {
    }

    @Inject
    public ClusterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
