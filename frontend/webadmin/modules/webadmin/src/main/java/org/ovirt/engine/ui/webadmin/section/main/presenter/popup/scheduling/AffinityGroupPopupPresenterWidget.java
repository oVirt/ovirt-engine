package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model.AffinityGroupModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AffinityGroupPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<AffinityGroupModel, AffinityGroupPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AffinityGroupModel> {
    }

    @Inject
    public AffinityGroupPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}

