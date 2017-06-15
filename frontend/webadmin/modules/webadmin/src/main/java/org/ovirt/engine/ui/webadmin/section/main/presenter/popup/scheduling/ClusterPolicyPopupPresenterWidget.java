package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterPolicyPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<NewClusterPolicyModel, ClusterPolicyPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<NewClusterPolicyModel> {
    }

    @Inject
    public ClusterPolicyPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
